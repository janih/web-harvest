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

import org.webharvest.runtime.processors.WebHarvestPlugin;

import java.util.Iterator;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeSet;

/**
 * @author: Vladimir Nikic
 * Date: May 24, 2007
 */
public class ElementInfo {

    // properties containing suggested attribute values

    private String name;
    private Class pluginClass;
    private boolean isInternalPlugin;
    private Class definitionClass;
    private String validTags;
    private String validAtts;

    private Set tagsSet = new TreeSet();
    private Set requiredTagsSet = new TreeSet();
    private Set attsSet = new TreeSet();
    private Set requiredAttsSet = new TreeSet();

    private boolean allTagsAllowed;

    // pluging instance for this element, if element represents Web-Harvest plugin
    private WebHarvestPlugin plugin = null;

    public ElementInfo(String name, Class definitionClass, String validTags, String validAtts) {
        this(name, null, true, definitionClass, validTags, validAtts);
    }

    public ElementInfo(String name, Class pluginClass, boolean isInternalPlugin, Class definitionClass, String validTags, String validAtts) {
        this.name = name;
        this.pluginClass = pluginClass;
        this.isInternalPlugin = isInternalPlugin;
        this.definitionClass = definitionClass;
        this.validTags = validTags;
        this.validAtts = validAtts;

        this.allTagsAllowed = validTags == null;

        if (validTags != null) {
            StringTokenizer tokenizer = new StringTokenizer(validTags, ",");
            while (tokenizer.hasMoreTokens()) {
                String token = tokenizer.nextToken().toLowerCase();
                if (token.startsWith("!")) {
                    token = token.substring(1);
                    this.requiredTagsSet.add(token);
                }
                this.tagsSet.add(token);
            }
        }

        if (validAtts != null) {
            StringTokenizer tokenizer = new StringTokenizer(validAtts, ",");
            while (tokenizer.hasMoreTokens()) {
                String token = tokenizer.nextToken().toLowerCase();
                if (token.startsWith("!")) {
                    token = token.substring(1);
                    this.requiredAttsSet.add(token);
                }
                this.attsSet.add(token);
            }
        }
    }

    /**
     * @param onlyRequiredAtts
     * @return Template with allowed attributes.
     */
    public String getTemplate(boolean onlyRequiredAtts) {
        StringBuffer result = new StringBuffer("<" + this.name);

        Set atts = onlyRequiredAtts ? this.requiredAttsSet : this.attsSet;

        Iterator iterator = atts.iterator();
        while (iterator.hasNext()) {
            String att = (String) iterator.next();
            result.append(" " + att + "=\"\"");
        }

        // if no valid subtags
        if (this.validTags != null && "".equals(this.validTags.trim())) {
            result.append("/>");
        } else {
            result.append("></" + name + ">");
        }

        return result.toString();
    }


    public Class getPluginClass() {
        return pluginClass;
    }

    public boolean isInternalPlugin() {
        return isInternalPlugin;
    }

    public Class getDefinitionClass() {
        return definitionClass;
    }

    public String getName() {
        return name;
    }

    public Set getTagsSet() {
        return tagsSet;
    }

    public Set getAttsSet() {
        return attsSet;
    }

    public Set getRequiredAttsSet() {
        return requiredAttsSet;
    }

    public Set getRequiredTagsSet() {
        return requiredTagsSet;
    }

    public boolean areAllTagsAllowed() {
        return allTagsAllowed;
    }

    public void setPlugin(WebHarvestPlugin plugin) {
        this.plugin = plugin;
    }

    public WebHarvestPlugin getPlugin() {
        return plugin;
    }
}