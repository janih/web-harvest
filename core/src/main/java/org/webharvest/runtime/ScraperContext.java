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

import org.apache.commons.collections.IteratorUtils;
import org.apache.commons.collections.Transformer;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.webharvest.exception.VariableException;
import org.webharvest.runtime.variables.EmptyVariable;
import org.webharvest.runtime.variables.Variable;
import org.webharvest.utils.Assert;
import org.webharvest.utils.CommonUtil;
import org.webharvest.utils.KeyValuePair;
import org.webharvest.utils.Stack;

import java.util.*;
import java.util.concurrent.Callable;

import static java.text.MessageFormat.format;

/**
 * Context of scraper execution. All the variables created during
 * scraper execution are stored in this context.
 */
public class ScraperContext implements DynamicScopeContext {

    public final Logger log;

    private Stack<Set<String>> variablesNamesStack = new Stack<Set<String>>();
    protected Map<String, Stack<Variable>> centralReferenceTable = new HashMap<String, Stack<Variable>>();

    public ScraperContext(Scraper scraper) {
        variablesNamesStack.push(new HashSet<String>());
        log = scraper.getLogger();
    }

    public void setLocalVar(String name, Object value) {
        setLocalVar(name, CommonUtil.createVariable(value));
    }

    @Override
    public void setLocalVar(String name, Variable variable) {
        checkIdentifier(name);
        Stack<Variable> variableValueStack = centralReferenceTable.get(name);
        final Set<String> localVariableNames = variablesNamesStack.peek();

        if (variableValueStack == null) {
            variableValueStack = new Stack<Variable>();
            centralReferenceTable.put(name, variableValueStack);
            localVariableNames.add(name);
        } else if (localVariableNames.contains(name)) {
            variableValueStack.pop();
        } else {
            localVariableNames.add(name);
        }
        variableValueStack.push((Variable) ObjectUtils.defaultIfNull(variable, EmptyVariable.INSTANCE));
    }

    @Override
    @SuppressWarnings({"ConstantConditions"})
    public Variable replaceExistingVar(String name, Variable variable) {
        checkIdentifier(name);
        final Stack<Variable> variableValueStack = centralReferenceTable.get(name);
        Assert.isFalse(variableValueStack == null || variableValueStack.isEmpty(), "Variable {0} does not exist", name);
        return variableValueStack.replaceTop((Variable) ObjectUtils.defaultIfNull(variable, EmptyVariable.INSTANCE));
    }

    @Override
    public Variable getVar(String name) {
        checkIdentifier(name);
        final Stack<Variable> stack = centralReferenceTable.get(name);
        return (stack == null) ? null : stack.peek();
    }

    @Override
    public boolean containsVar(String name) {
        return getVar(name) != null;
    }

    @Override
    @SuppressWarnings({"unchecked"})
    public Iterator<KeyValuePair<Variable>> iterator() {
        return IteratorUtils.transformedIterator(centralReferenceTable.entrySet().iterator(), new Transformer() {
            @Override
            public Object transform(Object input) {
                final Map.Entry<String, Stack<Variable>> crtEntry = (Map.Entry<String, Stack<Variable>>) input;
                return new KeyValuePair<Variable>(crtEntry.getKey(), crtEntry.getValue().peek());
            }
        });
    }

    @Override
    public <R> R executeWithinNewContext(Callable<R> callable) throws InterruptedException {
        try {
            variablesNamesStack.push(new HashSet<String>());
            return callable.call();

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw e;
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            for (String varName : variablesNamesStack.pop()) {
                removeVarFromCRT(varName);
            }
        }
    }

    private Variable removeVarFromCRT(String varName) {
        checkIdentifier(varName);
        final Stack<Variable> stack = centralReferenceTable.get(varName);
        try {
            return stack.pop();
        } finally {
            if (stack.isEmpty()) {
                centralReferenceTable.remove(varName);
            }
        }
    }

    protected void checkIdentifier(String identifier) {
        if (StringUtils.isBlank(identifier)) {
            throw new VariableException(format("Invalid identifier ''{0}''", identifier));
        }
    }
}