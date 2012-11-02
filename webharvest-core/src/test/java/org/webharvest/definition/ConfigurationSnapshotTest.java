package org.webharvest.definition;

import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertEquals;
import org.testng.annotations.Test;
import org.webharvest.runtime.scripting.ScriptingLanguage;

public class ConfigurationSnapshotTest {

    private static final ScriptingLanguage LANGUAGE = ScriptingLanguage.GROOVY;

    @Test
    public void getScriptingLanguage() {
        final ScriptingLanguage language =
            new ConfigurationSnapshot(LANGUAGE).getScriptingLanguage();
        assertNotNull("ScriptingLanguage is null.", language);
        assertEquals("Unexpected ScriptingLanguage.", LANGUAGE, language);
    }

}
