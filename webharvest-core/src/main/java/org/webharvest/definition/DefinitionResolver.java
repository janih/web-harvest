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

import static org.webharvest.WHConstants.XMLNS_CORE;
import static org.webharvest.WHConstants.XMLNS_CORE_10;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.webharvest.AlreadyBoundException;
import org.webharvest.deprecated.runtime.processors.CallProcessor10;
import org.webharvest.deprecated.runtime.processors.VarDefProcessor;
import org.webharvest.deprecated.runtime.processors.VarProcessor;
import org.webharvest.exception.ConfigurationException;
import org.webharvest.exception.ErrMsg;
import org.webharvest.exception.PluginException;
import org.webharvest.runtime.processors.AbstractProcessor;
import org.webharvest.runtime.processors.CallParamProcessor;
import org.webharvest.runtime.processors.CallProcessor;
import org.webharvest.runtime.processors.CaseProcessor;
import org.webharvest.runtime.processors.ConstantProcessor;
import org.webharvest.runtime.processors.EmptyProcessor;
import org.webharvest.runtime.processors.ExitProcessor;
import org.webharvest.runtime.processors.FileProcessor;
import org.webharvest.runtime.processors.FunctionProcessor;
import org.webharvest.runtime.processors.HtmlToXmlProcessor;
import org.webharvest.runtime.processors.HttpHeaderProcessor;
import org.webharvest.runtime.processors.HttpParamProcessor;
import org.webharvest.runtime.processors.HttpProcessor;
import org.webharvest.runtime.processors.IncludeProcessor;
import org.webharvest.runtime.processors.LoopProcessor;
import org.webharvest.runtime.processors.RegexpProcessor;
import org.webharvest.runtime.processors.ReturnProcessor;
import org.webharvest.runtime.processors.ScriptProcessor;
import org.webharvest.runtime.processors.TemplateProcessor;
import org.webharvest.runtime.processors.TextProcessor;
import org.webharvest.runtime.processors.TryProcessor;
import org.webharvest.runtime.processors.WebHarvestPlugin;
import org.webharvest.runtime.processors.WhileProcessor;
import org.webharvest.runtime.processors.XPathProcessor;
import org.webharvest.runtime.processors.XQueryProcessor;
import org.webharvest.runtime.processors.XsltProcessor;
import org.webharvest.utils.Assert;
import org.webharvest.utils.ClassLoaderUtil;
import org.webharvest.utils.CommonUtil;

/**
 * Class contains information and logic to validate and crate definition classes for
 * parsed xml nodes from Web-Harvest configurations.
 *
 * @author Vladimir Nikic
 */
@SuppressWarnings({"UnusedDeclaration"})
public class DefinitionResolver extends AbstractRefreshableResolver {

    /**
     * Singleton instance reference.
     */
    public static final DefinitionResolver INSTANCE;

    static {
        INSTANCE = new DefinitionResolver();
    }

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

    // map containing pairs (class name, plugin element name)
    // of externally registered plugins
    private Map<PluginClassKey, ElementName> externalPlugins =
        new LinkedHashMap<PluginClassKey, ElementName>();

    // map of external plugin dependencies
    private Map<ElementName, Class[]> externalPluginDependencies =
        new HashMap<ElementName, Class[]>();


    private DefinitionResolver() {
        // FIXME: invocation below is a dirty hack and should be removed when
        //        deprecated registerInternalProcessors method will be moved
        //        somewhere else
        addPostProcessor(new ResolverPostProcessor() {
            @Override
            public void postProcess(final ConfigurableResolver resolver) {
                registerInternalProcessors();
            }
        });
        addPostProcessor(new AnnotatedPluginsPostProcessor(
            "org.webharvest.runtime.processors.plugins"));

        refresh();
    }

    @Deprecated
    private void registerInternalProcessors() {
         // register processors
        registerInternalElement("config", WebHarvestPluginDef.class, null, null, "charset,scriptlang,id",
                XMLNS_CORE_10, XMLNS_CORE);
        registerInternalElement("empty", EmptyDef.class, EmptyProcessor.class, null, "id",
                XMLNS_CORE_10, XMLNS_CORE);
        registerInternalElement("text", TextDef.class, TextProcessor.class, null, "id,charset,delimiter",
                XMLNS_CORE_10, XMLNS_CORE);
        registerInternalElement("file", FileDef.class, FileProcessor.class, null,
                "id,!path,action,type,charset,listfilter,listfiles,listdirs,listrecursive",
                XMLNS_CORE_10, XMLNS_CORE);
        registerInternalElement("http", HttpDef.class, HttpProcessor.class, null,
                "id,!url,method,follow-redirects,ignore-response-body,retry-attempts,retry-delay,retry-delay-factor,content-type,charset,username,password,cookie-policy",
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
        registerInternalElement("regexp-pattern", WebHarvestPluginDef.class, null, null, "id",
                XMLNS_CORE_10, XMLNS_CORE);
        registerInternalElement("regexp-source", WebHarvestPluginDef.class, null, null, "id",
                XMLNS_CORE_10, XMLNS_CORE);
        registerInternalElement("regexp-result", WebHarvestPluginDef.class, null, null, "id",
                XMLNS_CORE_10, XMLNS_CORE);
        registerInternalElement("xpath", XPathDef.class, XPathProcessor.class, null, "id,expression,v:*",
                XMLNS_CORE_10, XMLNS_CORE);
        registerInternalElement("xquery", XQueryDef.class, XQueryProcessor.class, "xq-param,!xq-expression", "id",
                XMLNS_CORE_10, XMLNS_CORE);
        registerInternalElement("xq-param", XQueryExternalParamDef.class, null, null, "!name,type,id",
                XMLNS_CORE_10, XMLNS_CORE);
        registerInternalElement("xq-expression", WebHarvestPluginDef.class, null, null, "id",
                XMLNS_CORE_10, XMLNS_CORE);
        registerInternalElement("xslt", XsltDef.class, XsltProcessor.class, "!xml,!stylesheet", "id",
                XMLNS_CORE_10, XMLNS_CORE);
        registerInternalElement("xml", WebHarvestPluginDef.class, null, null, "id",
                XMLNS_CORE_10, XMLNS_CORE);
        registerInternalElement("stylesheet", WebHarvestPluginDef.class, null, null, "id",
                XMLNS_CORE_10, XMLNS_CORE);
        registerInternalElement("template", TemplateDef.class, TemplateProcessor.class, null, "id,language",
                XMLNS_CORE_10, XMLNS_CORE);
        registerInternalElement("case", CaseDef.class, CaseProcessor.class, "!if,else", "id",
                XMLNS_CORE_10, XMLNS_CORE);
        registerInternalElement("if", IfDef.class, null, null, "!condition,id",
                XMLNS_CORE_10, XMLNS_CORE);
        registerInternalElement("else", WebHarvestPluginDef.class, null, null, "id",
                XMLNS_CORE_10, XMLNS_CORE);
        registerInternalElement("loop", LoopDef.class, LoopProcessor.class, "!list,!body", "id,item,index,maxloops,filter,empty",
                XMLNS_CORE_10, XMLNS_CORE);
        registerInternalElement("list", WebHarvestPluginDef.class, null, null, "id",
                XMLNS_CORE_10, XMLNS_CORE);
        registerInternalElement("body", WebHarvestPluginDef.class, null, null, "id",
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
        registerInternalElement("catch", WebHarvestPluginDef.class, null, null, "id",
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
    }

    private void registerInternalElement(String name,
                                                Class<? extends IElementDef> defClass,
                                                Class<? extends AbstractProcessor> processorClass,
                                                String children, String attributes,
                                                String... xmlns) {
        final ElementInfo elementInfo = new ElementInfo(name, defClass, processorClass, children, attributes);
        for (String ns : xmlns) {
            try {
                getElementsRegistry().bind(new ElementName(name, ns),
                        elementInfo);
            } catch (AlreadyBoundException e) {
                // FIXME: This exception should never happen, since
                // only internal elements are registered here. We'll get rid
                // of this exception as soon as we'll refactor processors
                // registration logic
                throw new RuntimeException(e);
            }
        }
    }

    private void registerPlugin(Class pluginClass, boolean isInternalPlugin, String... uris) {
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
                        WebHarvestPluginDef.class,
                        pluginClass,
                        plugin.getTagDesc(),
                        plugin.getAttributeDesc());

                elementInfo.setPlugin(plugin);

                final ElementName pluginElementName = new ElementName(pluginName, uri);
                try {
                    getElementsRegistry().bind(pluginElementName, elementInfo);
                } catch (AlreadyBoundException e) {
                    throw new PluginException("Plugin \"" + pluginElementName + "\" is already registered!");
                }

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

    /**
     * {@inheritDoc}
     */
    @Override
    public void registerPlugin(
            final Class< ? extends WebHarvestPlugin > pluginClass,
            final String namespace) throws PluginException {
        registerPlugin(pluginClass, false, namespace);
    }

    public void registerPlugin(String className, String uri) throws PluginException {
        registerPlugin(ClassLoaderUtil.getPluginClass(className), false, uri);
    }

    public void unregisterPlugin(Class pluginClass, String uri) {
        if (pluginClass != null) {
            unregisterPlugin(pluginClass.getName(), uri);
        }
    }

    public void unregisterPlugin(String className, String uri) {
        final PluginClassKey key = new PluginClassKey(className, uri);
        // only external plugins can be unregistered
        if (externalPlugins.containsKey(key)) {
            final ElementName pluginElementName = externalPlugins.get(key);
            getElementsRegistry().unbind(pluginElementName);
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

    public boolean isPluginRegistered(String className, String uri) {
        return externalPlugins.containsKey(new PluginClassKey(className, uri));
    }

    public boolean isPluginRegistered(Class pluginClass, String uri) {
        return pluginClass != null && isPluginRegistered(pluginClass.getName(), uri);
    }

    /**
     * Returns names of all known elements.
     */
    public Set<ElementName> getElementNames() {
        return getElementsRegistry().listBound();
    }

    /**
     * @param name Name of the element
     * @param uri  URI of the element
     * @return Instance of ElementInfo class for the specified element name,
     *         or null if no element is defined.
     */
    public ElementInfo getElementInfo(String name, String uri) {
        return getElementsRegistry().lookup(new ElementName(name, uri));
    }

    /**
     * Creates proper element definition instance based on given xml node
     * from input configuration.
     *
     * @param node node
     * @return Instance of IElementDef, or exception is thrown if cannot find
     *         appropriate element definition.
     */
    public IElementDef createElementDefinition(XmlNode node) {
        final String nodeName = node.getName();
        final String nodeUri = node.getUri();

        final ElementInfo elementInfo = getElementInfo(nodeName, nodeUri);
        if (elementInfo == null || elementInfo.getDefinitionClass() == null) {
            throw new ConfigurationException("Unexpected configuration element: " + node.getQName() + "!");
        }

        validate(node);

        //FIXME: use a better construction than this as soon as possible
        try {
            final AbstractElementDef elementDef = (AbstractElementDef) elementInfo.getDefinitionClass().
                getConstructor(XmlNode.class, Class.class).
                newInstance(node, elementInfo.getProcessorClass());

            for (final Object element : node.getElementList()) {
                elementDef.add(toElementDef(element));
            }

            return elementDef;
        } catch (NoSuchMethodException e) {
            throw new ConfigurationException("Cannot create class instance: " +
                    elementInfo.getDefinitionClass() + "!", e);
        } catch (InvocationTargetException e) {
            final Throwable cause = e.getCause();
            if (cause instanceof ConfigurationException) {
                throw (ConfigurationException) cause;
            }
            throw new ConfigurationException("Cannot create class instance: " +
                    elementInfo.getDefinitionClass() + "!", e);
        } catch (InstantiationException e) {
            throw new ConfigurationException("Cannot create class instance: " +
                    elementInfo.getDefinitionClass() + "!", e);
        } catch (IllegalAccessException e) {
            throw new ConfigurationException("Cannot create class instance: " +
                    elementInfo.getDefinitionClass() + "!", e);
        }
    }

    private IElementDef toElementDef(final Object subject) {
        if (subject instanceof XmlNode) {
            // TODO Use a proxy instead of real definition
            return createElementDefinition((XmlNode) subject);
        } else {
            // TODO Use a proxy instead of real definition
            return new ConstantDef(subject.toString(), ConstantProcessor.class);
        }
    }

    /**
     * Validates specified xml node with appropriate element info instance.
     * If validation fails, an runtime exception is thrown.
     *
     * @param node node
     */
    public void validate(XmlNode node) {
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
        final Set<ElementName> allTagNameSet =
            getElementsRegistry().listBound();
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
    @Deprecated public boolean isPluginRegistered(String className) {
        return externalPlugins.containsKey(new PluginClassKey(className, XMLNS_CORE_10));
    }

    /**
     * Check if plugin is registered in <em>http://web-harvest.sourceforge.net/schema/1.0/config</em> namespace
     *
     * @param pluginClass plugin class
     * @return boolean
     * @deprecated Use {@link #isPluginRegistered(Class pluginClass, String uri)}
     */
    @Deprecated public boolean isPluginRegistered(Class pluginClass) {
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
    @Deprecated public void registerPlugin(String className) throws PluginException {
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
    @Deprecated public void registerPlugin(Class pluginClass) throws PluginException {
        registerPlugin(pluginClass, false, XMLNS_CORE_10);
    }

    /**
     * Unregister plugin from <em>http://web-harvest.sourceforge.net/schema/1.0/config</em> namespace
     *
     * @param pluginClass plugin class
     * @deprecated Use {@link #unregisterPlugin(Class pluginClass, String uri)}
     */
    @Deprecated public void unregisterPlugin(Class pluginClass) {
        unregisterPlugin(pluginClass, XMLNS_CORE_10);
    }

    /**
     * Unregister plugin from <em>http://web-harvest.sourceforge.net/schema/1.0/config</em> namespace
     *
     * @param className class name
     * @deprecated Use {@link #unregisterPlugin(String className, String uri)}
     */
    @Deprecated public void unregisterPlugin(String className) {
        unregisterPlugin(className, XMLNS_CORE_10);
    }

}