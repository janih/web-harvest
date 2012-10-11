package org.webharvest.runtime.processors.plugins.db;

import static org.webharvest.WHConstants.XMLNS_CORE;
import static org.webharvest.WHConstants.XMLNS_CORE_10;

import java.sql.Connection;
import java.sql.SQLException;

import org.webharvest.annotation.Definition;
import org.webharvest.exception.DatabaseException;
import org.webharvest.runtime.Scraper;
import org.webharvest.runtime.processors.plugins.Autoscanned;
import org.webharvest.runtime.processors.plugins.TargetNamespace;

/**
 * Support for database operations.
 */
@Autoscanned
@TargetNamespace({ XMLNS_CORE, XMLNS_CORE_10 })
@Definition(value = "database", validAttributes = {
        "connection", "jdbcclass", "username", "password", "autocommit" })
public final class DatabasePlugin extends AbstractDatabasePlugin {

    /**
     * {@inheritDoc}
     */
    @Override
    protected Connection obtainConnection(final Scraper scraper) {
        final String jdbc = evaluateAttribute("jdbcclass", scraper);
        final String connection = evaluateAttribute("connection", scraper);
        final String username = evaluateAttribute("username", scraper);
        final String password = evaluateAttribute("password", scraper);
        final boolean isAutoCommit = evaluateAttributeAsBoolean("autocommit",
                true, scraper);

        final Connection conn = scraper.getConnectionFactory().getConnection(
                jdbc, connection, username, password);
        try {
            conn.setAutoCommit(isAutoCommit);
        } catch (SQLException cause) {
            throw new DatabaseException(
                    "Cannot set connection autocommit mode", cause);
        }
        return conn;
    }

    public String[] getValidAttributes() {
        return new String[] {"jdbcclass", "connection", "username",
                "password", "autocommit" };
    }

    public String[] getRequiredAttributes() {
        return new String[] {"jdbcclass", "connection"};
    }

    public String[] getAttributeValueSuggestions(String attributeName) {
        if ("output".equalsIgnoreCase(attributeName)) {
            return new String[] {"text", "xml"};
        } else if ("autocommit".equalsIgnoreCase(attributeName)) {
            return new String[] {"true", "false"};
        }
        return null;
    }
}
