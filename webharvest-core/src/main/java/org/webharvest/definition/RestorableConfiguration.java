package org.webharvest.definition;

/**
 * Enables possibility to capture current configuration's state and to restore
 * it later.
 *
 * @author mczapiewski
 * @since 2.1-SNAPSHOT
 * @version %I%, %G%
 */
public interface RestorableConfiguration {

    /**
     * Restores configuration's state using information contained by given
     * {@link ConfigurationSnapshot}.
     *
     * @param state
     *            an instance of {@link ConfigurationSnapshot} containing
     *            information about configuration's state
     */
    void restoreState(ConfigurationSnapshot state);

    /**
     * Captures current state of the configurations.
     *
     * @return {@link ConfigurationSnapshot} representing current state of the
     *         configurations
     */
    ConfigurationSnapshot captureState();

}
