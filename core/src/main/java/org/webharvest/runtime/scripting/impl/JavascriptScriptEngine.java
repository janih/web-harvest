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

import org.mozilla.javascript.*;
import org.webharvest.runtime.scripting.ScriptEngine;
import org.webharvest.utils.Assert;

/**
 * javascript scripting engine based on Rhino.
 */
public class JavascriptScriptEngine extends ScriptEngine {

    private final Script jsScript;

    private transient Context jsContext;
    private transient ScriptableObject jsScope;

    public JavascriptScriptEngine(final String sourceCode) {
        jsScript = (Script) ContextFactory.getGlobal().call(new ContextAction() {
            @Override
            public Object run(Context cx) {
                return cx.compileString(sourceCode, "JavaScript", 1, null);
            }
        });
    }

    @Override
    protected void beforeEvaluation() {
        Assert.isNull(jsContext);
        Assert.isNull(jsScope);
        jsContext = Context.enter();
        jsScope = jsContext.initStandardObjects();
    }

    @Override
    protected void setVariable(String name, Object value) {
        ScriptableObject.putProperty(jsScope, name, Context.javaToJS(value, jsScope));
    }

    @Override
    protected Object doEvaluate() {
        final Object result = jsScript.exec(jsContext, jsScope);
        return (result instanceof Wrapper) ? ((Wrapper) result).unwrap() : result;
    }

    @Override
    protected void afterEvaluation() {
        Context.exit();
        jsContext = null;
        jsScope = null;
    }

}