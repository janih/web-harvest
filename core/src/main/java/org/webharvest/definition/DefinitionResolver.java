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
package org.webharvest.definition;

import org.webharvest.exception.ConfigurationException;
import org.webharvest.exception.ErrMsg;
import org.webharvest.exception.PluginException;
import org.webharvest.runtime.processors.WebHarvestPlugin;
import org.webharvest.runtime.processors.plugins.*;
import org.webharvest.runtime.processors.plugins.db.DatabasePlugin;
import org.webharvest.runtime.processors.plugins.ftp.FtpPlugin;
import org.webharvest.runtime.processors.plugins.mail.MailPlugin;
import org.webharvest.runtime.processors.plugins.variable.DefVarPlugin;
import org.webharvest.runtime.processors.plugins.variable.GetVarPlugin;
import org.webharvest.runtime.processors.plugins.variable.SetVarPlugin;
import org.webharvest.runtime.processors.plugins.zip.ZipPlugin;
import org.webharvest.utils.Assert;
import org.webharvest.utils.ClassLoaderUtil;
import org.webharvest.utils.CommonUtil;
import org.webharvest.utils.Constants;

import java.lang.reflect.InvocationTargetException;
import java.util.*;

/**
 * Class contains information and logic to validate and crate definition classes for
 * parsed xml nodes from Web-Harvest configurations.
 *
 * @author Vladimir Nikic
 */
@SuppressWarnings({"UnusedDeclaration"})
public class DefinitionResolver {

    private static Map<ElementName, ElementInfo> elementInfos = new TreeMap<ElementName, ElementInfo>();

    // map containing pairs (class name, plugin element name) of externally registered plugins
    private static Map<String, ElementName> externalPlugins = new LinkedHashMap<String, ElementName>();

    // map of external plugin dependances
    private static Map<ElementName, Class[]> externalPluginDependences = new HashMap<ElementName, Class[]>();

    // defines all valid elements of Web-Harvest configuration file

    static {
        registerInternalElement("config", new ElementInfo("config", BaseElementDef.class, null, "charset,scriptlang,id"));
        registerInternalElement("empty", new ElementInfo("empty", EmptyDef.class, null, "id"));
        registerInternalElement("text", new ElementInfo("text", TextDef.class, null, "id,charset,delimiter"));
        registerInternalElement("file", new ElementInfo("file", FileDef.class, null, "id,!path,action,type,charset,listfilter,listfiles,listdirs,listrecursive"));
        registerInternalElement("var-def", new ElementInfo("var-def", VarDefDef.class, null, "id,!name,overwrite"));
        registerInternalElement("var", new ElementInfo("var", VarDef.class, "", "id,!name"));
        registerInternalElement("http", new ElementInfo("http", HttpDef.class, null, "id,!url,method,follow-redirects,retry-attempts,retry-delay,retry-delay-factor,multipart,charset,username,password,cookie-policy"));
        registerInternalElement("http-param", new ElementInfo("http-param", HttpParamDef.class, null, "id,!name,isfile,filename,contenttype"));
        registerInternalElement("http-header", new ElementInfo("http-header", HttpHeaderDef.class, null, "id,!name"));
        registerInternalElement("html-to-xml", new ElementInfo("html-to-xml", HtmlToXmlDef.class, null, "" +
                "id,outputtype,advancedxmlescape,usecdata,specialentities,unicodechars,nbsp-to-sp," +
                "omitunknowntags,treatunknowntagsascontent,omitdeprtags,treatdeprtagsascontent," +
                "omitxmldecl,omitcomments,omithtmlenvelope,useemptyelementtags,allowmultiwordattributes," +
                "allowhtmlinsideattributes,namespacesaware,hyphenreplacement,prunetags,booleanatts"));
        registerInternalElement("regexp", new ElementInfo("regexp", RegexpDef.class, "!regexp-pattern,!regexp-source,regexp-result", "id,replace,max,flag-caseinsensitive,flag-multiline,flag-dotall,flag-unicodecase,flag-canoneq"));
        registerInternalElement("regexp-pattern", new ElementInfo("regexp-pattern", BaseElementDef.class, null, "id"));
        registerInternalElement("regexp-source", new ElementInfo("regexp-source", BaseElementDef.class, null, "id"));
        registerInternalElement("regexp-result", new ElementInfo("regexp-result", BaseElementDef.class, null, "id"));
        registerInternalElement("xpath", new ElementInfo("xpath", XPathDef.class, null, "id,expression,v:*"));
        registerInternalElement("xquery", new ElementInfo("xquery", XQueryDef.class, "xq-param,!xq-expression", "id"));
        registerInternalElement("xq-param", new ElementInfo("xq-param", BaseElementDef.class, null, "!name,type,id"));
        registerInternalElement("xq-expression", new ElementInfo("xq-expression", BaseElementDef.class, null, "id"));
        registerInternalElement("xslt", new ElementInfo("xslt", XsltDef.class, "!xml,!stylesheet", "id"));
        registerInternalElement("xml", new ElementInfo("xml", BaseElementDef.class, null, "id"));
        registerInternalElement("stylesheet", new ElementInfo("stylesheet", BaseElementDef.class, null, "id"));
        registerInternalElement("template", new ElementInfo("template", TemplateDef.class, null, "id,language"));
        registerInternalElement("case", new ElementInfo("case", CaseDef.class, "!if,else", "id"));
        registerInternalElement("if", new ElementInfo("if", BaseElementDef.class, null, "!condition,id"));
        registerInternalElement("else", new ElementInfo("else", BaseElementDef.class, null, "id"));
        registerInternalElement("loop", new ElementInfo("loop", LoopDef.class, "!list,!body", "id,item,index,maxloops,filter,empty"));
        registerInternalElement("list", new ElementInfo("list", BaseElementDef.class, null, "id"));
        registerInternalElement("body", new ElementInfo("body", BaseElementDef.class, null, "id"));
        registerInternalElement("while", new ElementInfo("while", WhileDef.class, null, "id,!condition,index,maxloops,empty"));
        registerInternalElement("function", new ElementInfo("function", FunctionDef.class, null, "id,!name"));
        registerInternalElement("return", new ElementInfo("return", ReturnDef.class, null, "id"));
        registerInternalElement("call", new ElementInfo("call", CallDef.class, null, "id,!name"));
        registerInternalElement("call-param", new ElementInfo("call-param", CallParamDef.class, null, "id,!name"));
        registerInternalElement("include", new ElementInfo("include", IncludeDef.class, "", "id,!path"));
        registerInternalElement("try", new ElementInfo("try", TryDef.class, "!body,!catch", "id"));
        registerInternalElement("catch", new ElementInfo("catch", BaseElementDef.class, null, "id"));
        registerInternalElement("script", new ElementInfo("script", ScriptDef.class, null, "id,language,return"));
        registerInternalElement("exit", new ElementInfo("exit", ExitDef.class, "", "id,condition,message"));

        registerPlugin(SetVarPlugin.class, true, Constants.XMLNS_CORE);
        registerPlugin(DefVarPlugin.class, true, Constants.XMLNS_CORE);
        registerPlugin(GetVarPlugin.class, true, Constants.XMLNS_CORE);
        registerPlugin(ValueOfPlugin.class, true, Constants.XMLNS_CORE);
        registerPlugin(DatabasePlugin.class, true, Constants.XMLNS_CORE);
        registerPlugin(JsonToXmlPlugin.class, true, Constants.XMLNS_CORE);
        registerPlugin(XmlToJsonPlugin.class, true, Constants.XMLNS_CORE);
        registerPlugin(MailPlugin.class, true, Constants.XMLNS_CORE);
        registerPlugin(ZipPlugin.class, true, Constants.XMLNS_CORE);
        registerPlugin(FtpPlugin.class, true, Constants.XMLNS_CORE);
        registerPlugin(TokenizePlugin.class, true, Constants.XMLNS_CORE);
        registerPlugin(SleepPlugin.class, true, Constants.XMLNS_CORE);
    }

    /**
     * Register core element.
     * @param name Name of the element corresponding to the tag name in xml configuration
     * @param elementInfo ElementInfo instance of the element
     */
    private static void registerInternalElement(String name, ElementInfo elementInfo) {
        elementInfos.put(new ElementName(name), elementInfo);
    }

    private static void registerPlugin(Class pluginClass, boolean isInternalPlugin, String uri) {
        Assert.notNull(pluginClass);
        try {
            final Object pluginObj = pluginClass.newInstance();
            if (!(pluginObj instanceof WebHarvestPlugin)) {
                throw new PluginException("Plugin class \"" + pluginClass.getName() + "\" does not extend WebHarvestPlugin class!");
            }
            final WebHarvestPlugin plugin = (WebHarvestPlugin) pluginObj;
            String pluginName = plugin.getName();
            if (!CommonUtil.isValidXmlIdentifier(pluginName)) {
                throw new PluginException("Plugin class \"" + pluginClass.getName() + "\" does not define valid name!");
            }

            ElementName pluginElementName = new ElementName(pluginName, uri);

            if (elementInfos.containsKey(pluginElementName)) {
                throw new PluginException("Plugin \"" + pluginElementName + "\" is already registered!");
            }

            final ElementInfo elementInfo = new ElementInfo(
                    pluginName,
                    pluginClass,
                    isInternalPlugin,
                    WebHarvestPluginDef.class,
                    plugin.getTagDesc(),
                    plugin.getAttributeDesc());

            elementInfo.setPlugin(plugin);
            elementInfos.put(pluginElementName, elementInfo);
            if (!isInternalPlugin) {
                externalPlugins.put(pluginClass.getName(), pluginElementName);
            }
            externalPluginDependences.put(pluginElementName, plugin.getDependantProcessors());

            for (Class subClass : plugin.getDependantProcessors()) {
                registerPlugin(subClass, isInternalPlugin, uri);
            }
        } catch (InstantiationException e) {
            throw new PluginException("Error instantiating plugin class \"" + pluginClass.getName() + "\": " + e.getMessage(), e);
        } catch (IllegalAccessException e) {
            throw new PluginException("Error instantiating plugin class \"" + pluginClass.getName() + "\": " + e.getMessage(), e);
        }
    }

    public static void registerPlugin(Class pluginClass) throws PluginException {
        registerPlugin(pluginClass, false, Constants.XMLNS_CORE);
    }

    public static void registerPlugin(Class pluginClass, String uri) throws PluginException {
        registerPlugin(pluginClass, false, uri);
    }

    public static void registerPlugin(String fullClassName) throws PluginException {
        registerPlugin(fullClassName, Constants.XMLNS_CORE);
    }

    public static void registerPlugin(String fullClassName, String uri) throws PluginException {
        Class pluginClass = ClassLoaderUtil.getPluginClass(fullClassName);
        registerPlugin(pluginClass, false, uri);
    }

    public static void unregisterPlugin(Class pluginClass) {
        if (pluginClass != null) {
            unregisterPlugin(pluginClass.getName());
        }
    }

    public static void unregisterPlugin(String className) {
        // only external plugins can be unregistered
        if (isPluginRegistered(className)) {
            ElementName pluginElementName = externalPlugins.get(className);
            elementInfos.remove(pluginElementName);
            externalPlugins.remove(className);

            // unregister deependant classes as well
            Class[] dependantClasses = externalPluginDependences.get(pluginElementName);
            externalPluginDependences.remove(pluginElementName);
            if (dependantClasses != null) {
                for (Class c : dependantClasses) {
                    unregisterPlugin(c);
                }
            }
        }
    }

    public static boolean isPluginRegistered(String className) {
        return externalPlugins.containsKey(className);
    }

    public static boolean isPluginRegistered(Class pluginClass) {
        return pluginClass != null && isPluginRegistered(pluginClass.getName());
    }

    public static Map<String, ElementName> getExternalPlugins() {
        return externalPlugins;
    }

    /**
     * @return Map of all allowed element infos.
     */
    public static Map<ElementName, ElementInfo> getElementInfos() {
        return elementInfos;
    }

    /**
     * @param name Name of the element
     * @param uri URI of the element
     * @return Instance of ElementInfo class for the specified element name,
     *         or null if no element is defined.
     */
    public static ElementInfo getElementInfo(String name, String uri) {
        return elementInfos.get(new ElementName(name, uri));
    }

    /**
     * Creates proper element definition instance based on given xml node
     * from input configuration.
     *
     * @param node
     * @return Instance of IElementDef, or exception is thrown if cannot find
     *         appropriate element definition.
     */
    public static IElementDef createElementDefinition(XmlNode node) {
        final String nodeName = node.getName();
        final String nodeUri = node.getUri();

        final ElementInfo elementInfo = getElementInfo(nodeName, nodeUri);
        if (elementInfo == null || elementInfo.getDefinitionClass() == null || elementInfo.getDefinitionClass() == BaseElementDef.class) {
            throw new ConfigurationException("Unexpected configuration element: " + node.getQName() + "!");
        }

        validate(node);

        Class elementClass = elementInfo.getDefinitionClass();
        try {
            final IElementDef elementDef = (IElementDef) elementClass.getConstructor(new Class[]{XmlNode.class}).newInstance(node);
            if (elementDef instanceof WebHarvestPluginDef) {
                WebHarvestPluginDef pluginDef = (WebHarvestPluginDef) elementDef;
                pluginDef.setPluginClass(elementInfo.getPluginClass());
                pluginDef.setPluginName(elementInfo.getName());
            }
            return elementDef;
        } catch (NoSuchMethodException e) {
            throw new ConfigurationException("Cannot create class instance: " + elementClass + "!", e);
        } catch (InvocationTargetException e) {
            final Throwable cause = e.getCause();
            if (cause instanceof ConfigurationException) {
                throw (ConfigurationException)cause;
            }
            throw new ConfigurationException("Cannot create class instance: " + elementClass + "!", e);
        } catch (InstantiationException e) {
            throw new ConfigurationException("Cannot create class instance: " + elementClass + "!", e);
        } catch (IllegalAccessException e) {
            throw new ConfigurationException("Cannot create class instance: " + elementClass + "!", e);
        }
    }

    /**
     * Validates specified xml node with appropriate element info instance.
     * If validation fails, an runtime exception is thrown.
     *
     * @param node
     */
    public static void validate(XmlNode node) {
        if (node == null) {
            return;
        }

        final String uri = node.getUri();

        final ElementInfo elementInfo = getElementInfo(node.getName(), uri);

        if (elementInfo == null) {
            return;
        }

        // checks if tag contains all required subelements
        for (String tag : elementInfo.getRequiredTagsSet()) {
            if (node.getElement(tag) == null) {
                throw new ConfigurationException(ErrMsg.missingTag(node.getName(), tag));
            }
        }

        final boolean areAllTagsAllowed = elementInfo.areAllTagsAllowed();
        final Set<ElementName> allTagNameSet = elementInfos.keySet();
        final Set<String> tags = elementInfo.getTagsSet();

        // check if element contains only allowed subelements
        for (ElementName elementName : node.getElementNameSet()) {
            if ( (!areAllTagsAllowed && (!tags.contains(elementName.getName()) || !uri.equals(node.getUri())) ) ||
                 (areAllTagsAllowed && !allTagNameSet.contains(elementName))
               ) {
                throw new ConfigurationException(ErrMsg.invalidTag(node.getName(), elementName.toString()));
            }
        }

        // checks if tag contains all required attributes
        for (String att : elementInfo.getRequiredAttsSet()) {
            if (node.getAttribute(uri, att) == null) {
                throw new ConfigurationException(ErrMsg.missingAttribute(node.getName(), att));
            }
        }

        final Set<String> atts = elementInfo.getAttsSet();

        // check if element contains only allowed attributes
        for (XmlAttribute att : node.getAllAttributes()) {
            String attUri = att.getUri();
            String attName = att.getName();
            if ( !atts.contains(attName) || !uri.equals(attUri) ) {
                if ( !elementInfo.getNsAttsSet().contains(attUri) ) {
                    throw new ConfigurationException(ErrMsg.invalidAttribute(node.getName(), attName));
                }
            }
        }
    }

}