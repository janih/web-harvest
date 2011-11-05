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

import org.webharvest.deprecated.runtime.processors.CallProcessor10;
import org.webharvest.deprecated.runtime.processors.VarDefProcessor;
import org.webharvest.deprecated.runtime.processors.VarProcessor;
import org.webharvest.exception.ConfigurationException;
import org.webharvest.exception.ErrMsg;
import org.webharvest.exception.PluginException;
import org.webharvest.runtime.processors.*;
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

import java.lang.reflect.InvocationTargetException;
import java.util.*;

import static org.webharvest.WHConstants.XMLNS_CORE;
import static org.webharvest.WHConstants.XMLNS_CORE_10;

/**
 * Class contains information and logic to validate and crate definition classes for
 * parsed xml nodes from Web-Harvest configurations.
 *
 * @author Vladimir Nikic
 */
@SuppressWarnings({"UnusedDeclaration"})
public class DefinitionResolver {

    private final static class PluginClassKey {

        private PluginClassKey(String className, String uri) {
            this.className = className;
            this.uri = uri;
        }

        final String className;
        final String uri;

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            final PluginClassKey that = (PluginClassKey) o;
            return className.equals(that.className) && uri.equals(that.uri);

        }

        @Override
        public int hashCode() {
            return 31 * className.hashCode() + uri.hashCode();
        }
    }

    private static Map<ElementName, ElementInfo> elementInfos = new TreeMap<ElementName, ElementInfo>();

    // map containing pairs (class name, plugin element name) of externally registered plugins
    private static Map<PluginClassKey, ElementName> externalPlugins = new LinkedHashMap<PluginClassKey, ElementName>();

    // map of external plugin dependencies
    private static Map<ElementName, Class[]> externalPluginDependencies = new HashMap<ElementName, Class[]>();

    // defines all valid elements of Web-Harvest configuration file

    static {

        // register processors
        registerInternalElement("config", ProcessorElementDef.class, null, null, "charset,scriptlang,id",
                XMLNS_CORE_10, XMLNS_CORE);
        registerInternalElement("empty", EmptyDef.class, EmptyProcessor.class, null, "id",
                XMLNS_CORE_10, XMLNS_CORE);
        registerInternalElement("text", TextDef.class, TextProcessor.class, null, "id,charset,delimiter",
                XMLNS_CORE_10, XMLNS_CORE);
        registerInternalElement("file", FileDef.class, FileProcessor.class, null,
                "id,!path,action,type,charset,listfilter,listfiles,listdirs,listrecursive",
                XMLNS_CORE_10, XMLNS_CORE);
        registerInternalElement("http", HttpDef.class, HttpProcessor.class, null,
                "id,!url,method,follow-redirects,retry-attempts,retry-delay,retry-delay-factor,multipart,charset,username,password,cookie-policy",
                XMLNS_CORE_10, XMLNS_CORE);
        registerInternalElement("http-param", HttpParamDef.class, HttpParamProcessor.class, null, "id,!name,isfile,filename,contenttype",
                XMLNS_CORE_10, XMLNS_CORE);
        registerInternalElement("http-header", HttpHeaderDef.class, HttpHeaderProcessor.class, null, "id,!name",
                XMLNS_CORE_10, XMLNS_CORE);
        registerInternalElement("html-to-xml", HtmlToXmlDef.class, HtmlToXmlProcessor.class, null, "" +
                "id,outputtype,advancedxmlescape,usecdata,specialentities,unicodechars,nbsp-to-sp," +
                "omitunknowntags,treatunknowntagsascontent,omitdeprtags,treatdeprtagsascontent," +
                "omitxmldecl,omitcomments,omithtmlenvelope,useemptyelementtags,allowmultiwordattributes," +
                "allowhtmlinsideattributes,namespacesaware,hyphenreplacement,prunetags,booleanatts",
                XMLNS_CORE_10, XMLNS_CORE);
        registerInternalElement("regexp", RegexpDef.class, RegexpProcessor.class,
                "!regexp-pattern,!regexp-source,regexp-result", "id,replace,max,flag-caseinsensitive,flag-multiline,flag-dotall,flag-unicodecase,flag-canoneq",
                XMLNS_CORE_10, XMLNS_CORE);
        registerInternalElement("regexp-pattern", ProcessorElementDef.class, null, null, "id",
                XMLNS_CORE_10, XMLNS_CORE);
        registerInternalElement("regexp-source", ProcessorElementDef.class, null, null, "id",
                XMLNS_CORE_10, XMLNS_CORE);
        registerInternalElement("regexp-result", ProcessorElementDef.class, null, null, "id",
                XMLNS_CORE_10, XMLNS_CORE);
        registerInternalElement("xpath", XPathDef.class, XPathProcessor.class, null, "id,expression,v:*",
                XMLNS_CORE_10, XMLNS_CORE);
        registerInternalElement("xquery", XQueryDef.class, XQueryProcessor.class, "xq-param,!xq-expression", "id",
                XMLNS_CORE_10, XMLNS_CORE);
        registerInternalElement("xq-param", ProcessorElementDef.class, null, null, "!name,type,id",
                XMLNS_CORE_10, XMLNS_CORE);
        registerInternalElement("xq-expression", ProcessorElementDef.class, null, null, "id",
                XMLNS_CORE_10, XMLNS_CORE);
        registerInternalElement("xslt", XsltDef.class, XsltProcessor.class, "!xml,!stylesheet", "id",
                XMLNS_CORE_10, XMLNS_CORE);
        registerInternalElement("xml", ProcessorElementDef.class, null, null, "id",
                XMLNS_CORE_10, XMLNS_CORE);
        registerInternalElement("stylesheet", ProcessorElementDef.class, null, null, "id",
                XMLNS_CORE_10, XMLNS_CORE);
        registerInternalElement("template", TemplateDef.class, TemplateProcessor.class, null, "id,language",
                XMLNS_CORE_10, XMLNS_CORE);
        registerInternalElement("case", CaseDef.class, CaseProcessor.class, "!if,else", "id",
                XMLNS_CORE_10, XMLNS_CORE);
        registerInternalElement("if", ProcessorElementDef.class, null, null, "!condition,id",
                XMLNS_CORE_10, XMLNS_CORE);
        registerInternalElement("else", ProcessorElementDef.class, null, null, "id",
                XMLNS_CORE_10, XMLNS_CORE);
        registerInternalElement("loop", LoopDef.class, LoopProcessor.class, "!list,!body", "id,item,index,maxloops,filter,empty",
                XMLNS_CORE_10, XMLNS_CORE);
        registerInternalElement("list", ProcessorElementDef.class, null, null, "id",
                XMLNS_CORE_10, XMLNS_CORE);
        registerInternalElement("body", ProcessorElementDef.class, null, null, "id",
                XMLNS_CORE_10, XMLNS_CORE);
        registerInternalElement("while", WhileDef.class, WhileProcessor.class, null, "id,!condition,index,maxloops,empty",
                XMLNS_CORE_10, XMLNS_CORE);
        registerInternalElement("function", FunctionDef.class, FunctionProcessor.class, null, "id,!name",
                XMLNS_CORE_10, XMLNS_CORE);
        registerInternalElement("return", ReturnDef.class, ReturnProcessor.class, null, "id",
                XMLNS_CORE_10, XMLNS_CORE);
        registerInternalElement("call", CallDef.class, CallProcessor10.class, null, "id,!name",
                XMLNS_CORE_10);
        registerInternalElement("call", CallDef.class, CallProcessor.class, null, "id,!name",
                XMLNS_CORE);
        registerInternalElement("call-param", CallParamDef.class, CallParamProcessor.class, null, "id,!name",
                XMLNS_CORE_10, XMLNS_CORE);
        registerInternalElement("include", IncludeDef.class, IncludeProcessor.class, "", "id,!path",
                XMLNS_CORE_10, XMLNS_CORE);
        registerInternalElement("try", TryDef.class, TryProcessor.class, "!body,!catch", "id",
                XMLNS_CORE_10, XMLNS_CORE);
        registerInternalElement("catch", ProcessorElementDef.class, null, null, "id",
                XMLNS_CORE_10, XMLNS_CORE);
        registerInternalElement("script", ScriptDef.class, ScriptProcessor.class, null, "id,language,return",
                XMLNS_CORE_10, XMLNS_CORE);
        registerInternalElement("exit", ExitDef.class, ExitProcessor.class, "", "id,condition,message",
                XMLNS_CORE_10, XMLNS_CORE);

        // register deprecated processor
        registerInternalElement("var-def", VarDefDef.class, VarDefProcessor.class, null, "id,!name,overwrite",
                XMLNS_CORE_10);
        registerInternalElement("var", VarDef.class, VarProcessor.class, "", "id,!name",
                XMLNS_CORE_10);

        // plugins for version 1.0 and 2.1
        registerPlugin(DatabasePlugin.class, true, XMLNS_CORE, XMLNS_CORE_10);
        registerPlugin(JsonToXmlPlugin.class, true, XMLNS_CORE, XMLNS_CORE_10);
        registerPlugin(XmlToJsonPlugin.class, true, XMLNS_CORE, XMLNS_CORE_10);
        registerPlugin(MailPlugin.class, true, XMLNS_CORE, XMLNS_CORE_10);
        registerPlugin(ZipPlugin.class, true, XMLNS_CORE, XMLNS_CORE_10);
        registerPlugin(FtpPlugin.class, true, XMLNS_CORE, XMLNS_CORE_10);
        registerPlugin(TokenizePlugin.class, true, XMLNS_CORE, XMLNS_CORE_10);

        // plugins introduced in version 2.1
        registerPlugin(SetVarPlugin.class, true, XMLNS_CORE);
        registerPlugin(DefVarPlugin.class, true, XMLNS_CORE);
        registerPlugin(GetVarPlugin.class, true, XMLNS_CORE);
        registerPlugin(ValueOfPlugin.class, true, XMLNS_CORE);
        registerPlugin(SleepPlugin.class, true, XMLNS_CORE);
    }

    private static void registerInternalElement(String name,
                                                Class<? extends IElementDef> defClass,
                                                Class<? extends AbstractProcessor> processorClass,
                                                String children, String attributes,
                                                String... xmlns) {
        final ElementInfo elementInfo = new ElementInfo(name, defClass, processorClass, children, attributes);
        for (String ns : xmlns) {
            elementInfos.put(new ElementName(name, ns), elementInfo);
        }
    }

    private static void registerPlugin(Class pluginClass, boolean isInternalPlugin, String... uris) {
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

            for (String uri : uris) {
                final ElementInfo elementInfo = new ElementInfo(
                        pluginName,
                        pluginClass,
                        isInternalPlugin,
                        WebHarvestPluginDef.class,
                        plugin.getTagDesc(),
                        plugin.getAttributeDesc(),
                        null);

                elementInfo.setPlugin(plugin);

                final ElementName pluginElementName = new ElementName(pluginName, uri);
                if (elementInfos.containsKey(pluginElementName)) {
                    throw new PluginException("Plugin \"" + pluginElementName + "\" is already registered!");
                }
                elementInfos.put(pluginElementName, elementInfo);


                if (!isInternalPlugin) {
                    externalPlugins.put(new PluginClassKey(pluginClass.getName(), uri), pluginElementName);
                }
                externalPluginDependencies.put(pluginElementName, plugin.getDependantProcessors());
            }

            for (Class subClass : plugin.getDependantProcessors()) {
                registerPlugin(subClass, isInternalPlugin, uris);
            }

        } catch (InstantiationException e) {
            throw new PluginException("Error instantiating plugin class \"" + pluginClass.getName() + "\": " + e.getMessage(), e);
        } catch (IllegalAccessException e) {
            throw new PluginException("Error instantiating plugin class \"" + pluginClass.getName() + "\": " + e.getMessage(), e);
        }
    }

    public static void registerPlugin(Class pluginClass, String uri) throws PluginException {
        registerPlugin(pluginClass, false, uri);
    }

    public static void registerPlugin(String className, String uri) throws PluginException {
        registerPlugin(ClassLoaderUtil.getPluginClass(className), false, uri);
    }

    public static void unregisterPlugin(Class pluginClass, String uri) {
        if (pluginClass != null) {
            unregisterPlugin(pluginClass.getName(), uri);
        }
    }

    public static void unregisterPlugin(String className, String uri) {
        final PluginClassKey key = new PluginClassKey(className, uri);
        // only external plugins can be unregistered
        if (externalPlugins.containsKey(key)) {
            final ElementName pluginElementName = externalPlugins.get(key);
            elementInfos.remove(pluginElementName);
            externalPlugins.remove(key);

            // unregister dependant classes as well
            Class[] dependantClasses = externalPluginDependencies.get(pluginElementName);
            externalPluginDependencies.remove(pluginElementName);
            if (dependantClasses != null) {
                for (Class c : dependantClasses) {
                    unregisterPlugin(c, uri);
                }
            }
        }
    }

    public static boolean isPluginRegistered(String className, String uri) {
        return externalPlugins.containsKey(new PluginClassKey(className, uri));
    }

    public static boolean isPluginRegistered(Class pluginClass, String uri) {
        return pluginClass != null && isPluginRegistered(pluginClass.getName(), uri);
    }

    /**
     * @return Map of all allowed element infos.
     */
    public static Map<ElementName, ElementInfo> getElementInfos() {
        return elementInfos;
    }

    /**
     * @param name Name of the element
     * @param uri  URI of the element
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
     * @param node node
     * @return Instance of IElementDef, or exception is thrown if cannot find
     *         appropriate element definition.
     */
    public static IElementDef createElementDefinition(XmlNode node) {
        final String nodeName = node.getName();
        final String nodeUri = node.getUri();

        final ElementInfo elementInfo = getElementInfo(nodeName, nodeUri);
        if (elementInfo == null || elementInfo.getDefinitionClass() == null || elementInfo.getDefinitionClass() == ProcessorElementDef.class) {
            throw new ConfigurationException("Unexpected configuration element: " + node.getQName() + "!");
        }

        validate(node);

        final Class elementClass = elementInfo.getDefinitionClass();

        try {
            final IElementDef elementDef = (elementClass == WebHarvestPluginDef.class)
                    ? new WebHarvestPluginDef(node)
                    : (IElementDef) elementClass.
                    getConstructor(XmlNode.class, Class.class).
                    newInstance(node, elementInfo.getProcessorClass());

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
                throw (ConfigurationException) cause;
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
     * @param node node
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
            if ((!areAllTagsAllowed && (!tags.contains(elementName.getName()) || !uri.equals(node.getUri()))) ||
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
            if (!atts.contains(attName) || !uri.equals(attUri)) {
                if (!elementInfo.getNsAttsSet().contains(attUri)) {
                    throw new ConfigurationException(ErrMsg.invalidAttribute(node.getName(), attName));
                }
            }
        }
    }

    // Deprecated stuff

    /**
     * Check if plugin is registered in <em>http://web-harvest.sourceforge.net/schema/1.0/config</em> namespace
     *
     * @param className plugin class
     * @return boolean
     * @deprecated Use {@link #isPluginRegistered(String className, String uri)}
     */
    @Deprecated public static boolean isPluginRegistered(String className) {
        return externalPlugins.containsKey(new PluginClassKey(className, XMLNS_CORE_10));
    }

    /**
     * Check if plugin is registered in <em>http://web-harvest.sourceforge.net/schema/1.0/config</em> namespace
     *
     * @param pluginClass plugin class
     * @return boolean
     * @deprecated Use {@link #isPluginRegistered(Class pluginClass, String uri)}
     */
    @Deprecated public static boolean isPluginRegistered(Class pluginClass) {
        return pluginClass != null && isPluginRegistered(pluginClass.getName(), XMLNS_CORE_10);
    }


    /**
     * Register plugin to <em>http://web-harvest.sourceforge.net/schema/1.0/config</em> namespace
     *
     * @param className plugin class
     * @throws org.webharvest.exception.PluginException
     *          trouble
     * @deprecated Use {@link #registerPlugin(String className, String uri)}
     */
    @Deprecated public static void registerPlugin(String className) throws PluginException {
        registerPlugin(ClassLoaderUtil.getPluginClass(className), false, XMLNS_CORE_10);
    }

    /**
     * Register plugin to <em>http://web-harvest.sourceforge.net/schema/1.0/config</em> namespace
     *
     * @param pluginClass plugin class
     * @throws org.webharvest.exception.PluginException
     *          trouble
     * @deprecated Use {@link #unregisterPlugin(Class pluginClass, String uri)}
     */
    @Deprecated public static void registerPlugin(Class pluginClass) throws PluginException {
        registerPlugin(pluginClass, false, XMLNS_CORE_10);
    }

    /**
     * Unregister plugin from <em>http://web-harvest.sourceforge.net/schema/1.0/config</em> namespace
     *
     * @param pluginClass plugin class
     * @deprecated Use {@link #unregisterPlugin(Class pluginClass, String uri)}
     */
    @Deprecated public static void unregisterPlugin(Class pluginClass) {
        unregisterPlugin(pluginClass, XMLNS_CORE_10);
    }

    /**
     * Unregister plugin from <em>http://web-harvest.sourceforge.net/schema/1.0/config</em> namespace
     *
     * @param className class name
     * @deprecated Use {@link #unregisterPlugin(String className, String uri)}
     */
    @Deprecated public static void unregisterPlugin(String className) {
        unregisterPlugin(className, XMLNS_CORE_10);
    }

}