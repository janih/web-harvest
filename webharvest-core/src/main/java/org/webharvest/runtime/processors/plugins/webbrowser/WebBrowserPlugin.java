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
import java.net.URLEncoder;

import static org.webharvest.WHConstants.XMLNS_CORE;
import static org.webharvest.WHConstants.XMLNS_CORE_10;

/**
 * Support headless web browser supported by PhantomJS open source project.
 */
@Autoscanned
@TargetNamespace({ XMLNS_CORE, XMLNS_CORE_10 })
@Definition(value = "web-browser", validAttributes = {"path", "port", "width", "height", "paperformat", "paperorientation", "paperborder"})
public class WebBrowserPlugin extends WebHarvestPlugin {

    public static final long TIME_TO_LAUNCH_WEB_SERVER = 1000L;

    private static final int DEFAULT_PORT = 8081;
    // todo: this default name should depend on OS
    private static final String HEADLESS_BROWSER_FILENAME = "phantomjs.exe";

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

    @Inject
    @WorkingDir
    private String workingDir;


    public Variable executePlugin(DynamicScopeContext context) throws InterruptedException {
        String browserExecutablePath = evaluateAttribute("path", context);
        if (CommonUtil.isEmpty(browserExecutablePath)) {
            browserExecutablePath = CommonUtil.getAbsoluteFilename(workingDir, HEADLESS_BROWSER_FILENAME);
        }

        browserPort = evaluateAttributeAsInteger("port", DEFAULT_PORT, context);
        String pageW = evaluateAttribute("width", context);
        if (CommonUtil.isEmptyString(pageW)) {
            pageW = "1280";
        }
        String pageH = evaluateAttribute("height", context);
        if (CommonUtil.isEmptyString(pageH)) {
            pageH = "768";
        }
        String pageFormat = evaluateAttribute("paperformat", context);
        if (CommonUtil.isEmptyString(pageFormat)) {
            pageFormat = "A4";
        }
        String pageOrientation = evaluateAttribute("paperorientation", context);
        if (CommonUtil.isEmptyString(pageOrientation)) {
            pageOrientation = "portrait";
        }
        String pageBorder = evaluateAttribute("paperborder", context);
        if (CommonUtil.isEmptyString(pageBorder)) {
            pageBorder = "0";
        }

        Process process = null;
        try {
            String template = getPhantomTemplateAsString();
            template = template.replaceAll("\\$\\{PORT\\}", String.valueOf(browserPort));
            template = template.replaceAll("\\$\\{WIDTH\\}", String.valueOf(pageW));
            template = template.replaceAll("\\$\\{HEIGHT\\}", String.valueOf(pageH));
            template = template.replaceAll("\\$\\{FORMAT\\}", String.valueOf(pageFormat));
            template = template.replaceAll("\\$\\{ORIENTATION\\}", String.valueOf(pageOrientation));
            template = template.replaceAll("\\$\\{BORDER\\}", String.valueOf(pageBorder));

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
                    b.append("&").append(encode(pair.getKey())).append("=").append(encode(pair.getValue()));
                }
            }
            return CommonUtil.readStringFromUrl(b.toString(), true);
        } catch (Exception e) {
            throw new WebBrowserlPluginException(e);
        }
    }

    String loadUrl(String url, String pageContent) {
        if (CommonUtil.isEmptyString(url)) {
            url = "";
        }
        if (CommonUtil.isEmptyString(pageContent)) {
            pageContent = "";
        }
        return sendActionRequest("load", new KeyValuePair<String>("url", url), new KeyValuePair<String>("content", pageContent));
    }

    String evaluateOnPage(String expression, boolean isUrlChange) {
        return sendActionRequest( "eval", new KeyValuePair<String>("exp", expression), new KeyValuePair<String>("urlchange", String.valueOf(isUrlChange)) );
    }

    String includeJSOnPage(String jsPath) {
        return sendActionRequest("includejs", new KeyValuePair<String>("path", jsPath));
    }

    String renderToImage(String type) {
        return sendActionRequest("rendertoimage", new KeyValuePair<String>("type", type));
    }

    String renderToPdf(String path) {
        path = CommonUtil.getAbsoluteFilename(workingDir, path);
        return sendActionRequest("rendertopdf", new KeyValuePair<String>("path", path));
    }

}