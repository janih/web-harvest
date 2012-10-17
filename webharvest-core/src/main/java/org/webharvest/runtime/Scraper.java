/*  Copyright (c) 2006-2007, Vladimir Nikic
    All rights reserved.

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

    You can contact Vladimir Nikic by sending e-mail to
    nikic_vladimir@yahoo.com. Please include the word "Web-Harvest" in the
    subject line.
*/
package org.webharvest.runtime;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webharvest.WHConstants;
import org.webharvest.definition.IElementDef;
import org.webharvest.definition.ScraperConfiguration;
import org.webharvest.deprecated.runtime.ScraperContext10;
import org.webharvest.events.ProcessorStartEvent;
import org.webharvest.events.ScraperExecutionEndEvent;
import org.webharvest.events.ScraperExecutionErrorEvent;
import org.webharvest.ioc.ConfigDir;
import org.webharvest.ioc.ConfigModule;
import org.webharvest.ioc.FakeNotifier;
import org.webharvest.ioc.ScraperScope;
import org.webharvest.ioc.Scraping;
import org.webharvest.ioc.WorkingDir;
import org.webharvest.runtime.database.ConnectionFactory;
import org.webharvest.runtime.processors.CallProcessor;
import org.webharvest.runtime.processors.Processor;
import org.webharvest.runtime.processors.ProcessorResolver;
import org.webharvest.runtime.scripting.ScriptEngineFactory;
import org.webharvest.runtime.scripting.jsr.JSRScriptEngineFactory;
import org.webharvest.runtime.variables.EmptyVariable;
import org.webharvest.runtime.variables.ScriptingVariable;
import org.webharvest.runtime.variables.Variable;
import org.webharvest.runtime.web.HttpClientManager;
import org.webharvest.utils.CommonUtil;
import org.webharvest.utils.Stack;
import org.webharvest.utils.SystemUtilities;
import org.xml.sax.InputSource;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.Provider;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;

/**
 * Basic runtime class.
 */
public class Scraper implements WebScraper {

    private static final Logger LOG = LoggerFactory.getLogger(Scraper.class);

    public static final int STATUS_READY = 0;
    public static final int STATUS_RUNNING = 1;
    public static final int STATUS_PAUSED = 2;
    public static final int STATUS_FINISHED = 3;
    public static final int STATUS_STOPPED = 4;
    public static final int STATUS_ERROR = 5;
    public static final int STATUS_EXIT = 6;

    @Inject
    private EventBus eventBus;

    private ScraperConfiguration configuration;
    private String workingDir;
    private final DynamicScopeContext context;
    private ScriptEngineFactory scriptEngineFactory;

    // FIXME: [pdyraga] It would be neat to decouple Scraper from
    // ConnectionFactory. In order to achieve it, we need scoped dependency
    // injection to work with processors (database plugin). Since currently
    // we can't provide this functionality, we are not able to get rid of
    // ConnectionFactory from here.
    @Inject
    private Provider<ConnectionFactory> connectionFactory;

    private RuntimeConfig runtimeConfig;

    private transient boolean isDebugMode = false;

    private HttpClientManager httpClientManager;

    // stack of running processors
    private transient Stack<Processor> runningProcessors = new Stack<Processor>();

    // stack of running functions
    private transient Stack<CallProcessor> runningFunctions = new Stack<CallProcessor>();

    // params that are proceeded to calling function
    private transient Map<String, Variable> functionParams = new HashMap<String, Variable>();

    private List<ScraperRuntimeListener> scraperRuntimeListeners = new LinkedList<ScraperRuntimeListener>();

    private volatile int status = STATUS_READY;

    private String message = null;

    @Inject
    private Provider<FakeNotifier> notifier;

    private @Inject ScraperScope scope;

    // TODO Missing documentation
    // TODO Missing unit test
    // FIXME rbala temporary solution?
    @AssistedInject
    public Scraper(final Injector injector, @WorkingDir final String workingDir,
            @ConfigDir final String configDir, @Assisted final URL config)
                throws IOException {
        this(new InjectorHelper(injector, config, configDir), workingDir);
    }

    // TODO Missing documentation
    // TODO Missing unit test
    // FIXME rbala temporary solution?
    @AssistedInject
    public Scraper(final Injector injector, @WorkingDir final String workingDir,
            @ConfigDir final String configDir, @Assisted final String config)
                throws FileNotFoundException {
        this(new InjectorHelper(injector, config, configDir), workingDir);
    }

    // TODO Missing documentation
    // TODO Missing unit test
    // FIXME rbala temporary solution?
    @AssistedInject
    public Scraper(final Injector injector, @WorkingDir final String workingDir,
            @ConfigDir final String configDir,
            @Assisted final InputSource config) {
        this(new InjectorHelper(injector, config, configDir), workingDir);
    }

    // TODO Missing documentation
    // TODO Missing unit test
    // FIXME rbala temporary solution?
    private Scraper(final InjectorHelper injector, final String workingDir) {
        this(injector.getConfig(), workingDir);
    }

    final static class InjectorHelper {

        private final Injector injector;

        private final String configDir;

        public InjectorHelper(final Injector injector, final URL config,
                final String configDir) throws IOException {
            this(injector, new ConfigModule(config), configDir);
        }

        public InjectorHelper(final Injector injector, final String config,
                final String configDir) throws FileNotFoundException {
            this(injector, new ConfigModule(config), configDir);
        }

        public InjectorHelper(final Injector injector,
                final InputSource config, final String configDir) {
            this(injector, new ConfigModule(config), configDir);
        }

        private InjectorHelper(final Injector injector, final Module module,
                final String configDir) {
            this.injector = injector.createChildInjector(module);
            this.configDir = configDir;
        }

        public ScraperConfiguration getConfig() {
            final ScraperConfiguration config = injector.
                getInstance(ScraperConfiguration.class);
            if (configDir != null) {
                config.setSourceFile(new File(configDir));
            }

            return config;
        }

    }

    /**
     * Constructor.
     *
     * @param configuration
     * @param workingDir
     * @deprecated as public constructor make it private.
     */
    @Deprecated
    public Scraper(final ScraperConfiguration configuration,
            final String workingDir) {
        this.configuration = configuration;
        this.runtimeConfig = new RuntimeConfig();
        this.workingDir = CommonUtil.adaptFilename(workingDir);

        this.httpClientManager = new HttpClientManager();

        this.context = WHConstants.XMLNS_CORE_10.equals(configuration.getNamespaceURI())
                ? new ScraperContext10("sys", "http")
                : new ScraperContext(this);

        this.scriptEngineFactory = new JSRScriptEngineFactory(
                configuration.getScriptingLanguage());
    }

    @PostConstruct
    public void initContext() {
        this.context.setLocalVar("sys", new ScriptingVariable(
                new SystemUtilities(this)));
        this.context.setLocalVar("http", new ScriptingVariable(
                httpClientManager.getHttpInfo()));
    }

    /**
     * Adds parameter with specified name and value to the context.
     * This way some predefined variables can be put in runtime context
     * before execution starts.
     *
     * @param name
     * @param value
     * @deprecated Use {@link DynamicScopeContext#setLocalVar(String, Object)} instead
     */
    @Deprecated
    public void addVariableToContext(String name, Object value) {
        this.context.setLocalVar(name, CommonUtil.createVariable(value));
    }

    /**
     * Add all map values to the context.
     *
     * @param map
     * @deprecated Use {@link DynamicScopeContext#setLocalVar(Map)} instead.
     */
    @Deprecated
    public void addVariablesToContext(Map<String, Object> map) {
        if (map != null) {
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                this.context.setLocalVar(entry.getKey(), CommonUtil.createVariable(entry.getValue()));
            }
        }
    }

    // TODO rbala Make it private. Currently used only by IncludeProcessor
    public Variable execute(List<IElementDef> ops) {
        this.setStatus(STATUS_RUNNING);

        // inform al listeners that execution is just about to start
        for (ScraperRuntimeListener listener : scraperRuntimeListeners) {
            listener.onExecutionStart(this);
        }

        try {
            for (IElementDef elementDef : ops) {
                Processor processor = ProcessorResolver.createProcessor(elementDef);
                if (processor != null) {
                    processor.run(this, context);
                }
            }
        } catch (InterruptedException e) {
            setStatus(STATUS_STOPPED);
            Thread.currentThread().interrupt();
        }

        return EmptyVariable.INSTANCE;
    }

    @Scraping
    public void execute() {
        /*
        scope.enter(this);
        try {

        */

            notifier.get().sendEvent();


            executeInternal();
            /*
        } finally {
            scope.exit();
        }
        */
    }

    private void executeInternal() {
        long startTime = System.currentTimeMillis();

        execute(configuration.getOperations());

        if (this.status == STATUS_RUNNING) {
            this.setStatus(STATUS_FINISHED);
        }

        // inform al listeners that execution is finished
        for (ScraperRuntimeListener listener : scraperRuntimeListeners) {
            listener.onExecutionEnd(this);
        }
        eventBus.post(new ScraperExecutionEndEvent(this));

        if (LOG.isInfoEnabled()) {
            if (this.status == STATUS_FINISHED) {
                LOG.info("Configuration executed in {} ms.",
                        (System.currentTimeMillis() - startTime));
            } else if (this.status == STATUS_STOPPED) {
                LOG.info("Configuration stopped!");
            }
        }
    }

    public DynamicScopeContext getContext() {
        return context;
    }

    public ScraperConfiguration getConfiguration() {
        return configuration;
    }

    public String getWorkingDir() {
        return this.workingDir;
    }

    public HttpClientManager getHttpClientManager() {
        return httpClientManager;
    }

    public void addRunningFunction(CallProcessor callProcessor) {
        runningFunctions.push(callProcessor);
    }

    public CallProcessor getRunningFunction() {
        return runningFunctions.isEmpty() ? null : runningFunctions.peek();
    }

    public void removeRunningFunction() {
        runningFunctions.pop();
    }

    public void addFunctionParam(String name, Variable value) {
        this.functionParams.put(name, value);
    }

    public Map<String, Variable> getFunctionParams() {
        return functionParams;
    }

    public void clearFunctionParams() {
        this.functionParams.clear();
    }

    public int getRunningLevel() {
        return runningProcessors.size() + 1;
    }

    public boolean isDebugMode() {
        return isDebugMode;
    }

    public void setDebug(boolean debug) {
        this.isDebugMode = debug;
    }

    /**
     * @param processorClazz
     *            Class of enclosing running processor.
     * @return Parent running processor in the tree of specified class, or null
     *         if it doesn't exist.
     */
    public final <T extends Processor< ? >> T getRunningProcessorOfType(
            final Class<T> processorClazz) {
        List<Processor> runningProcessorList = runningProcessors.getList();
        ListIterator<Processor> listIterator = runningProcessorList.listIterator(runningProcessors.size());
        while (listIterator.hasPrevious()) {
            final Processor< ? > curr = listIterator.previous();
            if (processorClazz.equals(curr.getClass())) {
                return processorClazz.cast(curr);
            }
        }
        return null;
    }

    public RuntimeConfig getRuntimeConfig() {
        return runtimeConfig;
    }

    /**
     * @deprecated It would be neat to decouple Scraper from ConnectionFactory.
     *             In order to achieve it, we need scoped dependency injection
     *             to work with processors (database plugin). Since currently we
     *             can't provide this functionality, we are not able to get rid
     *             of ConnectionFactory from here.
     */
    @Deprecated
    public ConnectionFactory getConnectionFactory() {
        return this.connectionFactory.get();
    }

    public void setExecutingProcessor(Processor processor) {
        runningProcessors.push(processor);
        for (ScraperRuntimeListener listener : scraperRuntimeListeners) {
            listener.onNewProcessorExecution(this, processor);
        }
    }

    public void finishExecutingProcessor() {
        this.runningProcessors.pop();
    }

    public void processorFinishedExecution(final Processor processor, Map properties) {
        for (ScraperRuntimeListener listener : scraperRuntimeListeners) {
            listener.onProcessorExecutionFinished(this, processor, properties);
        }
    }

    public void addRuntimeListener(ScraperRuntimeListener listener) {
        this.scraperRuntimeListeners.add(listener);
    }

    public void removeRuntimeListener(ScraperRuntimeListener listener) {
        this.scraperRuntimeListeners.remove(listener);
    }

    public synchronized int getStatus() {
        return status;
    }

    private synchronized void setStatus(int status) {
        this.status = status;
    }

    public void stopExecution() {
        setStatus(STATUS_STOPPED);
    }

    public void exitExecution(String message) {
        setStatus(STATUS_EXIT);
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void pauseExecution() {
        if (this.status == STATUS_RUNNING) {
            setStatus(STATUS_PAUSED);

            // inform al listeners that execution is paused
            for (ScraperRuntimeListener listener : scraperRuntimeListeners) {
                listener.onExecutionPaused(this);
            }
        }
    }

    public void continueExecution() {
        if (this.status == STATUS_PAUSED) {
            setStatus(STATUS_RUNNING);

            // inform al listeners that execution is continued
            for (ScraperRuntimeListener listener : scraperRuntimeListeners) {
                listener.onExecutionContinued(this);
            }
        }
    }

    /**
     * Inform all scraper listeners that an error has occured during scraper execution.
     */
    public void informListenersAboutError(Exception e) {
        setStatus(STATUS_ERROR);

        // inform al listeners that execution is continued
        for (ScraperRuntimeListener listener : scraperRuntimeListeners) {
            listener.onExecutionError(this, e);
        }
        eventBus.post(new ScraperExecutionErrorEvent(this, e));
    }

    public ScriptEngineFactory getScriptEngineFactory() {
        return scriptEngineFactory;
    }


    @Subscribe
    public void handle(final ProcessorStartEvent event) {
        LOG.info("Received en event!!! {} {}", this, event);
    }

}
