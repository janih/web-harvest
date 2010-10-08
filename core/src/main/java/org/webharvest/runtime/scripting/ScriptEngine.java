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
package org.webharvest.runtime.scripting;

import org.webharvest.runtime.DynamicScopeContext;
import org.webharvest.runtime.variables.InternalVariable;
import org.webharvest.runtime.variables.Variable;
import org.webharvest.utils.CommonUtil;
import org.webharvest.utils.KeyValuePair;

/**
 * Abstract scripting engine.
 */
public abstract class ScriptEngine {

    public Object evaluate(DynamicScopeContext context) {
        try {
            beforeEvaluation();

            // push all variables from context to the scripter
            for (KeyValuePair<Variable> pair : context) {
                final Variable value = pair.getValue();
                //todo: why not just unwrap every variable?
                setEngineVariable(pair.getKey(), (value instanceof InternalVariable)
                        ? value.getWrappedObject()
                        : value);
            }

            final Object result = doEvaluate();

            for (KeyValuePair<Object> pair : getEngineVariables()) {
                final String varName = pair.getKey();
                final Variable value = CommonUtil.createVariable(pair.getValue());
                if (context.containsVar(varName)) {
                    context.replaceExistingVar(varName, value);
                } else {
                    context.setLocalVar(varName, value);
                }
            }

            return result;
        } finally {
            afterEvaluation();
        }
    }

    protected abstract void beforeEvaluation();

    protected abstract void setEngineVariable(String name, Object value);

    protected abstract Object doEvaluate();

    protected abstract Iterable<KeyValuePair<Object>> getEngineVariables();

    protected abstract void afterEvaluation();
}