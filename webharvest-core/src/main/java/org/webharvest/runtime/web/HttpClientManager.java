/*  Copyright (c) 2006-2007, Vladimir Nikic
    All rights reserved.

    Redistribution and use of this software in source and binary forms,
    with or without modification, are permitted provided that the following
    conditions are met:

    * Redistributions of source code must retain the above
      copyright notice, this list of conditions and the
      following disclaimer.

    * Redistributions in binary form must reproduce the above
      copyright notice, this list of conditions and the
      following disclaimer in the documentation and/or other
      materials provided with the distribution.

    * The name of Web-Harvest may not be used to endorse or promote
      products derived from this software without specific prior
      written permission.

    THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
    AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
    IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
    ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
    LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
    CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
    SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
    INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
    CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
    ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
    POSSIBILITY OF SUCH DAMAGE.

    You can contact Vladimir Nikic by sending e-mail to
    nikic_vladimir@yahoo.com. Please include the word "Web-Harvest" in the
    subject line.
*/
package org.webharvest.runtime.web;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.httpclient.Cookie;
import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.httpclient.HttpState;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.NTCredentials;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.ProxyHost;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.contrib.ssl.EasySSLProtocolSocketFactory;
import org.apache.commons.httpclient.cookie.CookiePolicy;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.commons.httpclient.methods.multipart.ByteArrayPartSource;
import org.apache.commons.httpclient.methods.multipart.FilePart;
import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity;
import org.apache.commons.httpclient.methods.multipart.Part;
import org.apache.commons.httpclient.methods.multipart.StringPart;
import org.apache.commons.httpclient.params.HttpClientParams;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.commons.httpclient.protocol.Protocol;
import org.apache.commons.httpclient.protocol.ProtocolSocketFactory;
import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webharvest.runtime.variables.Variable;
import org.webharvest.utils.CommonUtil;

import com.google.inject.Inject;

/**
 * HTTP client functionality.
 */
public class HttpClientManager {

    private static final Logger LOG =
        LoggerFactory.getLogger(HttpClientManager.class);

    public static final String DEFAULT_USER_AGENT = "Mozilla/5.0 (Windows; U; Windows NT 5.1; en-US; rv:1.8.0.1) Gecko/20060111 Firefox/1.5.0.1";

    static {
        // registers default handling for https
        Protocol.registerProtocol("https", new Protocol("https", (ProtocolSocketFactory) new EasySSLProtocolSocketFactory(), 443));
    }

    private final HttpClient client;
    private final HttpInfo httpInfo;

    @Inject
    public HttpClientManager(final ProxySettings proxySettings) {
        this.client = new HttpClient();
        this.httpInfo = new HttpInfo(client);

        final HttpClientParams clientParams = new HttpClientParams();
        clientParams.setBooleanParameter("http.protocol.allow-circular-redirects", true);
        this.client.setParams(clientParams);

        proxySettings.apply(this.client);
    }

    public void setCookiePolicy(String cookiePolicy) {
        if (StringUtils.isBlank(cookiePolicy) || "browser".equalsIgnoreCase(cookiePolicy)) {
            client.getParams().setCookiePolicy(CookiePolicy.BROWSER_COMPATIBILITY);
            // http://hc.apache.org/httpclient-3.x/cookies.html
            client.getParams().setParameter(HttpMethodParams.SINGLE_COOKIE_HEADER, Boolean.TRUE);

        } else if ("ignore".equalsIgnoreCase(cookiePolicy)) {
            client.getParams().setCookiePolicy(CookiePolicy.IGNORE_COOKIES);

        } else if ("netscape".equalsIgnoreCase(cookiePolicy)) {
            client.getParams().setCookiePolicy(CookiePolicy.NETSCAPE);

        } else if ("rfc_2109".equalsIgnoreCase(cookiePolicy)) {
            client.getParams().setCookiePolicy(CookiePolicy.RFC_2109);

        } else {
            client.getParams().setCookiePolicy(cookiePolicy);
        }
    }

    public HttpResponseWrapper execute(
            String methodType,
            Boolean followRedirects,
            String contentType,
            String url,
            String charset,
            String username,
            String password,
            Variable bodyContent, Map<String, HttpParamInfo> params,
            Map headers, int retryAttempts, long retryDelay, double retryDelayFactor) throws InterruptedException, UnsupportedEncodingException {
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            url = "http://" + url;
        }

        url = CommonUtil.encodeUrl(url, charset);

        HttpState clientState = client.getState();

        // if username and password are specified, define new credentials for authenticaton
        if (username != null && password != null) {
            try {
                URL urlObj = new URL(url);
                clientState.setCredentials(
                        new AuthScope(urlObj.getHost(), urlObj.getPort()),
                        new UsernamePasswordCredentials(username, password)
                );
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
        }

        fixCookiesWithoutExpirationDate(clientState);

        HttpMethodBase method;
        if ("post".equalsIgnoreCase(methodType)) {
            method = createPostMethod(url, params, contentType, charset, bodyContent);
        } else {
            method = createGetMethod(url, params, charset, followRedirects);
        }

        boolean isUserAgentSpecified = false;

        // define request headers, if any exist
        if (headers != null) {
            for (Object o : headers.keySet()) {
                String headerName = (String) o;
                if ("User-Agent".equalsIgnoreCase(headerName)) {
                    isUserAgentSpecified = true;
                }
                String headerValue = (String) headers.get(headerName);
                method.addRequestHeader(new Header(headerName, headerValue));
            }
        }

        if (!isUserAgentSpecified) {
            identifyAsDefaultBrowser(method);
        }

        HttpResponseWrapper responseWrapper = null;
        try {
            responseWrapper = doExecute(url, method, followRedirects, retryAttempts, retryDelay, retryDelayFactor);
            return responseWrapper;
        } finally {
            if (responseWrapper == null) {
                // i.e. an exception need thrown
                method.releaseConnection();
            }
        }
    }

    private void fixCookiesWithoutExpirationDate(HttpState clientState) {
        // If cookie expiry date is not specified in the response, HttpClient 3.1 doesn't send it back.
        // This leads to inability to login to some sites, being always redirected to login page.
        // Workaround here is to set cookies with null expiry dates to the current date plus 1 day
        // ( patched by heysteveo - https://sourceforge.net/projects/web-harvest/forums/forum/591299/topic/4372223 post #10 )
        // todo: remove this method if HttpClient 4.x fixes the problem
        final Cookie[] cookies = clientState.getCookies();
        if (cookies != null && cookies.length > 0) {
            final Calendar defaultExpirationDate = Calendar.getInstance();
            defaultExpirationDate.setTime(new Date());
            defaultExpirationDate.add(Calendar.DAY_OF_MONTH, 1);
            for (Cookie cookie : cookies) {
                if (cookie.getExpiryDate() == null) {
                    cookie.setExpiryDate(defaultExpirationDate.getTime());
                }
            }
        }
    }

    private HttpResponseWrapper doExecute(String url, HttpMethodBase method, Boolean followRedirects,
                                          int retryAttempts, long retryDelay, double retryDelayFactor) throws InterruptedException {

        int attemptsRemain = retryAttempts;

        do {
            boolean wasException = false;
            try {
                method = executeFollowingRedirects(method, url, followRedirects);
            } catch (IOException e) {
                if (attemptsRemain == 0) {
                    throw new org.webharvest.exception.HttpException("IO error during HTTP execution for URL: " + url, e);
                }
                wasException = true;
                LOG.warn("Exception occurred during executing HTTP method {}: {}", method.getName(), e.getMessage());
            }

            if (!wasException
                    && method.getStatusCode() != HttpStatus.SC_BAD_GATEWAY
                    && method.getStatusCode() != HttpStatus.SC_SERVICE_UNAVAILABLE
                    && method.getStatusCode() != HttpStatus.SC_GATEWAY_TIMEOUT
                    && method.getStatusCode() != 509 /*Bandwidth Limit Exceeded (Apache bw/limited extension)*/) {
                // success.
                break;
            }
            if (attemptsRemain == 0) {
                throw new org.webharvest.exception.HttpException("HTTP Status: " + method.getStatusCode() + ", Url: " + url);
            }

            final long delayBeforeRetry = (long) (retryDelay * (Math.pow(retryDelayFactor, retryAttempts - attemptsRemain)));

            LOG.warn("HTTP Status: {}; URL: [{}]; Waiting for {} second(s) before retrying (attempt {} of {})...", new Object[]{
                    method.getStatusLine(), url, MILLISECONDS.toSeconds(delayBeforeRetry), retryAttempts - attemptsRemain + 1, retryAttempts});

            Thread.sleep(delayBeforeRetry);
            attemptsRemain--;
        } while (true);

        final HttpResponseWrapper httpResponseWrapper = new HttpResponseWrapper(method);
        // updates HTTP info with response's details
        this.httpInfo.setResponse(httpResponseWrapper);
        return httpResponseWrapper;
    }

    private HttpMethodBase executeFollowingRedirects(HttpMethodBase method, String url, Boolean followRedirects) throws IOException {
        final int statusCode = client.executeMethod(method);
        // POST method is not redirected automatically, so it's on our responsibility then.
        if (BooleanUtils.isTrue(followRedirects)
                && ((statusCode == HttpStatus.SC_MOVED_TEMPORARILY) ||
                (statusCode == HttpStatus.SC_MOVED_PERMANENTLY) ||
                (statusCode == HttpStatus.SC_SEE_OTHER) ||
                (statusCode == HttpStatus.SC_TEMPORARY_REDIRECT))) {
            final Header header = method.getResponseHeader("location");
            if (header != null) {
                final String nextURI = header.getValue();
                if (!CommonUtil.isEmptyString(nextURI)) {
                    method.releaseConnection();
                    final GetMethod nextMethod = new GetMethod(CommonUtil.fullUrl(url, nextURI));
                    identifyAsDefaultBrowser(nextMethod);
                    client.executeMethod(nextMethod);
                    return nextMethod;
                }
            }
        }
        return method;
    }

    /**
     * Defines "User-Agent" HTTP header.
     *
     * @param method
     */
    private void identifyAsDefaultBrowser(HttpMethodBase method) {
        method.addRequestHeader(new Header("User-Agent", DEFAULT_USER_AGENT));
    }

    private HttpMethodBase createPostMethod(String url, Map<String, HttpParamInfo> params, String contentType, String charset, Variable bodyContent)
            throws UnsupportedEncodingException {
        PostMethod method = new PostMethod(url);

        int filenameIndex = 1;
        if (params != null) {
            if ("multipart/form-data".equals(contentType)) {
                Part[] parts = new Part[params.size()];
                int index = 0;
                for (Map.Entry<String, HttpParamInfo> entry : params.entrySet()) {
                    String name = entry.getKey();
                    HttpParamInfo httpParamInfo = entry.getValue();
                    Variable value = httpParamInfo.getValue();

                    if (httpParamInfo.isFile()) {
                        String filename = httpParamInfo.getFileName();
                        if (CommonUtil.isEmptyString(filename)) {
                            filename = "uploadedfile_" + filenameIndex;
                            filenameIndex++;
                        }
                        String paramContentType = httpParamInfo.getContentType();
                        if (CommonUtil.isEmptyString(paramContentType)) {
                            paramContentType = null;
                        }

                        byte[] bytes = value.toBinary(charset);
                        parts[index] = new FilePart(httpParamInfo.getName(), new ByteArrayPartSource(filename, bytes), paramContentType, charset);
                    } else {
                        parts[index] = new StringPart(name, CommonUtil.nvl(value, ""), charset);
                    }
                    index++;
                }
                method.setRequestEntity(new MultipartRequestEntity(parts, method.getParams()));

            } else if (StringUtils.startsWith(contentType, "text/") || StringUtils.startsWith(contentType, "application/xml")) {
                method.setRequestEntity(new StringRequestEntity(bodyContent.toString(charset), contentType, charset));

            } else {
                NameValuePair[] paramArray = new NameValuePair[params.size()];
                int index = 0;
                for (Map.Entry<String, HttpParamInfo> entry : params.entrySet()) {
                    String name = entry.getKey();
                    String value = entry.getValue().getValue().toString();
                    paramArray[index++] = new NameValuePair(name, value);
                }
                method.setRequestBody(paramArray);
            }
        }

        return method;
    }

    // FIXME: package protected for testing. This is not perfect solution
    // - we need to refactor entire HttpClientManager class (splitting it into
    // multiple classes/interfaces)
    GetMethod createGetMethod(String url, Map<String, HttpParamInfo> params,
            String charset, Boolean followRedirects) {
        if (params != null) {
            final StringBuilder urlParamsBuilder = new StringBuilder();
            final Iterator<Entry<String, HttpParamInfo>> iterator =
                params.entrySet().iterator();

            while (iterator.hasNext()) {
                final Entry<String, HttpParamInfo> entry = iterator.next();
                final HttpParamInfo httpParamInfo = entry.getValue();

                try {
                    urlParamsBuilder.append(entry.getKey())
                        .append("=")
                        .append(URLEncoder.encode(CommonUtil.nvl(
                                httpParamInfo.getValue(), ""), charset));
                } catch (UnsupportedEncodingException e) {
                    throw new org.webharvest.exception.HttpException("Charset "
                            + charset + " is not supported!", e);
                }
                if (iterator.hasNext()) {
                    urlParamsBuilder.append("&");
                }
            }

            if (urlParamsBuilder.length() != 0) {
                final String urlParams = urlParamsBuilder.toString();
                if (url.indexOf("?") < 0) {
                    url += "?" + urlParams;
                } else if (url.endsWith("&")) {
                    url += urlParams;
                } else {
                    url += "&" + urlParams;
                }
            }
        }

        final GetMethod method = new GetMethod(url);
        method.setFollowRedirects(followRedirects);
        return method;
    }

    public HttpInfo getHttpInfo() {
        return httpInfo;
    }

    public HttpClient getHttpClient() {
        return client;
    }

    // ProxySettings class and its inner Builder class encapsulates logic
    // previously distributed across entire application;
    // Now, we have one class responsible for performing proxy configuration
    // - it's still too complex, but far better than the previous approach,
    // when proxy configuration logic was shared between HttpClientManager,
    // CommandLine or ConfigPanel...
    public static final class ProxySettings {

        public static final ProxySettings NO_PROXY_SET =
            new ProxySettings(null);

        private final ProxyHost proxyHost;
        private Credentials proxyCredentials;

        private ProxySettings(final ProxyHost proxyHost) {
            this.proxyHost = proxyHost;
        }

        private void setProxyCredentials(final Credentials credentials) {
            this.proxyCredentials = credentials;
        }

        void apply(final HttpClient httpClient) {
            if (this == NO_PROXY_SET) {
                return;
            }
            httpClient.getHostConfiguration().setProxyHost(proxyHost);
            httpClient.getState().setProxyCredentials(AuthScope.ANY,
                    this.proxyCredentials);
        }

        public static final class Builder {
            private final String proxyHost;
            private int proxyPort = -1;

            // proxy credentials
            private String proxyUsername;
            private String proxyPassword;
            private String proxyNTHost;
            private String proxyNTDomain;

            public Builder(final String proxyHost) {
                if (proxyHost == null || "".equals(proxyHost)) {
                    throw new IllegalArgumentException(
                            "Proxy host is required");
                }
                this.proxyHost = proxyHost;
            }

            public Builder setProxyPort(int proxyPort) {
                this.proxyPort = proxyPort;
                return this;
            }

            public Builder setProxyCredentialsUsername(final String username) {
                this.proxyUsername = username;
                return this;
            }

            public Builder setProxyCredentialsPassword(final String password) {
                this.proxyPassword = password;
                return this;
            }

            public Builder setProxyCredentialsNTHost(final String proxyNTHost) {
                this.proxyNTHost = proxyNTHost;
                return this;
            }

            public Builder setProxyCredentialsNTDomain(
                    final String proxyNTDomain) {
                this.proxyNTDomain = proxyNTDomain;
                return this;
            }

            public ProxySettings build() {
                final ProxySettings settings = new ProxySettings(
                        new ProxyHost(proxyHost, proxyPort));

                if (proxyUsername != null) {
                    settings.setProxyCredentials(createCredentials());
                }

                return settings;
            }

            private Credentials createCredentials() {
                return (proxyNTHost == null || proxyNTDomain == null
                        || "".equals(proxyNTHost.trim()) || "".equals(proxyNTDomain.trim())) ?
                            new UsernamePasswordCredentials(proxyUsername, proxyPassword) :
                            new NTCredentials(proxyUsername, proxyPassword, proxyNTHost, proxyNTDomain);
            }
        }
    }
}