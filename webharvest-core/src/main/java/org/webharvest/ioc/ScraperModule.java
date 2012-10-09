package org.webharvest.ioc;

import com.google.inject.AbstractModule;
import com.google.inject.assistedinject.FactoryModuleBuilder;

// TODO Add javadoc
// TODO Add unit test
public final class ScraperModule extends AbstractModule {

    /**
     * {@inheritDoc}
     */
    @Override
    protected void configure() {
        install(new FactoryModuleBuilder().
                build(ScraperFactory.class));
    }

}
