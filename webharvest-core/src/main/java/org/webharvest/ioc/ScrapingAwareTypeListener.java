package org.webharvest.ioc;

import org.webharvest.ScrapingAware;
import org.webharvest.ioc.ScrapingInterceptor.ScrapingAwareHelper;

import com.google.inject.Provider;
import com.google.inject.TypeLiteral;
import com.google.inject.spi.InjectionListener;
import com.google.inject.spi.TypeEncounter;
import com.google.inject.spi.TypeListener;

public final class ScrapingAwareTypeListener implements TypeListener {

    /**
     * {@inheritDoc}
     */
    @Override
    public <I> void hear(final TypeLiteral<I> type,
            final TypeEncounter<I> encounter) {
        final Provider<ScrapingAwareHelper> provider = encounter
                .getProvider(ScrapingAwareHelper.class);

        encounter.register(new InjectionListener<I>() {

            @Override
            public void afterInjection(I i) {
                if (i instanceof ScrapingAware) {
                    provider.get().addListener((ScrapingAware) i);
                }
            }
        });
    }

}
