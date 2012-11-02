package org.webharvest.runtime.processors;

import static org.webharvest.WHConstants.XMLNS_CORE;
import static org.webharvest.WHConstants.XMLNS_CORE_10;

import org.webharvest.annotation.Definition;
import org.webharvest.definition.ConfigDef;
import org.webharvest.definition.ScraperConfiguration;
import org.webharvest.runtime.DynamicScopeContext;
import org.webharvest.runtime.Scraper;
import org.webharvest.runtime.processors.plugins.Autoscanned;
import org.webharvest.runtime.processors.plugins.TargetNamespace;
import org.webharvest.runtime.variables.EmptyVariable;
import org.webharvest.runtime.variables.Variable;

import com.google.inject.Inject;

/**
 * @author Robert Bala
 * @since 2.1.0-SNAPSHOT
 * @version %I%, %G%
 */
// TODO Add unit test
// TODO Add javadoc
@Autoscanned
@TargetNamespace({ XMLNS_CORE, XMLNS_CORE_10 })
@Definition(value = "config", validAttributes = { "charset", "scriptlang" },
        definitionClass = ConfigDef.class)
public final class ConfigProcessor extends AbstractProcessor<ConfigDef> {

    @Inject
    private ScraperConfiguration configuration;

    /**
     * {@inheritDoc}
     */
    @Override
    protected Variable execute(final Scraper scraper,
            final DynamicScopeContext context) throws InterruptedException {

        context.setCharset(getElementDef().getCharset());
        context.setScriptingLanguage(getElementDef().getScriptingLanguage());

        //evaluate body of config element
        getBodyTextContent(getElementDef(), scraper, context);

        return EmptyVariable.INSTANCE;
    }

}
