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
        registerInternalElement("config", ProcessorElementDef.class, null, null, "charset,scriptlang,id",
                Constants.XMLNS_CORE_10, Constants.XMLNS_CORE);
        registerInternalElement("empty", EmptyDef.class, EmptyProcessor.class, null, "id",
                Constants.XMLNS_CORE_10, Constants.XMLNS_CORE);
        registerInternalElement("text", TextDef.class, TextProcessor.class, null, "id,charset,delimiter",
                Constants.XMLNS_CORE_10, Constants.XMLNS_CORE);
        registerInternalElement("file", FileDef.class, FileProcessor.class, null,
                "id,!path,action,type,charset,listfilter,listfiles,listdirs,listrecursive",
                Constants.XMLNS_CORE_10, Constants.XMLNS_CORE);
        registerInternalElement("var-def", VarDefDef.class, VarDefProcessor.class, null, "id,!name,overwrite",
                Constants.XMLNS_CORE_10);
        registerInternalElement("var", VarDef.class, VarProcessor.class, "", "id,!name",
                Constants.XMLNS_CORE_10);
        registerInternalElement("http", HttpDef.class, HttpProcessor.class, null,
                "id,!url,method,follow-redirects,retry-attempts,retry-delay,retry-delay-factor,multipart,charset,username,password,cookie-policy",
                Constants.XMLNS_CORE_10, Constants.XMLNS_CORE);
        registerInternalElement("http-param", HttpParamDef.class, HttpParamProcessor.class, null, "id,!name,isfile,filename,contenttype",
                Constants.XMLNS_CORE_10, Constants.XMLNS_CORE);
        registerInternalElement("http-header", HttpHeaderDef.class, HttpHeaderProcessor.class, null, "id,!name",
                Constants.XMLNS_CORE_10, Constants.XMLNS_CORE);
        registerInternalElement("html-to-xml", HtmlToXmlDef.class, HtmlToXmlProcessor.class, null, "" +
                "id,outputtype,advancedxmlescape,usecdata,specialentities,unicodechars,nbsp-to-sp," +
                "omitunknowntags,treatunknowntagsascontent,omitdeprtags,treatdeprtagsascontent," +
                "omitxmldecl,omitcomments,omithtmlenvelope,useemptyelementtags,allowmultiwordattributes," +
                "allowhtmlinsideattributes,namespacesaware,hyphenreplacement,prunetags,booleanatts",
                Constants.XMLNS_CORE_10, Constants.XMLNS_CORE);
        registerInternalElement("regexp", RegexpDef.class, RegexpProcessor.class,
                "!regexp-pattern,!regexp-source,regexp-result", "id,replace,max,flag-caseinsensitive,flag-multiline,flag-dotall,flag-unicodecase,flag-canoneq",
                Constants.XMLNS_CORE_10, Constants.XMLNS_CORE);
        registerInternalElement("regexp-pattern", ProcessorElementDef.class, null, null, "id",
                Constants.XMLNS_CORE_10, Constants.XMLNS_CORE);
        registerInternalElement("regexp-source", ProcessorElementDef.class, null, null, "id",
                Constants.XMLNS_CORE_10, Constants.XMLNS_CORE);
        registerInternalElement("regexp-result", ProcessorElementDef.class, null, null, "id",
                Constants.XMLNS_CORE_10, Constants.XMLNS_CORE);
        registerInternalElement("xpath", XPathDef.class, XPathProcessor.class, null, "id,expression,v:*",
                Constants.XMLNS_CORE_10, Constants.XMLNS_CORE);
        registerInternalElement("xquery", XQueryDef.class, XQueryProcessor.class, "xq-param,!xq-expression", "id",
                Constants.XMLNS_CORE_10, Constants.XMLNS_CORE);
        registerInternalElement("xq-param", ProcessorElementDef.class, null, null, "!name,type,id",
                Constants.XMLNS_CORE_10, Constants.XMLNS_CORE);
        registerInternalElement("xq-expression", ProcessorElementDef.class, null, null, "id",
                Constants.XMLNS_CORE_10, Constants.XMLNS_CORE);
        registerInternalElement("xslt", XsltDef.class, XsltProcessor.class, "!xml,!stylesheet", "id",
                Constants.XMLNS_CORE_10, Constants.XMLNS_CORE);
        registerInternalElement("xml", ProcessorElementDef.class, null, null, "id",
                Constants.XMLNS_CORE_10, Constants.XMLNS_CORE);
        registerInternalElement("stylesheet", ProcessorElementDef.class, null, null, "id",
                Constants.XMLNS_CORE_10, Constants.XMLNS_CORE);
        registerInternalElement("template", TemplateDef.class, TemplateProcessor.class, null, "id,language",
                Constants.XMLNS_CORE_10, Constants.XMLNS_CORE);
        registerInternalElement("case", CaseDef.class, CaseProcessor.class, "!if,else", "id",
                Constants.XMLNS_CORE_10, Constants.XMLNS_CORE);
        registerInternalElement("if", ProcessorElementDef.class, null, null, "!condition,id",
                Constants.XMLNS_CORE_10, Constants.XMLNS_CORE);
        registerInternalElement("else", ProcessorElementDef.class, null, null, "id",
                Constants.XMLNS_CORE_10, Constants.XMLNS_CORE);
        registerInternalElement("loop", LoopDef.class, LoopProcessor.class, "!list,!body", "id,item,index,maxloops,filter,empty",
                Constants.XMLNS_CORE_10, Constants.XMLNS_CORE);
        registerInternalElement("list", ProcessorElementDef.class, null, null, "id",
                Constants.XMLNS_CORE_10, Constants.XMLNS_CORE);
        registerInternalElement("body", ProcessorElementDef.class, null, null, "id",
                Constants.XMLNS_CORE_10, Constants.XMLNS_CORE);
        registerInternalElement("while", WhileDef.class, WhileProcessor.class, null, "id,!condition,index,maxloops,empty",
                Constants.XMLNS_CORE_10, Constants.XMLNS_CORE);
        registerInternalElement("function", FunctionDef.class, FunctionProcessor.class, null, "id,!name",
                Constants.XMLNS_CORE_10, Constants.XMLNS_CORE);
        registerInternalElement("return", ReturnDef.class, ReturnProcessor.class, null, "id",
                Constants.XMLNS_CORE_10, Constants.XMLNS_CORE);
        registerInternalElement("call", CallDef.class, CallProcessor10.class, null, "id,!name",
                Constants.XMLNS_CORE_10);
        registerInternalElement("call", CallDef.class, CallProcessor.class, null, "id,!name",
                Constants.XMLNS_CORE);
        registerInternalElement("call-param", CallParamDef.class, CallParamProcessor.class, null, "id,!name",
                Constants.XMLNS_CORE_10, Constants.XMLNS_CORE);
        registerInternalElement("include", IncludeDef.class, IncludeProcessor.class, "", "id,!path",
                Constants.XMLNS_CORE_10, Constants.XMLNS_CORE);
        registerInternalElement("try", TryDef.class, TryProcessor.class, "!body,!catch", "id",
                Constants.XMLNS_CORE_10, Constants.XMLNS_CORE);
        registerInternalElement("catch", ProcessorElementDef.class, null, null, "id",
                Constants.XMLNS_CORE_10, Constants.XMLNS_CORE);
        registerInternalElement("script", ScriptDef.class, ScriptProcessor.class, null, "id,language,return",
                Constants.XMLNS_CORE_10, Constants.XMLNS_CORE);
        registerInternalElement("exit", ExitDef.class, ExitProcessor.class, "", "id,condition,message",
                Constants.XMLNS_CORE_10, Constants.XMLNS_CORE);

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


            final ElementInfo elementInfo = new ElementInfo(
                    pluginName,
                    pluginClass,
                    isInternalPlugin,
                    WebHarvestPluginDef.class,
                    plugin.getTagDesc(),
                    plugin.getAttributeDesc(),
                    null);

            elementInfo.setPlugin(plugin);

            if (elementInfos.containsKey(pluginElementName)) {
                throw new PluginException("Plugin \"" + pluginElementName + "\" is already registered!");
            }
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

    public static void registerPlugin(Class pluginClass, String uri) throws PluginException {
        registerPlugin(pluginClass, false, uri);
    }

    public static void registerPlugin(String fullClassName, String uri) throws PluginException {
        registerPlugin(ClassLoaderUtil.getPluginClass(fullClassName), false, uri);
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
     * @param node
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
        if (elementClass == WebHarvestPluginDef.class) {
            return new WebHarvestPluginDef(node);
        }

        try {
            final IElementDef elementDef = (IElementDef) elementClass.
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

}