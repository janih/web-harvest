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
package org.webharvest.runtime.processors;

import org.apache.commons.lang.StringUtils;
import org.webharvest.definition.HttpDef;
import org.webharvest.exception.HttpException;
import org.webharvest.runtime.DynamicScopeContext;
import org.webharvest.runtime.Scraper;
import org.webharvest.runtime.templaters.BaseTemplater;
import org.webharvest.runtime.variables.EmptyVariable;
import org.webharvest.runtime.variables.NodeVariable;
import org.webharvest.runtime.variables.Variable;
import org.webharvest.runtime.web.HttpClientManager;
import org.webharvest.runtime.web.HttpParamInfo;
import org.webharvest.runtime.web.HttpResponseWrapper;
import org.webharvest.utils.CommonUtil;
import org.webharvest.utils.KeyValuePair;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.webharvest.utils.CommonUtil.getBooleanValue;

/**
 * Http processor.
 */
public class HttpProcessor extends AbstractProcessor<HttpDef> {

    private static final String HTML_META_CHARSET_REGEX =
            "(<meta\\s*http-equiv\\s*=\\s*(\"|')content-type(\"|')\\s*content\\s*=\\s*(\"|')text/html;\\s*charset\\s*=\\s*(.*?)(\"|')\\s*/?>)";

    private Map<String, HttpParamInfo> httpParams = new LinkedHashMap<String, HttpParamInfo>();
    private Map<String, String> httpHeaderMap = new HashMap<String, String>();

    public HttpProcessor(HttpDef httpDef) {
        super(httpDef);
    }

    public Variable execute(Scraper scraper, DynamicScopeContext context) throws InterruptedException {
        scraper.setRunningHttpProcessor(this);

        final String url = BaseTemplater.evaluateToString(elementDef.getUrl(), null, scraper);
        final String method = BaseTemplater.evaluateToString(elementDef.getMethod(), null, scraper);
        final Boolean followRedirects = getBooleanValue(BaseTemplater.evaluateToString(elementDef.getFollowRedirects(), null, scraper), true);
        final Boolean isMultipart = getBooleanValue(BaseTemplater.evaluateToString(elementDef.getMultipart(), null, scraper), false);
        final String specifiedCharset = BaseTemplater.evaluateToString(elementDef.getCharset(), null, scraper);
        final String username = BaseTemplater.evaluateToString(elementDef.getUsername(), null, scraper);
        final String password = BaseTemplater.evaluateToString(elementDef.getPassword(), null, scraper);
        final String cookiePolicy = BaseTemplater.evaluateToString(elementDef.getCookiePolicy(), null, scraper);
        final boolean skipResponseBody = getBooleanValue(BaseTemplater.evaluateToString(elementDef.getIgnoreResponseBody(), null, scraper), false);

        final int retryAttempts = BaseTemplater.evaluateToVariable(elementDef.getRetryAttempts(), null, scraper).toInt();
        final long retryDelay = BaseTemplater.evaluateToVariable(elementDef.getRetryDelay(), null, scraper).toLong();
        final double retryDelayFactor = BaseTemplater.evaluateToVariable(elementDef.getRetryDelayFactor(), null, scraper).toDouble();

        String charset = specifiedCharset;

        if (charset == null) {
            charset = scraper.getConfiguration().getCharset();
        }

        final String encodedUrl = CommonUtil.encodeUrl(url, charset);

        // executes body of HTTP processor
        new BodyProcessor(elementDef).execute(scraper, context);

        HttpClientManager manager = scraper.getHttpClientManager();
        manager.setCookiePolicy(cookiePolicy);

        scraper.getLogger().info("Executing method {}...", method);

        final HttpResponseWrapper res = manager.execute(
                method, followRedirects, isMultipart, encodedUrl, charset, username, password,
                httpParams, httpHeaderMap, retryAttempts, retryDelay, retryDelayFactor);

        final long declaredContentLength = res.getContentLength();
        final long actualContentLength;

        scraper.removeRunningHttpProcessor();

        Variable result;

        if (skipResponseBody) {
            scraper.getLogger().info("Skipping response ({} bytes)", declaredContentLength);
            res.skipBody();
            result = EmptyVariable.INSTANCE;
            actualContentLength = 0;

        } else {
            scraper.getLogger().info("Getting response ({} bytes)...", declaredContentLength);

            final byte[] responseBody = res.readBodyAsArray();

            final String mimeType = StringUtils.lowerCase(res.getMimeType());

            actualContentLength = responseBody.length;

            if (scraper.getLogger().isInfoEnabled()) {
                scraper.getLogger().info("Downloaded: " + url + ", mime type = " + mimeType + ", length = " + actualContentLength + "B.");
            }

            if (mimeType != null && !isTextMimeType(mimeType)) {
                result = new NodeVariable(responseBody);
            } else {
                try {
                    // resolves charset in the following way:
                    //    1. if explicitly defined as charset attribute in http processor, then use it
                    //    2. if it is HTML document, reads first KB from response's body as ASCII stream
                    //       and tries to find meta tag with specified charset
                    //    3. use charset from response's header
                    //    4. uses default charset for the configuration
                    if (specifiedCharset == null) {
                        final String responseCharset = res.getCharset();
                        if (responseCharset != null && Charset.isSupported(responseCharset)) {
                            charset = responseCharset;
                        }
                        if ("text/html".equals(mimeType)) {
                            String firstBodyKb = new String(responseBody, 0, Math.min(responseBody.length, 1024), "ASCII");
                            Matcher matcher = Pattern.compile(HTML_META_CHARSET_REGEX, Pattern.CASE_INSENSITIVE).matcher(firstBodyKb);
                            if (matcher.find()) {
                                String foundCharset = matcher.group(5);
                                try {
                                    if (Charset.isSupported(foundCharset)) {
                                        charset = foundCharset;
                                    }
                                } catch (IllegalCharsetNameException e) {
                                    // do nothing - charset will not be set here
                                }
                            }
                        }
                    }
                    result = new NodeVariable(new String(responseBody, charset));

                } catch (UnsupportedEncodingException e) {
                    throw new HttpException("Charset " + charset + " is not supported!", e);
                }
            }
        }

        this.setProperty("URL", url);
        this.setProperty("Method", method);
        this.setProperty("Follow-Redirects", followRedirects);
        this.setProperty("Multipart", String.valueOf(isMultipart));
        this.setProperty("Status code", res.getStatusCode());
        this.setProperty("Status text", res.getStatusText());
        this.setProperty("Skip Response Body", skipResponseBody);
        this.setProperty("Declared Content length", String.valueOf(declaredContentLength));
        if (!skipResponseBody) {
            this.setProperty("Received Content length", String.valueOf(actualContentLength));
            this.setProperty("Charset", charset);
        }

        KeyValuePair<String>[] headerPairs = res.getHeaders();
        if (headerPairs != null) {
            int index = 1;
            for (KeyValuePair<String> pair : headerPairs) {
                this.setProperty("HTTP header [" + index + "]: " + pair.getKey(), pair.getValue());
                index++;
            }
        }

        return result;
    }

    private boolean isTextMimeType(String mimeType) {
        // todo: it's a temporary fix. Think better about handling mime-types.
        return mimeType.startsWith("text/")
                || mimeType.endsWith("/xml")
                || mimeType.contains("javascript");
    }

    protected void addHttpParam(String name, boolean isFile, String fileName, String contentType, Variable value) {
        httpParams.put(name, new HttpParamInfo(name, isFile, fileName, contentType, value));
    }

    protected void addHttpHeader(String name, String value) {
        httpHeaderMap.put(name, value);
    }

}