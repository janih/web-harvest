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
import java.io.FileInputStream;
import java.io.IOException;

import static org.webharvest.WHConstants.XMLNS_CORE;
import static org.webharvest.WHConstants.XMLNS_CORE_10;

/**
 * Evaluates javascript on the page inside headless web browser.
 */
@Autoscanned
@TargetNamespace({ XMLNS_CORE, XMLNS_CORE_10 })
@Definition(value = "web-browser-render-to-pdf", validAttributes = {})
public class WebBrowserRenderToPdfPlugin extends WebHarvestPlugin {

    private static final String TEMP_PDF_FILE_NAME = "__temp__.pdf";

    @Inject
    @WorkingDir
    private String workingDir;

    public Variable executePlugin(DynamicScopeContext context) throws InterruptedException {
        WebBrowserPlugin webBrowserPlugin = WebBrowserPlugin.findParentPlugin(this);
        if (webBrowserPlugin != null) {
            String path = CommonUtil.getAbsoluteFilename(workingDir, TEMP_PDF_FILE_NAME);
            String urlContent = webBrowserPlugin.renderToPdf(path);
            if (CommonUtil.isEmptyString(urlContent)) {
                File file = new File(path);
                try {
                    byte[] data = CommonUtil.readBytesFromFile(file);
                    return new NodeVariable(data);
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
                return new NodeVariable(urlContent);
            }
        } else {
            throw new WebBrowserlPluginException("Plugin 'web-browser-render-to-pdf' must be inside 'web-browser' execution context");
        }
    }

}