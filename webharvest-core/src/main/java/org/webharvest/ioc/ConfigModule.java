package org.webharvest.ioc;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

import org.webharvest.definition.ScraperConfiguration;
import org.webharvest.runtime.RuntimeConfig;
import org.webharvest.runtime.scripting.ScriptEngineFactory;
import org.webharvest.runtime.scripting.jsr.JSRScriptEngineFactory;
import org.webharvest.runtime.templaters.BaseTemplater;
import org.xml.sax.InputSource;

import com.google.inject.AbstractModule;
import com.google.inject.Singleton;

// TODO Add javadoc
// TODO Add unit test
public final class ConfigModule extends AbstractModule {

    private final InputSource config;

    // TODO Add documentation
    // TODO Add unit test
    // FIXME rbala I'm not convinced this is good idea
    public ConfigModule(final URL config) throws IOException {
        this(new InputSource(new InputStreamReader(config.openStream())));
    }

    // TODO Add documentation
    // TODO Add unit test
    // FIXME rbala I'm not convinced this is good idea
    public ConfigModule(final String config) throws FileNotFoundException {
        this(new InputSource(new FileReader(config)));
    }

    // TODO Add documentation
    // TODO Add unit test
    // FIXME rbala I'm not convinced this is good idea
    public ConfigModule(final InputSource config) {
        this.config = config;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void configure() {
        bind(InputSource.class).annotatedWith(ConfigSource.class).toInstance(
                config);
        bind(ScraperConfiguration.class).in(Singleton.class);
        bind(ScriptEngineFactory.class).to(JSRScriptEngineFactory.class).in(
                Singleton.class);

        // FIXME: This is a dirty trick replacing in InjectorHelper
        // ScraperModule's injector by injector for ConfigModule which is a
        // child module.
        requestStaticInjection(InjectorHelper.class);

        requestStaticInjection(BaseTemplater.class);

        bind(RuntimeConfig.class).in(Singleton.class);
    }

}
