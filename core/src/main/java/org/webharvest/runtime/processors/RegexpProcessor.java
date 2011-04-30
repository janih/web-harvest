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
import org.webharvest.definition.ProcessorElementDef;
import org.webharvest.definition.RegexpDef;
import org.webharvest.runtime.Scraper;
import org.webharvest.runtime.ScraperContext;
import org.webharvest.runtime.templaters.BaseTemplater;
import org.webharvest.runtime.variables.ListVariable;
import org.webharvest.runtime.variables.NodeVariable;
import org.webharvest.runtime.variables.Variable;
import org.webharvest.utils.CommonUtil;
import org.webharvest.utils.Constants;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Regular expression replace processor.
 */
public class RegexpProcessor extends AbstractProcessor<RegexpDef> {

    public RegexpProcessor(RegexpDef regexpDef) {
        super(regexpDef);
    }

    public Variable execute(final Scraper scraper, final ScraperContext context) throws InterruptedException {

        ProcessorElementDef patternDef = elementDef.getRegexpPatternDef();
        Variable patternVar = getBodyTextContent(patternDef, scraper, context, true);
        debug(patternDef, scraper, patternVar);

        ProcessorElementDef sourceDef = elementDef.getRegexpSourceDef();
        Variable source = new BodyProcessor(sourceDef).run(scraper, context);
        debug(sourceDef, scraper, source);

        String replace = BaseTemplater.evaluateToString(elementDef.getReplace(), null, scraper);
        final boolean isReplace = CommonUtil.isBooleanTrue(replace);

        boolean flagCaseInsensitive = CommonUtil.getBooleanValue(BaseTemplater.evaluateToString(elementDef.getFlagCaseInsensitive(), null, scraper), false);
        boolean flagMultiline = CommonUtil.getBooleanValue(BaseTemplater.evaluateToString(elementDef.getFlagMultiline(), null, scraper), false);
        boolean flagDotall = CommonUtil.getBooleanValue(BaseTemplater.evaluateToString(elementDef.getFlagDotall(), null, scraper), true);
        boolean flagUnicodecase = CommonUtil.getBooleanValue(BaseTemplater.evaluateToString(elementDef.getFlagUnicodecase(), null, scraper), true);
        boolean flagCanoneq = CommonUtil.getBooleanValue(BaseTemplater.evaluateToString(elementDef.getFlagCanoneq(), null, scraper), false);

        this.setProperty("Is replacing", String.valueOf(isReplace));
        this.setProperty("Flag CaseInsensitive", String.valueOf(flagCaseInsensitive));
        this.setProperty("Flag MultiLine", String.valueOf(flagMultiline));
        this.setProperty("Flag DotAll", String.valueOf(flagDotall));
        this.setProperty("Flag UnicodeCase", String.valueOf(flagUnicodecase));
        this.setProperty("Flag CanonEq", String.valueOf(flagCanoneq));

        final double maxLoops = NumberUtils.toDouble(BaseTemplater.evaluateToString(elementDef.getMax(), null, scraper), Constants.DEFAULT_MAX_LOOPS);

        this.setProperty("Max loops", String.valueOf(maxLoops));

        int flags = 0;
        if (flagCaseInsensitive) {
            flags |= Pattern.CASE_INSENSITIVE;
        }
        if (flagMultiline) {
            flags |= Pattern.MULTILINE;
        }
        if (flagDotall) {
            flags |= Pattern.DOTALL;
        }
        if (flagUnicodecase) {
            flags |= Pattern.UNICODE_CASE;
        }
        if (flagCanoneq) {
            flags |= Pattern.CANON_EQ;
        }

        final Pattern pattern = Pattern.compile(patternVar.toString(), flags);

        final List<NodeVariable> resultList = new ArrayList<NodeVariable>();

        List bodyList = source.toList();
        for (final Object currVar : bodyList) {
            context.executeWithinNewContext(new Callable<Object>() {
                public Object call() throws InterruptedException {
                    String text = currVar.toString();

                    Matcher matcher = pattern.matcher(text);
                    int groupCount = matcher.groupCount();

                    StringBuffer buffer = new StringBuffer();

                    int index = 0;
                    while (matcher.find()) {
                        index++;

                        // if index exceeds maximum number of matching sequences exists the loop
                        if (maxLoops < index) {
                            break;
                        }

                        for (int i = 0; i <= groupCount; i++) {
                            context.setLocalVar("_" + i, new NodeVariable(matcher.group(i)));
                        }

                        ProcessorElementDef resultDef = elementDef.getRegexpResultDef();
                        Variable result = getBodyTextContent(resultDef, scraper, context, true);
                        debug(resultDef, scraper, result);

                        String currResult = (result == null) ? matcher.group(0) : result.toString();
                        if (isReplace) {
                            matcher.appendReplacement(buffer, currResult);
                        } else {
                            resultList.add(new NodeVariable(currResult));
                        }
                    }

                    if (isReplace) {
                        matcher.appendTail(buffer);
                        resultList.add(new NodeVariable(buffer.toString()));
                    }

                    return null;
                }
            });
        }


        return new ListVariable(resultList);
    }

}