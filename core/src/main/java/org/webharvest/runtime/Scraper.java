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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webharvest.definition.IElementDef;
import org.webharvest.definition.ScraperConfiguration;
import org.webharvest.exception.DatabaseException;
import org.webharvest.runtime.processors.BaseProcessor;
import org.webharvest.runtime.processors.CallProcessor;
import org.webharvest.runtime.processors.HttpProcessor;
import org.webharvest.runtime.processors.ProcessorResolver;
import org.webharvest.runtime.scripting.ScriptEngineFactory;
import org.webharvest.runtime.variables.EmptyVariable;
import org.webharvest.runtime.variables.InternalVariable;
import org.webharvest.runtime.variables.Variable;
import org.webharvest.runtime.web.HttpClientManager;
import org.webharvest.utils.ClassLoaderUtil;
import org.webharvest.utils.CommonUtil;
import org.webharvest.utils.Stack;
import org.webharvest.utils.SystemUtilities;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.*;

/**
 * Basic runtime class.
 */
@SuppressWarnings({"UnusedDeclaration"})
public class Scraper {

    private static Logger logger = LoggerFactory.getLogger(Scraper.class);

    public static final int STATUS_READY = 0;
    public static final int STATUS_RUNNING = 1;
    public static final int STATUS_PAUSED = 2;
    public static final int STATUS_FINISHED = 3;
    public static final int STATUS_STOPPED = 4;
    public static final int STATUS_ERROR = 5;
    public static final int STATUS_EXIT = 6;

    private ScraperConfiguration configuration;
    private String workingDir;
    private ScraperContext context;
    private ScriptEngineFactory scriptEngineFactory;

    private RuntimeConfig runtimeConfig;

    private transient boolean isDebugMode = false;

    private HttpClientManager httpClientManager;

    // stack of running processors
    private transient Stack<BaseProcessor> runningProcessors = new Stack<BaseProcessor>();

    // stack of running functions
    private transient Stack<CallProcessor> runningFunctions = new Stack<CallProcessor>();

    // params that are proceeded to calling function
    private transient Map<String, Variable> functionParams = new HashMap<String, Variable>();

    // stack of running http processors
    private transient Stack<HttpProcessor> runningHttpProcessors = new Stack<HttpProcessor>();

    // pool of used database connections
    Map<String, Connection> dbPool = new HashMap<String, Connection>();

    private List<ScraperRuntimeListener> scraperRuntimeListeners = new LinkedList<ScraperRuntimeListener>();

    private int status = STATUS_READY;

    private String message = null;

    /**
     * Constructor.
     *
     * @param configuration
     * @param workingDir
     */
    public Scraper(ScraperConfiguration configuration, String workingDir) {
        this.configuration = configuration;
        this.runtimeConfig = new RuntimeConfig();
        this.workingDir = CommonUtil.adaptFilename(workingDir);

        this.httpClientManager = new HttpClientManager();

        this.context = new ScraperContext();
        context.setVar("sys", new InternalVariable(new SystemUtilities(this)));
        context.setVar("http", new InternalVariable(httpClientManager.getHttpInfo()));

        this.scriptEngineFactory = new ScriptEngineFactory(configuration.getScriptingLanguage());
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
        this.context.setVar(name, CommonUtil.createVariable(value));
    }

    /**
     * Add all map values to the context.
     *
     * @param map
     */
    public void addVariablesToContext(Map<String, Object> map) {
        if (map != null) {
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                this.context.setVar(entry.getKey(), CommonUtil.createVariable(entry.getValue()));
            }
        }
    }

    public Variable execute(List<IElementDef> ops) {
        this.setStatus(STATUS_RUNNING);

        // inform al listeners that execution is just about to start
        for (ScraperRuntimeListener listener : scraperRuntimeListeners) {
            listener.onExecutionStart(this);
        }

        try {
            for (IElementDef elementDef : ops) {
                BaseProcessor processor = ProcessorResolver.createProcessor(elementDef);
                if (processor != null) {
                    processor.run(this, context);
                }
            }
        } finally {
            releaseDBConnections();
        }

        return new EmptyVariable();
    }

    public void execute() {
        long startTime = System.currentTimeMillis();

        try {
            ScraperContextHolder.init(context);
            execute(configuration.getOperations());
        } finally {
            ScraperContextHolder.clear();
        }

        if (this.status == STATUS_RUNNING) {
            this.setStatus(STATUS_FINISHED);
        }

        // inform al listeners that execution is finished
        for (ScraperRuntimeListener listener : scraperRuntimeListeners) {
            listener.onExecutionEnd(this);
        }

        if (logger.isInfoEnabled()) {
            if (this.status == STATUS_FINISHED) {
                logger.info("Configuration executed in " + (System.currentTimeMillis() - startTime) + "ms.");
            } else if (this.status == STATUS_STOPPED) {
                logger.info("Configuration stopped!");
            }
        }
    }

    public ScraperContext getContext() {
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

    public Logger getLogger() {
        return logger;
    }

    public BaseProcessor getRunningProcessor() {
        return runningProcessors.peek();
    }

    /**
     * @param processor Processor whose parent is needed.
     * @return Parent running processor of the specified running processor, or null if processor is
     *         not currently running or if it is top running processor.
     */
    public BaseProcessor getParentRunningProcessor(BaseProcessor processor) {
        List<BaseProcessor> runningProcessorList = runningProcessors.getList();
        int index = CommonUtil.findValueInCollection(runningProcessorList, processor);
        return index > 0 ? runningProcessorList.get(index - 1) : null;
    }

    /**
     * @param processorClazz Class of enclosing running processor.
     * @return Parent running processor in the tree of specified class, or null if it doesn't exist.
     */
    public BaseProcessor getRunningProcessorOfType(Class processorClazz) {
        List<BaseProcessor> runningProcessorList = runningProcessors.getList();
        ListIterator<BaseProcessor> listIterator = runningProcessorList.listIterator(runningProcessors.size());
        while (listIterator.hasPrevious()) {
            BaseProcessor curr = listIterator.previous();
            if (processorClazz.equals(curr.getClass())) {
                return curr;
            }
        }
        return null;
    }

    public RuntimeConfig getRuntimeConfig() {
        return runtimeConfig;
    }

    /**
     * Get connection from the connection pool, and first create one if necessery
     *
     * @param jdbc       Name of JDBC class
     * @param connection JDBC connection string
     * @param username   Username
     * @param password   Password
     * @return JDBC connection used to access database
     */
    public Connection getConnection(String jdbc, String connection, String username, String password) {
        try {
            String poolKey = jdbc + "-" + connection + "-" + username + "-" + password;
            Connection conn = dbPool.get(poolKey);
            if (conn == null) {
                ClassLoaderUtil.registerJDBCDriver(jdbc);
                conn = DriverManager.getConnection(connection, username, password);
                dbPool.put(poolKey, conn);
            }
            return conn;
        }
        catch (Exception e) {
            throw new DatabaseException(e);
        }
    }

    public void setExecutingProcessor(BaseProcessor processor) {
        runningProcessors.push(processor);
        for (ScraperRuntimeListener listener : scraperRuntimeListeners) {
            listener.onNewProcessorExecution(this, processor);
        }
    }

    public void finishExecutingProcessor() {
        this.runningProcessors.pop();
    }

    public void processorFinishedExecution(BaseProcessor processor, Map properties) {
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

    /**
     * Releases all DB connections from the pool.
     */
    public void releaseDBConnections() {
        for (Connection connection : dbPool.values()) {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    throw new DatabaseException(e);
                }
            }
        }
    }

    public ScriptEngineFactory getScriptEngineFactory() {
        return scriptEngineFactory;
    }
}