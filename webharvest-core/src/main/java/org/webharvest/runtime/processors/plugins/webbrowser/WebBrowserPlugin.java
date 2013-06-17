package org.webharvest.runtime.processors.plugins.webbrowser;

import com.google.inject.Inject;
import org.webharvest.annotation.Definition;
import org.webharvest.ioc.WorkingDir;
import org.webharvest.runtime.DynamicScopeContext;
import org.webharvest.runtime.processors.AbstractProcessor;
import org.webharvest.runtime.processors.Processor;
import org.webharvest.runtime.processors.WebHarvestPlugin;
import org.webharvest.runtime.processors.plugins.Autoscanned;
import org.webharvest.runtime.processors.plugins.TargetNamespace;
import org.webharvest.runtime.variables.Variable;
import org.webharvest.utils.CommonUtil;
import org.webharvest.utils.KeyValuePair;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

import static org.webharvest.WHConstants.XMLNS_CORE;
import static org.webharvest.WHConstants.XMLNS_CORE_10;

/**
 * Support headless web browser supported by PhantomJS open source project.
 */
@Autoscanned
@TargetNamespace({ XMLNS_CORE, XMLNS_CORE_10 })
@Definition(value = "web-browser", validAttributes = {"path", "port"})
public class WebBrowserPlugin extends WebHarvestPlugin {

    public static final long TIME_TO_LAUNCH_WEB_SERVER = 1000L;

    private static final int DEFAULT_PORT = 8081;
    // todo: this default name should depend on OS
    private static final String HEADLESS_BROWSER_FILENAME = "phantomjs";

    public static synchronized String getPhantomTemplateAsString() throws IOException {
        if (phantomTemplate == null) {
            InputStream in = WebHarvestPlugin.class.getResourceAsStream("/org/webharvest/runtime/processors/plugins/webbrowser/phantomjs_template.js");
            LineNumberReader reader = new LineNumberReader(new InputStreamReader(in));
            int ch;
            StringBuilder result = new StringBuilder();
            while ((ch = reader.read()) != -1) {
                result.append((char) ch);
            }
            phantomTemplate = result.toString();
        }
        return phantomTemplate;
    }

    public static WebBrowserPlugin findParentPlugin(AbstractProcessor childProcessor) {
        Processor p = childProcessor.getParentProcessor();
        do {
            if (p instanceof WebBrowserPlugin) {
                return (WebBrowserPlugin)p;
            } else {
                p = p.getParentProcessor();
            }
        } while (p != null);
        return null;
    }


    private static String phantomTemplate = null;

    private int browserPort;

    private class HttpActionWrapper {
        private int responseCode = 200;
        private String content = null;

        private HttpActionWrapper(String urlString) throws IOException {
            String params = null;
            // get parameters after ?
            final int index = urlString.indexOf("?");
            if (index > 0) {
                if (index < urlString.length() - 1) {
                    params = urlString.substring(index + 1);
                }
                urlString = urlString.substring(0, index);
            }
            URL u = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) u.openConnection();
            conn.setDoOutput(true);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            conn.setRequestProperty("Content-Length", String.valueOf(params != null ? params.length() : 0));
            DataOutputStream os = new DataOutputStream(conn.getOutputStream());
            if (params != null) {
                os.writeBytes(params);
            }
            os.flush();
            os.close();

            InputStream in = conn.getInputStream();

            this.responseCode = conn.getResponseCode();

            StringBuilder buffer = new StringBuilder();
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            int ch;
            while ((ch = reader.read()) != -1) {
                buffer.append((char) ch);
            }
            reader.close();

            this.content = buffer.toString();
        }

        private boolean isOkResponse() {
            return this.responseCode == 200;
        }
    }

    @Inject
    @WorkingDir
    private String workingDir;


    public Variable executePlugin(DynamicScopeContext context) throws InterruptedException {
        String browserExecutablePath = evaluateAttribute("path", context);
        if (CommonUtil.isEmpty(browserExecutablePath)) {
            browserExecutablePath = CommonUtil.getAbsoluteFilename(workingDir, HEADLESS_BROWSER_FILENAME);
        }

        browserPort = evaluateAttributeAsInteger("port", DEFAULT_PORT, context);
        Process process = null;
        try {
            String template = getPhantomTemplateAsString();
            template = template.replaceAll("\\$\\{PORT\\}", String.valueOf(browserPort));

            String jsFileName = workingDir + "/phantom_input.js";
            CommonUtil.saveStringToFile(new File(jsFileName), template, "utf-8");

            ProcessBuilder pb = new ProcessBuilder(browserExecutablePath, jsFileName);
            process = pb.start();
            Thread.sleep(TIME_TO_LAUNCH_WEB_SERVER);
            Variable body = executeBody(context);
            return body;
        } catch (IOException e) {
            throw new WebBrowserlPluginException(e);
        } finally {
            if (process != null) {
                try {
                    process.destroy();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private String encode(String s) {
        try {
            StringBuilder result = new StringBuilder();
            for (int i = 0; i < s.length(); i++) {
                char ch = s.charAt(i);
                if (ch == '+') {
                    result.append("%2B");
                } else {
                    result.append(URLEncoder.encode(String.valueOf(ch), "utf-8"));
                }
            }
            return result.toString();
        } catch (Exception e) {
            throw new WebBrowserlPluginException(e);
        }
    }

    String sendActionRequest(String action, KeyValuePair<String>... params) {
        try {
            StringBuilder b = new StringBuilder();
            b.append("http://localhost:").append(browserPort).append("/?action=").append(action);
            if (params != null) {
                for (KeyValuePair<String> pair: params) {
                    String key = pair.getKey();
                    String value = pair.getValue();
                    if (!CommonUtil.isEmptyString(key) && value != null) {
                        b.append("&").append(encode(key)).append("=").append(encode(value));
                    }
                }
            }
            HttpActionWrapper httpActionWrapper = new HttpActionWrapper(b.toString());
            if (httpActionWrapper.isOkResponse()) {
                return httpActionWrapper.content;
            } else {
                throw new WebBrowserlPluginException(httpActionWrapper.content);
            }
        } catch (Exception e) {
            throw new WebBrowserlPluginException(e);
        }
    }

    String loadUrl(String url, String pageName, String width, String height, String paperformat, String paperorientation, String paperborder,
                   String javascriptenabled, String loadimages, String useragent, String username, String password, String zoomfactor, String pageContent) {
        if (CommonUtil.isEmptyString(url)) {
            url = "";
        }
        if (CommonUtil.isEmptyString(pageContent)) {
            pageContent = "";
        }
        return sendActionRequest(
                "load",
                new KeyValuePair<String>("url", url),
                new KeyValuePair<String>("page", pageName),
                new KeyValuePair<String>("width", width),
                new KeyValuePair<String>("height", height),
                new KeyValuePair<String>("paperformat", paperformat),
                new KeyValuePair<String>("paperorientation", paperorientation),
                new KeyValuePair<String>("paperborder", paperborder),
                new KeyValuePair<String>("javascriptenabled", javascriptenabled),
                new KeyValuePair<String>("loadimages", loadimages),
                new KeyValuePair<String>("useragent", useragent),
                new KeyValuePair<String>("username", username),
                new KeyValuePair<String>("password", password),
                new KeyValuePair<String>("zoomfactor", zoomfactor),
                new KeyValuePair<String>("content", pageContent)
        );
    }

    String evaluateOnPage(String expression, boolean isUrlChange, String pageName) {
        return sendActionRequest(
                "eval",
                new KeyValuePair<String>("exp", expression),
                new KeyValuePair<String>("urlchange", String.valueOf(isUrlChange)),
                new KeyValuePair<String>("page", pageName)
        );
    }

    String renderToImage(String type, String pageName) {
        return sendActionRequest("rendertoimage", new KeyValuePair<String>("type", type), new KeyValuePair<String>("page", pageName));
    }

    String renderToPdf(String path, String pageName) {
        path = CommonUtil.getAbsoluteFilename(workingDir, path);
        return sendActionRequest("rendertopdf", new KeyValuePair<String>("page", pageName));
    }

}