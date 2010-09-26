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
import org.webharvest.runtime.variables.Variable;
import org.webharvest.utils.CommonUtil;
import org.webharvest.utils.KeyValuePair;
import org.webharvest.utils.Stack;

import java.util.*;

/**
 * Context of scraper execution. All the variables created during
 * scraper execution are stored in this context.
 */
public class ScraperContext implements DynamicScopeContext {

    private Stack<Set<String>> variablesNamesStack = new Stack<Set<String>>();
    private Map<String, Stack<Variable>> centralReferenceTable = new HashMap<String, Stack<Variable>>();

    public ScraperContext() {
        variablesNamesStack.push(new HashSet<String>());
    }

    @SuppressWarnings({"UnusedDeclaration"})
    public void setVar(String name, Object value) {
        setVar(name, CommonUtil.createVariable(value));
    }

    @Override
    public void setVar(String name, Variable variable) {
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
        variableValueStack.push(variable);
    }

    /**
     * Sets variable into local context.
     * This method is kept for the backward compatibility only.
     *
     * @param varName
     * @deprecated use {@link #setVar(String, Object)}
     */
    @Deprecated
    public void put(String varName, Object value) {
        setVar(varName, value);
    }

    /**
     * Removes variable from the local context.
     * This method is kept for the backward compatibility only.
     *
     * @param varName
     * @return removed variable or null
     * @deprecated use {@link #removeVar(String)}
     */
    @Deprecated
    public Variable remove(String varName) {
        return removeVar(varName);
    }

    public Variable removeVar(String varName) {
        return (variablesNamesStack.peek().remove(varName)) ? removeVarFromCRT(varName) : null;
    }

    @Override
    public Variable getVar(String name) {
        final Stack<Variable> stack = centralReferenceTable.get(name);
        return (stack == null) ? null : stack.peek();
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
    public void executeWithinNewContext(Runnable runnable) {
        try {
            variablesNamesStack.push(new HashSet<String>());
            runnable.run();
        } finally {
            for (String varName : variablesNamesStack.pop()) {
                removeVarFromCRT(varName);
            }
        }
    }

    private Variable removeVarFromCRT(String varName) {
        final Stack<Variable> stack = centralReferenceTable.get(varName);
        try {
            return stack.pop();
        } finally {
            if (stack.isEmpty()) {
                centralReferenceTable.remove(varName);
            }
        }
    }

}