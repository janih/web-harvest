package org.webharvest.runtime.scripting.jsr;

import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.getCurrentArguments;
import static org.easymock.EasyMock.isA;
import static org.easymock.EasyMock.eq;
import static org.testng.AssertJUnit.assertSame;
import static org.testng.AssertJUnit.fail;

import java.util.HashSet;
import java.util.Set;

import javax.script.ScriptException;

import org.easymock.IAnswer;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.unitils.UnitilsTestNG;
import org.unitils.easymock.EasyMockUnitils;
import org.unitils.easymock.annotation.RegularMock;
import org.webharvest.runtime.DynamicScopeContext;
import org.webharvest.runtime.scripting.ScriptSource;
import org.webharvest.runtime.scripting.ScriptingLanguage;
import org.webharvest.runtime.variables.NodeVariable;
import org.webharvest.runtime.variables.Variable;
import org.webharvest.utils.KeyValuePair;

public class JSRScriptEngineAdapterTest extends UnitilsTestNG {

    private static final String SCRIPT = "def name = 'Dummy script';";

    private ScriptSource scriptSource;

    @RegularMock
    private DynamicScopeContext mockContext;

    @RegularMock
    private javax.script.ScriptEngine mockJsrEngine;

    private JSRScriptEngineAdapter adapter;

    @BeforeMethod
    public void setUp() {
        this.scriptSource = new ScriptSource(SCRIPT, ScriptingLanguage.GROOVY);
        this.adapter = new JSRScriptEngineAdapter(mockJsrEngine);
    }

    @AfterMethod
    public void tearDown() {
        this.scriptSource = null;
        this.adapter = null;
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void nullAdapteeNotAllowed() {
        new JSRScriptEngineAdapter(null);
    }

    @Test
    public void isDelegateInvoked() throws ScriptException {
        final Object adapteeResult = new Object();

        expect(mockContext.iterator()).andReturn(
                new HashSet<KeyValuePair<Variable>>().iterator());
        expect(mockJsrEngine.eval(eq(SCRIPT),
                isA(javax.script.ScriptContext.class)))
        .andReturn(adapteeResult);

        EasyMockUnitils.replay();

        final Object result = adapter.evaluate(mockContext, scriptSource);
        assertSame("Unexpected script result", adapteeResult, result);
    }

    @Test
    public void areContextVariablesCopied() throws ScriptException {
        final Object adapteeResult = new Object();
        final Capture capturedContext = new Capture();

        final Variable value1 = new NodeVariable("var1value");
        final Variable value2 = new NodeVariable("var2value");

        final Set<KeyValuePair<Variable>> variables =
            new HashSet<KeyValuePair<Variable>>();
        variables.add(new KeyValuePair<Variable>("var1name", value1));
        variables.add(new KeyValuePair<Variable>("var2name", value2));

        expect(mockContext.iterator()).andReturn(variables.iterator());
        mockJsrEngine.eval(eq(SCRIPT), isA(javax.script.ScriptContext.class));
        expectLastCall().andAnswer(new IAnswer<Object>() {
            @Override
            public Object answer() throws Throwable {
                capturedContext.setCaptured(getCurrentArguments()[1]);
                return adapteeResult;
            }
        });

        EasyMockUnitils.replay();

        final Object result = adapter.evaluate(mockContext, scriptSource);
        assertSame("Unexpected script result", adapteeResult, result);

        final javax.script.ScriptContext ctx = capturedContext.getCaptured();
        assertSame("Unexpected attribute value", value1,
                ctx.getAttribute("var1name",
                        javax.script.ScriptContext.ENGINE_SCOPE));
        assertSame("Unexpected attribute value", value2,
                ctx.getAttribute("var2name",
                        javax.script.ScriptContext.ENGINE_SCOPE));
    }

    @Test
    public void evaluateInCaseOfException() throws Exception {
        expect(mockContext.iterator()).andReturn(
                new HashSet<KeyValuePair<Variable>>().iterator());
        expect(mockJsrEngine.eval(eq(SCRIPT), isA(javax.script.ScriptContext.class)))
            .andThrow(new ScriptException("test"));

        EasyMockUnitils.replay();

        try {
            adapter.evaluate(mockContext, scriptSource);
            fail("ScriptException expected");
        } catch (org.webharvest.exception.ScriptException e) {
            // ok, it's expected
        }
    }

    // FIXME [pdyraga] Duplicate code with SpringAwareTypeListener
    private final class Capture {

        private Object captured;

        @SuppressWarnings("unchecked")
        public <T> T getCaptured() {
            return (T) captured;
        }

        private void setCaptured(final Object captured) {
            this.captured = captured;
        }
    }
}
