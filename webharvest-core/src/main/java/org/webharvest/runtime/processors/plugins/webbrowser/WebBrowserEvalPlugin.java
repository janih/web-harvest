package org.webharvest.runtime.processors.plugins.webbrowser;

import org.webharvest.annotation.Definition;
import org.webharvest.runtime.DynamicScopeContext;
import org.webharvest.runtime.processors.WebHarvestPlugin;
import org.webharvest.runtime.processors.plugins.Autoscanned;
import org.webharvest.runtime.processors.plugins.TargetNamespace;
import org.webharvest.runtime.variables.NodeVariable;
import org.webharvest.runtime.variables.Variable;

import static org.webharvest.WHConstants.XMLNS_CORE;
import static org.webharvest.WHConstants.XMLNS_CORE_10;

/**
 * Evaluates javascript on the page inside headless web browser.
 */
@Autoscanned
@TargetNamespace({ XMLNS_CORE, XMLNS_CORE_10 })
@Definition(value = "web-browser-eval", validAttributes = {"urlchange"})
public class WebBrowserEvalPlugin extends WebHarvestPlugin {

    public Variable executePlugin(DynamicScopeContext context) throws InterruptedException {
        WebBrowserPlugin webBrowserPlugin = WebBrowserPlugin.findParentPlugin(this);
        if (webBrowserPlugin != null) {
            boolean isUrlChange = evaluateAttributeAsBoolean("urlchange", false, context);
            Variable body = executeBody(context);
            String jsExpression = body.toString();
            String urlContent = webBrowserPlugin.evaluateOnPage(jsExpression, isUrlChange);
            return new NodeVariable(urlContent);
        } else {
            throw new WebBrowserlPluginException("Plugin 'web-browser-eval' must be inside 'web-browser' execution context");
        }
    }

    public String[] getAttributeValueSuggestions(String attributeName) {
        if ("urlchange".equalsIgnoreCase(attributeName)) {
            return new String[] {"true", "false"};
        }
        return null;
    }

}