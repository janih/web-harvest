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

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.binary.StringUtils;
import org.apache.commons.collections.map.IdentityMap;
import org.webharvest.exception.EnvironmentException;
import org.webharvest.exception.ScriptEngineException;
import org.webharvest.exception.ScriptException;

import java.lang.reflect.InvocationTargetException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
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
    private MessageDigest sha1Digest;

    public ScriptEngineFactory(ScriptingLanguage defaultScriptingLanguage) {
        try {
            sha1Digest = MessageDigest.getInstance("SHA1");
        } catch (NoSuchAlgorithmException e) {
            throw new EnvironmentException(e);
        }

        this.defaultScriptingLanguage = defaultScriptingLanguage;
        this.engineCachePerLanguage = new HashMap<ScriptingLanguage, Map<String, ScriptEngine>>();

        // initialize cache for each language
        for (ScriptingLanguage language : ScriptingLanguage.values()) {
            @SuppressWarnings({"unchecked"})
            final Map<String, ScriptEngine> identityMap = new IdentityMap();
            engineCachePerLanguage.put(language, identityMap);
        }
    }

    public ScriptEngine getEngine(ScriptSource scriptSource) {
        final Map<String, ScriptEngine> engineCache = engineCachePerLanguage.get(getLanguageNotNull(scriptSource.getLanguage()));
        final String sha1 = calculateSHA1(scriptSource.getSourceCode());

        ScriptEngine engine = engineCache.get(sha1);
        if (engine == null) {
            engine = createEngine(scriptSource);
            engineCache.put(sha1, engine);
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

    private ScriptingLanguage getLanguageNotNull(ScriptingLanguage language) {
        return (ScriptingLanguage) defaultIfNull(language, defaultScriptingLanguage);
    }

    private String calculateSHA1(String input) {
        return Hex.encodeHexString(sha1Digest.digest(StringUtils.getBytesUtf8(input))).intern();
    }
}
