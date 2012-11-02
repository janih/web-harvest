package org.webharvest.definition;

import org.webharvest.runtime.scripting.ScriptingLanguage;

/**
 * Snapshot of {@link ScraperConfiguration} which contains information such as
 * default {@link ScriptingLanguage}.
 *
 * @see RestorableConfiguration
 *
 * @author mczapiewski
 * @since 2.1-SNAPSHOT
 * @version %I%, %G%
 */
public final class ConfigurationSnapshot {

    private ScriptingLanguage scriptingLanguage;

    /**
     * Default class constructor which accepts configuration's
     * default {@link ScriptingLanguage}.
     *
     * @param scriptingLanguage
     *            default {@link ScriptingLanguage} of the configuration
     */
    public ConfigurationSnapshot(final ScriptingLanguage scriptingLanguage) {
        this.scriptingLanguage = scriptingLanguage;
    }

    /**
     * Returns configuration's {@link ScriptingLanguage}.
     *
     * @return configuration's {@link ScriptingLanguage}
     */
    public ScriptingLanguage getScriptingLanguage() {
        return scriptingLanguage;
    }

}
