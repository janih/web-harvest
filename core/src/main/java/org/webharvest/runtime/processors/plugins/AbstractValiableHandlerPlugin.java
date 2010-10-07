/*
 Copyright (c) 2006-2007, Vladimir Nikic
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

package org.webharvest.runtime.processors.plugins;

import org.webharvest.runtime.Scraper;
import org.webharvest.runtime.ScraperContext;
import org.webharvest.runtime.processors.WebHarvestPlugin;
import org.webharvest.runtime.templaters.BaseTemplater;
import org.webharvest.runtime.variables.EmptyVariable;
import org.webharvest.runtime.variables.Variable;

abstract class AbstractValiableHandlerPlugin extends WebHarvestPlugin {

    private static final String ATTR_VAR = "var";
    private static final String ATTR_VALUE = "value";

    private final String name;

    protected AbstractValiableHandlerPlugin(String name) {
        this.name = name;
    }

    public Variable executePlugin(Scraper scraper, ScraperContext context) {
        final String varName = evaluateAttribute(ATTR_VAR, scraper);

        Variable value = BaseTemplater.executeToVariable(getAttributes().get(ATTR_VALUE), null, scraper);

        if (value.isEmpty()) {
            value = executeBody(scraper, context);
        }

        doExecute(context, varName, value);

        this.setProperty("\"" + varName + "\"", value);

        return EmptyVariable.INSTANCE;
    }

    protected abstract void doExecute(ScraperContext context, String varName, Variable value);

    public String getName() {
        return name;
    }

    public String[] getValidAttributes() {
        return new String[]{ATTR_VAR, ATTR_VALUE};
    }

    @Override
    public String[] getRequiredAttributes() {
        return new String[]{ATTR_VAR};
    }
}
