package org.webharvest.runtime.processors.plugins.webbrowser;

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
 * Load page inside headless web browser.
 */
@Autoscanned
@TargetNamespace({ XMLNS_CORE, XMLNS_CORE_10 })
@Definition(value = "web-browser-load", validAttributes = {"url", "page"})
public class WebBrowserLoadPlugin extends WebHarvestPlugin {

    public Variable executePlugin(DynamicScopeContext context) throws InterruptedException {
        WebBrowserPlugin webBrowserPlugin = WebBrowserPlugin.findParentPlugin(this);
        if (webBrowserPlugin != null) {
            String url = evaluateAttribute("url", context);
            String pageName = evaluateAttribute("page", context);
            if (CommonUtil.isEmptyString(url)) {
                throw new WebBrowserlPluginException("Url must be non-empty!");
            }
            Variable content = executeBody(context);
            String urlContent = webBrowserPlugin.loadUrl(url, CommonUtil.nvl(pageName, ""), content == null ? null : content.toString());
            return new NodeVariable(urlContent);
        } else {
            throw new WebBrowserlPluginException("Plugin 'web-browser-load' must be inside 'web-browser' execution context");
        }
    }

    public String[] getValidAttributes() {
        return new String[]{};
    }

    public String[] getRequiredAttributes() {
        return new String[] {};
    }

    public String[] getAttributeValueSuggestions(String attributeName) {
        return null;
    }

}