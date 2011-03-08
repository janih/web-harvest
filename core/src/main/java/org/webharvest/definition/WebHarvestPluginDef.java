package org.webharvest.definition;

import org.webharvest.exception.PluginException;
import org.webharvest.runtime.processors.WebHarvestPlugin;
import org.webharvest.utils.*;

import java.util.Map;

/**
 * Definition of all plugin processors.
 */
public class WebHarvestPluginDef extends BaseElementDef {

    private XmlNode xmlNode;
    private Class pluginClass;
    private String name;

    public WebHarvestPluginDef(XmlNode xmlNode) {
        super(xmlNode, true);
        this.xmlNode = xmlNode;
    }

    void setPluginClass(Class pluginClass) {
        this.pluginClass = pluginClass;
    }

    void setPluginName(String name) {
        this.name = name;
    }

    public String getUri() {
        return xmlNode.getUri();
    }

    public Map<String, String> getAttributes() {
        return getAttributes(xmlNode.getUri());
    }

    public Map<String, String> getAttributes(String uri) {
        return xmlNode.getAttributes(uri);
    }

    public WebHarvestPlugin createPlugin() {
        if (pluginClass != null) {
            try {
                WebHarvestPlugin plugin = (WebHarvestPlugin) pluginClass.newInstance();
                plugin.setDef(this);
                return plugin;
            } catch (InstantiationException e) {
                throw new PluginException(e);
            } catch (IllegalAccessException e) {
                throw new PluginException(e);
            }
        }

        throw new PluginException("Cannot create plugin!");
    }

    public String getShortElementName() {
        return name != null ? name.toLowerCase() : "unknown plugin";
    }

}