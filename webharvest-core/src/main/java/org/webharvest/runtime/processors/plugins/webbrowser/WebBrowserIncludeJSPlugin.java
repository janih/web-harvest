package org.webharvest.runtime.processors.plugins.webbrowser;

import com.google.inject.Inject;
import org.webharvest.annotation.Definition;
import org.webharvest.ioc.WorkingDir;
import org.webharvest.runtime.DynamicScopeContext;
import org.webharvest.runtime.processors.WebHarvestPlugin;
import org.webharvest.runtime.processors.plugins.Autoscanned;
import org.webharvest.runtime.processors.plugins.TargetNamespace;
import org.webharvest.runtime.variables.NodeVariable;
import org.webharvest.runtime.variables.Variable;
import org.webharvest.utils.CommonUtil;

import java.io.File;
import java.io.IOException;

import static org.webharvest.WHConstants.XMLNS_CORE;
import static org.webharvest.WHConstants.XMLNS_CORE_10;

/**
 * Include external java script into the page.
 */
@Autoscanned
@TargetNamespace({ XMLNS_CORE, XMLNS_CORE_10 })
@Definition(value = "web-browser-includejs", validAttributes = {})
public class WebBrowserIncludeJSPlugin extends WebHarvestPlugin {

    private static final String TEMP_JS_FILE_NAME = "__temp__.js";

    @Inject
    @WorkingDir
    private String workingDir;

    public Variable executePlugin(DynamicScopeContext context) throws InterruptedException {
        WebBrowserPlugin webBrowserPlugin = WebBrowserPlugin.findParentPlugin(this);
        if (webBrowserPlugin != null) {
            Variable body = executeBody(context);
            String jsToLoad = body.toString();
            String path = CommonUtil.getAbsoluteFilename(workingDir, TEMP_JS_FILE_NAME);
            File file = new File(path);
            try {
                CommonUtil.saveStringToFile(file, jsToLoad, "UTF-8");
                String urlContent = webBrowserPlugin.includeJSOnPage(path);
                return new NodeVariable(urlContent);
            } catch (IOException e) {
                throw new WebBrowserlPluginException(e);
            } finally {
                try{
                    file.delete();
                } catch(Exception e) {
                    e.printStackTrace();
                }
            }

        } else {
            throw new WebBrowserlPluginException("Plugin 'web-browser-includejs' must be inside 'web-browser' execution context");
        }
    }

}