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
package org.webharvest.runtime.scripting.impl;

import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import groovy.lang.Script;
import org.apache.commons.collections.IteratorUtils;
import org.apache.commons.collections.Transformer;
import org.webharvest.runtime.scripting.ScriptEngine;
import org.webharvest.utils.Assert;
import org.webharvest.utils.KeyValuePair;

import java.util.Map;

/**
 * Groovy scripting engine.
 */
public class GroovyScriptEngine extends ScriptEngine {

    private static long scriptNameCounter;

    private static synchronized String generateScriptName() {
        return "WebHarvest.Script" + (++scriptNameCounter) + ".groovy";
    }

    private final Script grvScript;

    private transient Binding grvBinding;

    public GroovyScriptEngine(String sourceCode) {
        grvScript = new GroovyShell().parse(sourceCode, generateScriptName());
    }

    @Override
    protected void beforeEvaluation() {
        Assert.isNull(grvBinding);
        grvBinding = new Binding();
    }

    @Override
    protected void setEngineVariable(String name, Object value) {
        grvBinding.setVariable(name, value);
    }

    protected Object doEvaluate() {
        grvScript.setBinding(grvBinding);
        return grvScript.run();
    }

    @Override
    @SuppressWarnings({"unchecked"})
    protected Iterable<KeyValuePair<Object>> getEngineVariables() {
        return IteratorUtils.toList(IteratorUtils.transformedIterator(
                grvBinding.getVariables().entrySet().iterator(),
                new Transformer() {
                    @Override
                    public Object transform(Object input) {
                        final Map.Entry<String, Object> entry = (Map.Entry<String, Object>) input;
                        return new KeyValuePair(entry.getKey(), entry.getValue());
                    }
                }));
    }

    @Override
    protected void afterEvaluation() {
        grvBinding = null;
    }

}