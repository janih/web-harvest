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

package org.webharvest.runtime.scripting;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.unitils.UnitilsTestNG;
import org.unitils.mock.annotation.Dummy;
import org.webharvest.runtime.DynamicScopeContext;
import org.webharvest.runtime.Scraper;
import org.webharvest.runtime.ScraperContext;
import org.webharvest.runtime.ScraperContextHolder;
import org.webharvest.runtime.scripting.impl.BeanShellScriptEngine;
import org.webharvest.runtime.scripting.impl.GroovyScriptEngine;
import org.webharvest.runtime.scripting.impl.JavascriptScriptEngine;

import java.text.MessageFormat;

import static org.testng.Assert.assertNotSame;
import static org.testng.Assert.assertSame;
import static org.webharvest.runtime.scripting.ScriptingLanguage.*;

/**
 * Created by IntelliJ IDEA.
 * User: awajda
 * Date: Sep 26, 2010
 * Time: 7:24:56 PM
 */
public class ScriptEngineFactoryTest extends UnitilsTestNG {

    final Logger log = LoggerFactory.getLogger(getClass());

    @Dummy
    Scraper scraper;

    DynamicScopeContext context;

    ScriptEngineFactory factory;

    @BeforeMethod
    public void before() {
        context = new ScraperContext(scraper);
        ScraperContextHolder.init(context);
        factory = new ScriptEngineFactory(null, context);
    }

    @AfterMethod
    public void after() {
        ScraperContextHolder.clear();
    }

    @Test
    public void testGetEngine() {
        assertSame(factory.getEngine(new ScriptSource("dummy", BEANSHELL)).getClass(), BeanShellScriptEngine.class);
        assertSame(factory.getEngine(new ScriptSource("dummy", JAVASCRIPT)).getClass(), JavascriptScriptEngine.class);
        assertSame(factory.getEngine(new ScriptSource("dummy", GROOVY)).getClass(), GroovyScriptEngine.class);
    }

    @Test
    public void testGetEngine_defaultLanguage() {
        assertSame(new ScriptEngineFactory(GROOVY, context).getEngine(new ScriptSource("dummy", null)).getClass(), GroovyScriptEngine.class);
        assertSame(new ScriptEngineFactory(BEANSHELL, context).getEngine(new ScriptSource("dummy", null)).getClass(), BeanShellScriptEngine.class);
        assertSame(new ScriptEngineFactory(JAVASCRIPT, context).getEngine(new ScriptSource("dummy", null)).getClass(), JavascriptScriptEngine.class);
    }

    @Test
    public void testGetEngine_cached() {
        final ScriptEngine bshEngine1 = factory.getEngine(new ScriptSource("dummy1", BEANSHELL));
        final ScriptEngine jsEngine1 = factory.getEngine(new ScriptSource("dummy1", JAVASCRIPT));

        final ScriptEngine bshEngine2 = factory.getEngine(new ScriptSource("dummy2", BEANSHELL));
        final ScriptEngine jsEngine2 = factory.getEngine(new ScriptSource("dummy2", JAVASCRIPT));

        assertNotSame(jsEngine1, bshEngine1);
        assertNotSame(jsEngine2, bshEngine2);
        assertNotSame(bshEngine2, bshEngine1);
        assertNotSame(jsEngine2, jsEngine1);

        assertSame(factory.getEngine(new ScriptSource("dummy1", BEANSHELL)), bshEngine1);
        assertSame(factory.getEngine(new ScriptSource("dummy1", JAVASCRIPT)), jsEngine1);
        assertSame(factory.getEngine(new ScriptSource("dummy2", BEANSHELL)), bshEngine2);
        assertSame(factory.getEngine(new ScriptSource("dummy2", JAVASCRIPT)), jsEngine2);
    }

    @Test
    public void bulkTest() {
        runBulkTest(2000, "def f = {a, b -> a * b}; f(x.toInt(), y.toInt())", GROOVY);

        runBulkTest(2000, "function f(a, b) {return a * b} f(parseInt(x), parseInt(y))", JAVASCRIPT);

        factory.getEngine(new ScriptSource("int f(int a, int b) {return a * b;}", BEANSHELL)).evaluate(context);
        runBulkTest(2000, "f(x.getWrappedObject(), y.getWrappedObject())", BEANSHELL);
    }

    private void runBulkTest(int count, String code, ScriptingLanguage lang) {
        StringBuilder msg = new StringBuilder();

        msg.append(MessageFormat.format("{0}: {1} cycles", StringUtils.rightPad(lang.name(), 10), count));

        final ScraperContext context = new ScraperContext(scraper);
        context.setLocalVar("x", 2);

        final StopWatch watch = new StopWatch();
        watch.start();
        for (int i = 0; i < count; i++) {
            context.setLocalVar("y", i);
            Assert.assertEquals((Object) ((Number) factory.getEngine(new ScriptSource(code, lang)).evaluate(context)).intValue(), 2 * i);
        }
        watch.stop();

        msg.append(MessageFormat.format("\t=> {0}", watch.toString()));

        log.info(msg.toString());
    }
}
