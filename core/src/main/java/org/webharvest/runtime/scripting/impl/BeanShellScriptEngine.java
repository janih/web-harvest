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

import bsh.*;
import org.apache.commons.collections.IteratorUtils;
import org.apache.commons.collections.Predicate;
import org.apache.commons.collections.Transformer;
import org.webharvest.exception.ScriptException;
import org.webharvest.runtime.scripting.ScriptEngine;
import org.webharvest.utils.Assert;
import org.webharvest.utils.KeyValuePair;

/**
 * BeanShell scripting engine.
 */
public class BeanShellScriptEngine extends ScriptEngine {

    final private Interpreter bshInterpreter;
    final private BshClassManager bshClassManager;

    private String sourceCode;

    private transient NameSpace bshNameSpace;

    public BeanShellScriptEngine(String sourceCode) {
        bshInterpreter = new Interpreter();
        bshClassManager = bshInterpreter.getNameSpace().getClassManager();

        // BeanShell does not support pre-compiled scripts
        this.sourceCode = sourceCode;
    }

    @Override
    protected void beforeEvaluation() {
        Assert.isNull(bshNameSpace);
        bshNameSpace = new NameSpace(bshClassManager, "WebHarvest");
        bshNameSpace.importCommands("org.webharvest.runtime.scripting");
    }

    @Override
    protected void setEngineVariable(String name, Object value) {
        try {
            bshNameSpace.setVariable(name, value, false);
        } catch (UtilEvalError e) {
            throw new ScriptException("Cannot set variable in scripter: " + e.getMessage(), e);
        }
    }

    @Override
    protected Object doEvaluate() {
        try {
            bshInterpreter.setNameSpace(bshNameSpace);
            return bshInterpreter.eval(sourceCode);
        } catch (EvalError e) {
            throw new ScriptException("Error during script execution: " + e.getMessage(), e);
        }
    }

    @Override
    @SuppressWarnings({"unchecked"})
    protected Iterable<KeyValuePair<Object>> getEngineVariables() {
        return IteratorUtils.toList(
                IteratorUtils.filteredIterator(
                        IteratorUtils.transformedIterator(
                                IteratorUtils.arrayIterator(bshNameSpace.getVariableNames()),
                                new Transformer() {
                                    @Override
                                    public KeyValuePair transform(Object input) {
                                        final String varName = (String) input;
                                        try {
                                            return new KeyValuePair(varName, bshNameSpace.getVariable(varName));
                                        } catch (UtilEvalError e) {
                                            throw new ScriptException(e);
                                        }
                                    }
                                }),
                        new Predicate() {
                            @Override
                            public boolean evaluate(Object object) {
                                return !(((KeyValuePair) object).getValue() instanceof bsh.XThis);
                            }
                        }
                ));
    }

    @Override
    protected void afterEvaluation() {
        bshNameSpace = null;
        bshInterpreter.setNameSpace(null);
    }

}