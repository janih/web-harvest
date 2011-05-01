/*  Copyright (c) 2006-2008, Vladimir Nikic
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
package org.webharvest.runtime.processors;

import org.webharvest.definition.WebHarvestPluginDef;
import org.webharvest.runtime.DynamicScopeContext;
import org.webharvest.runtime.Scraper;
import org.webharvest.runtime.templaters.BaseTemplater;
import org.webharvest.runtime.variables.Variable;
import org.webharvest.utils.Assert;
import org.webharvest.utils.CommonUtil;

import java.util.Map;

/**
 * Base for all user-defined plugins.
 */
@SuppressWarnings({"UnusedDeclaration"})
public abstract class WebHarvestPlugin extends AbstractProcessor<WebHarvestPluginDef> {

    private static final Class[] EMPTY_CLASS_ARRAY = new Class[0];

    /**
     * Defines name of the processor. Should be valid identifier.
     * Processor's tag will use this name. For example, if this name is
     * "myprocessor" in config file it will be used as &lt;myprocessor...&gt;...&lt;/myprocessor&gt;
     *
     * @return Name of the processor
     */
    public abstract String getName();

    /**
     * This method should return all possible attribute names for the plugin processor.
     *
     * @return Array of attribute names
     */
    public String[] getValidAttributes() {
        return new String[]{};
    }

    /**
     * This method should return all mandatory attribute names for the plugin processor.
     *
     * @return Array of attribute names
     */
    public String[] getRequiredAttributes() {
        return new String[]{};
    }

    /**
     * This method should return all names of all allowed processors inside the body of
     * this processor plugin. If null is returned, then all subprocessors are allowed.
     *
     * @return Array of allowed subprocessor names (case insensitive)
     */
    public String[] getValidSubprocessors() {
        return null;
    }

    /**
     * This method should return all mandatory subprocessor names, or in other words all
     * mandatory subtags that must be present in the body of this processor plugin.
     *
     * @return Array of mandatory subprocessor names
     */
    public String[] getRequiredSubprocessors() {
        return new String[]{};
    }

    /**
     * Defines dependant subprocessors that are used inside this plugin and that will
     * automatically be registered with this plugin.
     *
     * @return Array of subprocessor classes
     */
    public Class[] getDependantProcessors() {
        return EMPTY_CLASS_ARRAY;
    }

    /**
     * @param attributeName Name of plugin attribute
     * @return Array of valuee suggestions for specified attribute (this is used for auto-complete feature in IDE).
     *         null means nothing to suggest.
     */
    public String[] getAttributeValueSuggestions(String attributeName) {
        return null;
    }

    public boolean hasBody() {
        return true;
    }

    public final Variable execute(Scraper scraper, DynamicScopeContext context) throws InterruptedException {
        // pre processing
        final Variable variable = executePlugin(scraper, context);
        Assert.notNull(variable, "Plugin {0} returned 'null' instead of 'empty'", getClass().getName());
        // post processing
        return variable;
    }

    /**
     * Mathod that actually executes processor. Since one instance of this class may
     * be used for multiple executions, creator of plugin is responsible for initiating
     * local variables at the beginning of this method.
     *
     *
     * @param scraper
     * @param context
     * @return Instance of variable as result of execution.
     */
    public abstract Variable executePlugin(Scraper scraper, DynamicScopeContext context) throws InterruptedException;

    public String getTagDesc() {
        if (!hasBody()) {
            return "";
        }

        String[] validSubprocessors = getValidSubprocessors();
        if (validSubprocessors == null) {
            return null;
        }

        String requiredTags[] = getRequiredSubprocessors();

        StringBuffer result = new StringBuffer();
        for (String subProcessor : validSubprocessors) {
            if (result.length() != 0) {
                result.append(',');
            }
            if (CommonUtil.existsInStringArray(requiredTags, subProcessor, true)) {
                result.append('!');
            }
            result.append(subProcessor);
        }
        return result.toString();
    }

    public String getAttributeDesc() {
        String[] validAtts = getValidAttributes();
        if (validAtts == null) {
            return "id";
        }

        String requiredAtts[] = getRequiredAttributes();

        StringBuffer result = new StringBuffer("id,");
        for (String attr : validAtts) {
            if (CommonUtil.existsInStringArray(requiredAtts, attr, true)) {
                result.append('!');
            }
            result.append(attr);
            result.append(",");
        }
        return result.toString();
    }

    public void setDef(WebHarvestPluginDef def) {
        this.elementDef = def;
    }

    /**
     * @return Map of attributes of this plugin
     */
    protected Map<String, String> getAttributes() {
        return elementDef.getAttributes();
    }

    /**
     * @return Map of attributes of this plugin
     */
    protected Map<String, String> getAttributes(String uri) {
        return elementDef.getAttributes(uri);
    }

    /**
     * @param attName Name of attribute
     * @param scraper
     * @return Value of specified attribute, or null if attribute doesn't exist
     */
    protected String evaluateAttribute(String attName, String uri, Scraper scraper) {
        return BaseTemplater.evaluateToString(getAttributes(uri).get(attName), null, scraper);
    }

    protected String evaluateAttribute(String attName, Scraper scraper) {
        return evaluateAttribute(attName, elementDef.getUri(), scraper);
    }

    /**
     * @param attName      Name of attribute
     * @param defaultValue
     * @param scraper
     * @return Value of specified attribute as boolean, or default value if it cannot be recognized as valid boolean
     */
    protected boolean evaluateAttributeAsBoolean(String attName, String uri, boolean defaultValue, Scraper scraper) {
        return CommonUtil.getBooleanValue(evaluateAttribute(attName, uri, scraper), defaultValue);
    }

    protected boolean evaluateAttributeAsBoolean(String attName, boolean defaultValue, Scraper scraper) {
        return CommonUtil.getBooleanValue(evaluateAttribute(attName, elementDef.getUri(), scraper), defaultValue);
    }

    /**
     * @param attName      Name of attribute
     * @param defaultValue
     * @param scraper
     * @return Value of specified attribute as integer, or default value if it cannot be recognized as valid integer
     */
    protected int evaluateAttributeAsInteger(String attName, String uri, int defaultValue, Scraper scraper) {
        return CommonUtil.getIntValue(evaluateAttribute(attName, uri, scraper), defaultValue);
    }

    protected int evaluateAttributeAsInteger(String attName, int defaultValue, Scraper scraper) {
        return CommonUtil.getIntValue(evaluateAttribute(attName, elementDef.getUri(), scraper), defaultValue);
    }

    /**
     * @param attName      Name of attribute
     * @param defaultValue
     * @param scraper
     * @return Value of specified attribute as double, or default value if it cannot be recognized as valid double
     */
    protected double evaluateAttributeAsDouble(String attName, String uri, double defaultValue, Scraper scraper) {
        return CommonUtil.getDoubleValue(evaluateAttribute(attName, uri, scraper), defaultValue);
    }

    protected double evaluateAttributeAsDouble(String attName, double defaultValue, Scraper scraper) {
        return CommonUtil.getDoubleValue(evaluateAttribute(attName, elementDef.getUri(), scraper), defaultValue);
    }

    /**
     * Executes body of plugin processor
     *
     *
     * @param scraper
     * @param context
     * @return Instance of Variable
     */
    protected Variable executeBody(Scraper scraper, DynamicScopeContext context) throws InterruptedException {
        return new BodyProcessor(elementDef).execute(scraper, context);
    }

}