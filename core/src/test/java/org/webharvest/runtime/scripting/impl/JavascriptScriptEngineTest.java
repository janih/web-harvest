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

package org.webharvest.runtime.scripting.impl;

import org.testng.annotations.Test;
import org.webharvest.runtime.ScraperContext;
import org.webharvest.runtime.variables.NodeVariable;
import org.webharvest.runtime.variables.ScriptingVariable;
import org.webharvest.runtime.variables.Variable;
import org.webharvest.utils.SystemUtilities;

import static org.testng.Assert.*;
import static org.unitils.reflectionassert.ReflectionAssert.assertReflectionEquals;

/**
 * Created by IntelliJ IDEA.
 * User: awajda
 * Date: Sep 26, 2010
 * Time: 10:41:09 PM
 */
public class JavascriptScriptEngineTest {

    private ScraperContext context = new ScraperContext();

    final Variable x = new NodeVariable(2);
    final Variable y = new NodeVariable(5);

    @Test
    public void testEvaluate() {
        context.setLocalVar("sys", new ScriptingVariable(new SystemUtilities(null)));
        context.setLocalVar("x", x);
        context.setLocalVar("y", y);
        context.setLocalVar("z", "old");
        context.setLocalVar("w", "old");

        context.executeWithinNewContext(new Runnable() {
            @Override
            public void run() {
                assertEquals(7.0, new JavascriptScriptEngine("" +
                        "function f(a, b) {return a + b};" +
                        "k = 'foo' + sys.space + 'bar';" +
                        "z = 'new';" +
                        "var w = 'new';" +
                        "f(parseInt(x), parseInt(y))").
                        evaluate(context));

                // 'x' and 'y' should remain untouched
                assertSame(x, context.getVar("x"));
                assertSame(y, context.getVar("y"));

                // new variable 'k' is defined in the local scope
                assertReflectionEquals(new ScriptingVariable("foo bar"), context.getVar("k"));

                // 'z' and 'w' are reassigned with new values in their original scope
                assertReflectionEquals(new ScriptingVariable("new"), context.getVar("z"));
                assertReflectionEquals(new ScriptingVariable("new"), context.getVar("w"));

                // function 'f' doesn't propagated out of the script
                assertFalse(context.containsVar("f"));
            }
        }, false);

        // 'k' is out of scope
        assertFalse(context.containsVar("k"));

        // 'z' and 'w' have been reassigned in this scope
        assertReflectionEquals(new ScriptingVariable("new"), context.getVar("z"));
        assertReflectionEquals(new ScriptingVariable("new"), context.getVar("w"));

        // Scripting variables are of the same type while passing over
        assertReflectionEquals(
                new ScriptingVariable(new SystemUtilities(null)),
                context.getVar("sys"));
    }

}
