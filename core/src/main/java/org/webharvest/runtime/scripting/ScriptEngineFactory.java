/*
 Copyright (c) 2006-2007, Vladimir Nikic
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

package org.webharvest.runtime.scripting;

import org.webharvest.exception.ScriptEngineException;
import org.webharvest.exception.ScriptException;
import org.webharvest.runtime.DynamicScopeContext;
import org.webharvest.runtime.scripting.impl.BeanShellScriptEngine;
import org.webharvest.runtime.variables.NodeVariable;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

import static org.apache.commons.lang.ObjectUtils.defaultIfNull;

/**
 * Created by IntelliJ IDEA.
 * User: awajda
 * Date: Sep 26, 2010
 * Time: 4:42:44 PM
 */
public class ScriptEngineFactory {

    private ScriptingLanguage defaultScriptingLanguage;

    private Map<ScriptingLanguage, Map<String, ScriptEngine>> engineCachePerLanguage;

    public ScriptEngineFactory(ScriptingLanguage defaultScriptingLanguage, DynamicScopeContext context) {
        this.defaultScriptingLanguage = defaultScriptingLanguage;
        this.engineCachePerLanguage = new HashMap<ScriptingLanguage, Map<String, ScriptEngine>>();

        // initialize cache for each language
        for (ScriptingLanguage language : ScriptingLanguage.values()) {
            engineCachePerLanguage.put(language, new HashMap<String, ScriptEngine>());
        }

        // todo: for backward compat only. Think about better way to reuse methods and functions between scripts.
        context.setLocalVar(BeanShellScriptEngine.BeanShellDelegate.class.getName(),
                new NodeVariable(new BeanShellScriptEngine.BeanShellDelegate()));
    }

    public ScriptEngine getEngine(ScriptSource scriptSource) {
        final Map<String, ScriptEngine> engineCache = engineCachePerLanguage.get(getLanguageNotNull(scriptSource.getLanguage()));
        final String key = createCacheKey(scriptSource.getSourceCode());

        ScriptEngine engine = engineCache.get(key);
        if (engine == null) {
            engine = createEngine(scriptSource);
            engineCache.put(key, engine);
        }

        return engine;
    }

    private ScriptEngine createEngine(ScriptSource scriptSource) {
        try {
            return getLanguageNotNull(scriptSource.getLanguage()).engineClass.
                    getConstructor(String.class).
                    newInstance(scriptSource.getSourceCode());
        } catch (InstantiationException e) {
            throw new ScriptEngineException(e);
        } catch (IllegalAccessException e) {
            throw new ScriptEngineException(e);
        } catch (NoSuchMethodException e) {
            throw new ScriptEngineException(e);
        } catch (InvocationTargetException e) {
            throw new ScriptException(e.getTargetException());
        }
    }

    private String createCacheKey(String input) {
        return input;
    }

    private ScriptingLanguage getLanguageNotNull(ScriptingLanguage language) {
        return (ScriptingLanguage) defaultIfNull(language, defaultScriptingLanguage);
    }
}
