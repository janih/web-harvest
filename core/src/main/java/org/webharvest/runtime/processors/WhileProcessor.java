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
package org.webharvest.runtime.processors;

import org.apache.commons.lang.math.NumberUtils;
import org.webharvest.definition.WhileDef;
import org.webharvest.runtime.Scraper;
import org.webharvest.runtime.ScraperContext;
import org.webharvest.runtime.templaters.BaseTemplater;
import org.webharvest.runtime.variables.EmptyVariable;
import org.webharvest.runtime.variables.ListVariable;
import org.webharvest.runtime.variables.NodeVariable;
import org.webharvest.runtime.variables.Variable;
import org.webharvest.utils.CommonUtil;
import org.webharvest.utils.Constants;

import java.util.ArrayList;
import java.util.List;

/**
 * Conditional processor.
 */
public class WhileProcessor extends BaseProcessor<WhileDef> {

    public WhileProcessor(WhileDef whileDef) {
        super(whileDef);
    }

    public Variable execute(final Scraper scraper, final ScraperContext context) {
        final String index = BaseTemplater.evaluateToString(elementDef.getIndex(), null, scraper);
        final String maxLoopsString = BaseTemplater.evaluateToString(elementDef.getMaxLoops(), null, scraper);
        final boolean isEmpty = CommonUtil.getBooleanValue(BaseTemplater.evaluateToString(elementDef.getEmpty(), null, scraper), false);

        final List<Object> resultList = new ArrayList<Object>();

        context.executeWithinNewContext(new Runnable() {
            public void run() {
                int i = 1;

                // define first value of index variable
                if (index != null && !"".equals(index)) {
                    context.setLocalVar(index, new NodeVariable(String.valueOf(i)));
                }

                String condition = BaseTemplater.evaluateToString(elementDef.getCondition(), null, scraper);

                setProperty("Condition", condition);
                setProperty("Index", index);
                setProperty("Max Loops", maxLoopsString);
                setProperty("Empty", String.valueOf(isEmpty));

                // iterates while testing variable represents boolean true or loop limit is exceeded
                final double maxLoops = NumberUtils.toDouble(maxLoopsString, Constants.DEFAULT_MAX_LOOPS);
                while (CommonUtil.isBooleanTrue(condition) && (i <= maxLoops)) {
                    Variable loopResult = new BodyProcessor(elementDef).execute(scraper, context);
                    if (!isEmpty) {
                        resultList.addAll(loopResult.toList());
                    }

                    i++;
                    // define current value of index variable
                    if (index != null && !"".equals(index)) {
                        context.setLocalVar(index, new NodeVariable(String.valueOf(i)));
                    }

                    condition = BaseTemplater.evaluateToString(elementDef.getCondition(), null, scraper);
                }
            }
        }, true);

        return isEmpty ? EmptyVariable.INSTANCE : new ListVariable(resultList);
    }

}