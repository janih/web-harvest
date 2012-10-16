/*
 Copyright (c) 2006-2012 the original author or authors.

 Redistribution and use of this software in source and binary forms,
 with or without modification, are permitted provided that the following
 conditions are met:

 * Redistributions of source code must retain the above
   copyright notice, this list of conditions and the
   following disclaimer.

 * Redistributions in binary form must reproduce the above
   copyright notice, this list of conditions and the
   following disclaimer in the documentation and/or other
   materials provided with the distribution.

 * The name of Web-Harvest may not be used to endorse or promote
   products derived from this software without specific prior
   written permission.

 THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 POSSIBILITY OF SUCH DAMAGE.
 */

package org.webharvest.ioc;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

import org.webharvest.runtime.database.ConnectionFactory;
import org.webharvest.runtime.database.JNDIConnectionFactory;
import org.xml.sax.InputSource;

import com.google.inject.AbstractModule;
import com.google.inject.Binder;
import com.google.inject.Module;
import com.google.inject.Singleton;
import com.google.inject.util.Modules;

/**
 * Google Guice module configuring web harvest to work within enterprise
 * container. Module enables functionalities not available for standalone
 * applications, such as JNDI database connection lookups.
 *
 * @see com.google.inject.Guice#createInjector(Module...)
 *
 * @author Piotr Dyraga
 * @since 2.1.0-SNAPSHOT
 * @version %I%, %G%
 */
public final class EnterpriseModule implements Module {

    private final Module module;

    // TODO Add documentation
    // TODO Add unit test
    // FIXME rbala I'm not convinced this is good idea
    public EnterpriseModule(final URL config, final String workingDir)
            throws IOException {
        this(new ScraperModule(config, workingDir));
    }

    // TODO Add documentation
    // TODO Add unit test
    // FIXME rbala I'm not convinced this is good idea
    public EnterpriseModule(final String config, final String workingDir)
            throws FileNotFoundException {
        this(new ScraperModule(config, workingDir));
    }

    // TODO Add documentation
    // TODO Add unit test
    // FIXME rbala I'm not convinced this is good idea
    public EnterpriseModule(final InputSource config, final String workingDir) {
        this(new ScraperModule(config, workingDir));
    }

    // TODO Add documentation
    // TODO Add unit test
    // FIXME rbala I'm not convinced this is good idea
    private EnterpriseModule(final Module module) {
        this.module = Modules.override(module).with(new AbstractModule() {
            @Override
            protected void configure() {
                bind(ConnectionFactory.class).to(JNDIConnectionFactory.class)
                        .in(Singleton.class);
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void configure(final Binder binder) {
        module.configure(binder);
    }

}
