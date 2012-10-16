package org.webharvest.ioc;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.concurrent.atomic.AtomicInteger;
import org.webharvest.definition.ScraperConfiguration;
import org.webharvest.runtime.database.ConnectionFactory;
import org.webharvest.runtime.database.StandaloneConnectionPool;
import org.xml.sax.InputSource;
import com.google.common.eventbus.EventBus;
import com.google.inject.AbstractModule;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.google.inject.Scope;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import com.google.inject.matcher.Matchers;

// TODO Add javadoc
// TODO Add unit test
public final class ScraperModule extends AbstractModule {

    private static final Scope SCRAPER_SCOPE = new ScraperScope();

    private final String workingDir;

    private final InputSource config;

    // TODO Add documentation
    // TODO Add unit test
    // FIXME rbala I'm not convinced this is good idea
    public ScraperModule(final URL config, final String workingDir)
            throws IOException {
        this(new InputSource(new InputStreamReader(config.openStream())),
                workingDir);
    }

    // TODO Add documentation
    // TODO Add unit test
    // FIXME rbala I'm not convinced this is good idea
    public ScraperModule(final String config, final String workingDir)
            throws FileNotFoundException {
        this(new InputSource(new FileReader(config)), workingDir);
    }

    // TODO Add documentation
    // TODO Add unit test
    // FIXME rbala I'm not convinced this is good idea
    public ScraperModule(final InputSource config, final String workingDir) {
        this.config = config;
        this.workingDir = workingDir;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void configure() {
        bindConstant().annotatedWith(WorkingDir.class).to(workingDir);
        bind(InputSource.class).annotatedWith(ConfigSource.class).toInstance(config);

        bindScope(ScrapingScope.class, SCRAPER_SCOPE);
        // Make our scope instance injectable
        bind(ScraperScope.class).toInstance((ScraperScope) SCRAPER_SCOPE);

        final EventBus eventBus = new EventBus();
        bind(EventBus.class).toInstance(eventBus);
        bindListener(Matchers.any(), new EventBusTypeListener());

        requestStaticInjection(EventBusTypeListener.Factory.class);


        bind(ScraperConfiguration.class).in(Singleton.class);

        bind(ConnectionFactory.class).to(StandaloneConnectionPool.class)
            .in(ScrapingScope.class);

        bind(MessagePublisher.class).to(EventBusProxy.class).
            in(Singleton.class);
    }

}
