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

import org.junit.Assert;
import org.junit.Test;
import org.webharvest.runtime.ScraperContext;
import org.webharvest.runtime.variables.InternalVariable;
import org.webharvest.utils.SystemUtilities;

/**
 * Created by IntelliJ IDEA.
 * User: awajda
 * Date: Sep 26, 2010
 * Time: 10:50:12 PM
 */
public class BeanShellScriptEngineTest {

    private ScraperContext context = new ScraperContext();

    @Test
    public void testEvaluate() {
        context.setLocalVar("internal", new InternalVariable(new SystemUtilities(null)));
        context.setLocalVar("x", 2);
        context.setLocalVar("y", 5);
        context.setLocalVar("z", "old");
        context.setLocalVar("w", "old");

        context.executeWithinNewContext(new Runnable() {
            @Override
            public void run() {
                Assert.assertEquals(7, new BeanShellScriptEngine("" +
                        "int f(int a, int b) {return a + b;}\n" +
                        "k = \"foo\";" +
                        "z = \"new\";" +
                        "String w = \"new\";" +
                        "f(x.getWrappedObject(), y.getWrappedObject())").
                        evaluate(context));

                Assert.assertEquals(2, context.getVar("x").getWrappedObject());
                Assert.assertEquals(5, context.getVar("y").getWrappedObject());
                Assert.assertEquals("foo", context.getVar("k").getWrappedObject());
                Assert.assertEquals("new", context.getVar("z").getWrappedObject());
                Assert.assertEquals("new", context.getVar("w").getWrappedObject());
                Assert.assertFalse(context.containsVar("f"));
                Assert.assertFalse(context.containsVar("bsh"));
            }
        }, false);

        Assert.assertFalse(context.containsVar("k"));
        Assert.assertEquals("new", context.getVar("z").getWrappedObject());
        Assert.assertSame(InternalVariable.class, context.getVar("internal").getClass());
    }

}
