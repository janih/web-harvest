package org.webharvest.runtime.scripting.jsr;

import static org.testng.AssertJUnit.*;

import org.apache.tools.ant.filters.StringInputStream;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.webharvest.definition.ScraperConfiguration;
import org.webharvest.runtime.scripting.ScriptEngine;
import org.webharvest.runtime.scripting.ScriptSource;
import org.webharvest.runtime.scripting.ScriptingLanguage;
import org.xml.sax.InputSource;

// We can't do more as testing if engines for all scripting languages declared
// in web harvest documentation as supported are accessible...
public class JSRScriptEngineFactoryTest {

    private static final String DUMMY_CONFIG = "<config " +
            "xmlns='http://web-harvest.sourceforge.net/schema/1.0/config' " +
            "xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance' " +
            "xsi:schemaLocation='http://web-harvest.sourceforge.net/schema/1.0/config config.xsd'>" +
            "<empty/>" +
            "</config>";

    private ScraperConfiguration configuration;

    private JSRScriptEngineFactory factory;

    @BeforeMethod
    public void setUp() {
        this.configuration = new ScraperConfiguration(
                new InputSource(new StringInputStream(DUMMY_CONFIG)));
        this.configuration.setScriptingLanguage(ScriptingLanguage.BEANSHELL);

        this.factory = new JSRScriptEngineFactory(this.configuration);
    }

    @AfterMethod
    public void tearDown() {
        this.configuration = null;
        this.factory = null;
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void cannotConstructWithoutConfiguration() {
        new JSRScriptEngineFactory(null);
    }

    @Test
    public void getJSRBeanshellEngine() {
        final ScriptEngine engine = factory.getEngine(
                new ScriptSource("a = 2", ScriptingLanguage.BEANSHELL));
        assertNotNull("Null script engine", engine);
    }

    @Test
    public void getGroovyEngine() {
        final ScriptEngine engine = factory.getEngine(new ScriptSource(
                "def name = 'mashup'", ScriptingLanguage.GROOVY));
        assertNotNull("Null script engine", engine);
    }

    @Test
    public void getJavascriptEngine() {
        final ScriptEngine engine = factory.getEngine(new ScriptSource(
                "var x = 123", ScriptingLanguage.JAVASCRIPT));
        assertNotNull("Null script engine", engine);
    }

    @Test
    public void getDefaultEngine() {
        final ScriptEngine engine = factory.getEngine(
                new ScriptSource("b = 1", null));
        assertNotNull("Null script engine", engine);
    }

}
