package org.webharvest.definition;

import org.webharvest.runtime.scripting.ScriptingLanguage;

/**
 * Snapshot of {@link ScraperConfiguration} which contains information such as
 * configuration's charset and its default {@link ScriptingLanguage}.
 *
 * @see RestorableConfiguration
 *
 * @author mczapiewski
 * @since 2.1-SNAPSHOT
 * @version %I%, %G%
 */
public final class ConfigurationSnapshot {

    private String charset;
    private ScriptingLanguage scriptingLanguage;

    /**
     * Default class constructor which accepts configuration's charset and
     * default {@link ScriptingLanguage}.
     *
     * @param charset
     *            configuration's charset
     * @param scriptingLanguage
     *            default {@link ScriptingLanguage} of the configuration
     */
    public ConfigurationSnapshot(final String charset,
            final ScriptingLanguage scriptingLanguage) {
        this.charset = charset;
        this.scriptingLanguage = scriptingLanguage;
    }

    /**
     * Returns configuration's charset.
     *
     * @return configuration's charset
     */
    public String getCharset() {
        return charset;
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
