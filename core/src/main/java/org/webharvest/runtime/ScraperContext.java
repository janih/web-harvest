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
import org.webharvest.runtime.variables.EmptyVariable;
import org.webharvest.runtime.variables.Variable;
import org.webharvest.utils.Assert;
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
    public void setLocalVar(String name, Object value) {
        setLocalVar(name, CommonUtil.createVariable(value));
    }

    @Override
    public void setLocalVar(String name, Variable variable) {
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
        final Stack<Variable> variableValueStack = centralReferenceTable.get(name);
        Assert.isFalse(variableValueStack == null || variableValueStack.isEmpty(), "Variable {0} does not exist", name);
        return variableValueStack.replaceTop((Variable) ObjectUtils.defaultIfNull(variable, EmptyVariable.INSTANCE));
    }

    /**
     * Sets variable into local context.
     * This method is kept for the backward compatibility only.
     *
     * @param varName
     * @deprecated use {@link #setLocalVar(String, Object)}
     */
    @Deprecated
    public void put(String varName, Object value) {
        setLocalVar(varName, value);
    }

    @Override
    public Variable getVar(String name) {
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
    public void executeWithinNewContext(Runnable runnable, boolean loopBody_compat2b1) {
        try {
            variablesNamesStack.push(new HashSet<String>());
            loopBodyScope_compat2b1.push(loopBody_compat2b1);
            runnable.run();
        } finally {
            loopBodyScope_compat2b1.pop();
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

    /**
     * Compatibility stuff dedicated to <var-def> processor
     * which is deprecated and replaced by <def> and <set> processors.
     * <p/>
     * ========= THE FOLLOWING MUST BE REMOVED IN 3.0 MAJOR RELEASE =========
     */

    private Stack<Boolean> loopBodyScope_compat2b1 = new Stack<Boolean>();

    {
        loopBodyScope_compat2b1.push(false);
    }

    @Deprecated
    public void setVar_compat2b1(String name, Variable var) {
        if (!loopBodyScope_compat2b1.peek()) {
            setLocalVar(name, var);
            return;
        }

        // Inside loops <var-def> used to operate with the parent scope
        // (the one of the loop itself instead of the loop body's scope)
        // and so do we below.

        Stack<Variable> variableValueStack = centralReferenceTable.get(name);
        final Set<String> localVariableNames = variablesNamesStack.peek();
        final Set<String> prevVariableNames = variablesNamesStack.getList().get(variablesNamesStack.size() - 2);

        if (var == null) {
            var = EmptyVariable.INSTANCE;
        }

        if (variableValueStack == null) {
            // Case A - new variable for the whole stack
            // [-]...[-][-]
            //        v
            // [-]...[+][-]
            variableValueStack = new Stack<Variable>();
            centralReferenceTable.put(name, variableValueStack);
            prevVariableNames.add(name);
            variableValueStack.push(var);
        } else if (prevVariableNames.contains(name) && localVariableNames.contains(name)) {
            // Case B - variable is defined in both the parent and the local scopes
            // [?]...[1][+]
            //        v
            // [?]...[2][+]
            variableValueStack.getList().set(variableValueStack.size() - 2, var);
        } else if (prevVariableNames.contains(name)) {
            // Case C - variable is defined in the parent scope but local
            // [?]...[1][-]
            //        v
            // [?]...[2][-]
            variableValueStack.getList().set(variableValueStack.size() - 1, var);
        } else if (localVariableNames.contains(name)) {
            // Case D - variable is defined in the parent local scope but parent
            // [?]...[-][+]
            //        v
            // [?]...[+][+]
            prevVariableNames.add(name);
            variableValueStack.getList().add(variableValueStack.size() - 1, var);
        } else {
            // Case E - variable is NOT defined in the parent or local scopes, but defined somewhere earlier.
            // [+]...[-][-]
            //        v
            // [+]...[+][-]
            prevVariableNames.add(name);
            variableValueStack.push(var);
        }
    }
}