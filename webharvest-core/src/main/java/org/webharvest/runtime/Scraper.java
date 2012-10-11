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

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webharvest.WHConstants;
import org.webharvest.definition.IElementDef;
import org.webharvest.definition.ScraperConfiguration;
import org.webharvest.deprecated.runtime.ScraperContext10;
import org.webharvest.events.ProcessorStartEvent;
import org.webharvest.ioc.AttributeHolder;
import org.webharvest.ioc.ScraperScope;
import org.webharvest.runtime.database.ConnectionFactory;
import org.webharvest.runtime.database.StandaloneConnectionPool;
import org.webharvest.runtime.processors.CallProcessor;
import org.webharvest.runtime.processors.HttpProcessor;
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

import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.name.Named;

/**
 * Basic runtime class.
 */
public class Scraper implements AttributeHolder {

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
    private DynamicScopeContext context;
    private ScriptEngineFactory scriptEngineFactory;

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

    // stack of running http processors
    private transient Stack<HttpProcessor> runningHttpProcessors = new Stack<HttpProcessor>();

    private List<ScraperRuntimeListener> scraperRuntimeListeners = new LinkedList<ScraperRuntimeListener>();

    private volatile int status = STATUS_READY;

    private String message = null;

    private final Map<Object, Object> attributes = new HashMap<Object, Object>();


    @Inject @Named("scraperScope") ScraperScope scope;

    /**
     * Constructor.
     *
     * @param configuration
     * @param workingDir
     */
    @Inject
    public Scraper(@Assisted ScraperConfiguration configuration,
            @Assisted String workingDir) {
        this.configuration = configuration;
        this.runtimeConfig = new RuntimeConfig();
        this.workingDir = CommonUtil.adaptFilename(workingDir);

        this.httpClientManager = new HttpClientManager();

        this.context = WHConstants.XMLNS_CORE_10.equals(configuration.getNamespaceURI())
                ? new ScraperContext10(this)
                : new ScraperContext(this);

        initContext(context, this);

        this.scriptEngineFactory = new JSRScriptEngineFactory(
                configuration.getScriptingLanguage());

        //this.connectionFactory = createDatabaseConnectionFactory();
    }

    protected ConnectionFactory createDatabaseConnectionFactory() {
        // return new JNDIConnectionFactory();
        final StandaloneConnectionPool pool = new StandaloneConnectionPool();
        addRuntimeListener(pool);
        return pool;
    }

    public static void initContext(DynamicScopeContext context, Scraper scraper) {
        context.setLocalVar("sys", new ScriptingVariable(new SystemUtilities(scraper)));
        context.setLocalVar("http", new ScriptingVariable(scraper.getHttpClientManager().getHttpInfo()));
    }

    /**
     * Adds parameter with specified name and value to the context.
     * This way some predefined variables can be put in runtime context
     * before execution starts.
     *
     * @param name
     * @param value
     */
    public void addVariableToContext(String name, Object value) {
        this.context.setLocalVar(name, CommonUtil.createVariable(value));
    }

    /**
     * Add all map values to the context.
     *
     * @param map
     */
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

    public void execute() {
        scope.enter(this);
        try {
            executeInternal();
        } finally {
            scope.exit();
        }
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

    public HttpProcessor getRunningHttpProcessor() {
        return runningHttpProcessors.peek();
    }

    public void setRunningHttpProcessor(HttpProcessor httpProcessor) {
        runningHttpProcessors.push(httpProcessor);
    }

    public void removeRunningHttpProcessor() {
        runningHttpProcessors.pop();
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
     * @param processorClazz Class of enclosing running processor.
     * @return Parent running processor in the tree of specified class, or null if it doesn't exist.
     */
    public Processor getRunningProcessorOfType(Class processorClazz) {
        List<Processor> runningProcessorList = runningProcessors.getList();
        ListIterator<Processor> listIterator = runningProcessorList.listIterator(runningProcessors.size());
        while (listIterator.hasPrevious()) {
            Processor curr = listIterator.previous();
            if (processorClazz.equals(curr.getClass())) {
                return curr;
            }
        }
        return null;
    }

    public RuntimeConfig getRuntimeConfig() {
        return runtimeConfig;
    }

    public ConnectionFactory getConnectionFactory() {
        return this.connectionFactory.get() ;
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
    }

    public ScriptEngineFactory getScriptEngineFactory() {
        return scriptEngineFactory;
    }


    private void handleProcessorStartEvent(final ProcessorStartEvent event) {
        // TODO Provide iplementation
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object getAttribute(final Object key) {
        return attributes.get(key);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasAttribute(final Object key) {
        return attributes.containsKey(key);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void putAttribute(final Object key, final Object value) {
        attributes.put(key, value);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<Object> getAttributes() {
        // FIXME rbala What should go there?
        return attributes.keySet();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object getAttributeLock() {
        return this.attributes;
    }

}
