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

package org.webharvest.deprecated.runtime;

import org.apache.commons.collections.IteratorUtils;
import org.apache.commons.collections.Transformer;
import org.apache.commons.lang.StringUtils;
import org.webharvest.exception.VariableException;
import org.webharvest.runtime.DynamicScopeContext;
import org.webharvest.runtime.Scraper;
import org.webharvest.runtime.variables.Variable;
import org.webharvest.utils.KeyValuePair;
import org.webharvest.utils.Stack;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import static java.text.MessageFormat.format;

public class ScraperContext10 implements DynamicScopeContext {

    private static final String CALLER_PREFIX = "caller.";

    private Stack<Map<String, Variable>> stack = new Stack<Map<String, Variable>>();
    private Scraper scraper;

    public ScraperContext10(Scraper scraper) {
        this.stack.push(new HashMap<String, Variable>());
        this.scraper = scraper;
        scraper.getLogger().warn("" +
                "You are using the DEPRECATED scraper configuration version. " +
                "We urge you to migrate to a newer one! " +
                "Please visit http://web-harvest.sourceforge.net/release.php for details.");
    }

    @Override
    public Variable getVar(String name) {
        checkIdentifier(name);

        int level = 0;
        while (name.startsWith(CALLER_PREFIX, level * CALLER_PREFIX.length())) {
            level++;
        }

        final List<Map<String, Variable>> mapList = stack.getList();
        if (mapList.size() > level) {
            return mapList.get(mapList.size() - 1 - level).get(name.substring(level * CALLER_PREFIX.length()));
        } else {
            throw new VariableException(MessageFormat.format("Too many ''caller.'' prefixes in the variable name ''{0}''", name));
        }
    }

    @Override
    public void setLocalVar(String key, Variable value) {
        checkIdentifier(key);
        stack.peek().put(key, value);
    }

    @Override
    public <R> R executeWithinNewContext(Callable<R> callable) throws InterruptedException {
        try {
            // No new contexts.
            // Just execute...
            return callable.call();

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw e;
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Variable replaceExistingVar(String name, Variable variable) {
        final Variable oldVar = getVar(name);
        setLocalVar(name, variable);
        return oldVar;
    }

    @Override
    public boolean containsVar(String name) {
        return getVar(name) != null;
    }

    public <R> R executeFunctionCall(Callable<R> callable) throws InterruptedException {
        // Here the context shifts.
        try {
            stack.push(new HashMap<String, Variable>());
            Scraper.initContext(this, scraper);
            return callable.call();

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw e;
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            stack.pop();
        }
    }


    @Override
    @SuppressWarnings({"unchecked"})
    public Iterator<KeyValuePair<Variable>> iterator() {
        return IteratorUtils.transformedIterator(stack.peek().entrySet().iterator(), new Transformer() {
            @Override
            public Object transform(Object input) {
                final Map.Entry<String, Variable> entry = (Map.Entry<String, Variable>) input;
                return new KeyValuePair<Variable>(entry.getKey(), entry.getValue());
            }
        });
    }

    protected void checkIdentifier(String identifier) {
        if (StringUtils.isBlank(identifier)) {
            throw new VariableException(format("Invalid identifier ''{0}''", identifier));
        }
    }
}
