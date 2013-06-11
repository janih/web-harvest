package org.webharvest.runtime.processors.plugins.webbrowser;

import org.apache.commons.codec.binary.Base64;
import org.webharvest.annotation.Definition;
import org.webharvest.runtime.DynamicScopeContext;
import org.webharvest.runtime.processors.WebHarvestPlugin;
import org.webharvest.runtime.processors.plugins.Autoscanned;
import org.webharvest.runtime.processors.plugins.TargetNamespace;
import org.webharvest.runtime.variables.NodeVariable;
import org.webharvest.runtime.variables.Variable;
import org.webharvest.utils.CommonUtil;

import static org.webharvest.WHConstants.XMLNS_CORE;
import static org.webharvest.WHConstants.XMLNS_CORE_10;

/**
 * Evaluates javascript on the page inside headless web browser.
 */
@Autoscanned
@TargetNamespace({ XMLNS_CORE, XMLNS_CORE_10 })
@Definition(value = "web-browser-render-to-image", validAttributes = {"type"})
public class WebBrowserRenderToImagePlugin extends WebHarvestPlugin {

    public Variable executePlugin(DynamicScopeContext context) throws InterruptedException {
        WebBrowserPlugin webBrowserPlugin = WebBrowserPlugin.findParentPlugin(this);
        if (webBrowserPlugin != null) {
            String type = evaluateAttribute("type", context);
            if ( CommonUtil.isEmptyString(type) || (!"png".equalsIgnoreCase(type) && !"gif".equalsIgnoreCase(type) && !"jpeg".equalsIgnoreCase(type)) ) {
                type = "JPEG";
            }
            String urlContent = webBrowserPlugin.renderToImage(type.toUpperCase());
            return new NodeVariable(urlContent != null ? Base64.decodeBase64(urlContent) : "");
        } else {
            throw new WebBrowserlPluginException("Plugin 'web-browser-render-to-image' must be inside 'web-browser' execution context");
        }
    }

}