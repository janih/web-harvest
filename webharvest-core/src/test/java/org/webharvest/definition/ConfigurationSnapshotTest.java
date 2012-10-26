package org.webharvest.definition;

import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertEquals;
import org.testng.annotations.Test;
import org.webharvest.runtime.scripting.ScriptingLanguage;

public class ConfigurationSnapshotTest {

    private static final String CHARSET = "ISO-8859-2";
    private static final ScriptingLanguage LANGUAGE = ScriptingLanguage.GROOVY;

    @Test
    public void getCharset() {
        final String charset = new ConfigurationSnapshot(CHARSET, LANGUAGE).
            getCharset();
        assertNotNull("Charset is null.", charset);
        assertEquals("Unexpected charset.", CHARSET, charset);
    }

    @Test
    public void getScriptingLanguage() {
        final ScriptingLanguage language =
            new ConfigurationSnapshot(CHARSET, LANGUAGE).getScriptingLanguage();
        assertNotNull("ScriptingLanguage is null.", language);
        assertEquals("Unexpected ScriptingLanguage.", LANGUAGE, language);
    }

}
