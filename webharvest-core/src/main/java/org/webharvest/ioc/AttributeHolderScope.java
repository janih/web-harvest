package org.webharvest.ioc;

import java.util.Stack;

import com.google.inject.Key;
import com.google.inject.OutOfScopeException;
import com.google.inject.Provider;
import com.google.inject.Scope;

/**
 * A {@code Scope} that uses an {@link AttributeHolder} as the backing store for
 * its scoped objects. {@link AttributeHolderScope} supports nested scopes, that
 * is, client code can enter scope as many times as required. Nested scopes are
 * separated from each other.
 *
 * Based on work of Matthias Treydte <waldheinz at gmail.com>.
 *
 * @author Robert Bala
 * @author Piotr Dyraga
 * @since 2.1.0-SNAPSHOT
 * @version %I%, %G%
 */
public class AttributeHolderScope<AHT extends AttributeHolder>
        implements Scope, Provider<AHT> {

    private final ThreadLocal<Stack<AHT>> holder =
        new ThreadLocal<Stack<AHT>>() {
            protected java.util.Stack<AHT> initialValue() {
                return new Stack<AHT>();
            };
    };

    /**
     * Lets the current {@code Thread} enter this {@code Scope}.
     *
     * @param holder
     *            the {@link AttributeHolder} instance for the {@code Scope}
     */
    public void enter(final AHT holder) {
        if (holder == null) {
            throw new IllegalArgumentException();
        }

        this.holder.get().add(holder);
    }

    /**
     * Lets the current {@code Thread} leave this {@code Scope}.
     *
     * @throws OutOfScopeException if the current thread is not in
     *      this {@code Scope}
     */
    public void exit() throws OutOfScopeException {
        assertInScope();
        this.holder.get().pop();
    }

    /**
     * {@inheritDoc}
     *
     * @return {@docRoot}
     * @throws OutOfScopeException if the current thread is not in
     *      this {@code Scope}
     */
    @Override
    public AHT get() throws OutOfScopeException {
        assertInScope();
        return this.holder.get().peek();
    }

    @Override
    public final <T> Provider<T> scope(
            final Key<T> key, final Provider<T> outer) {

        return new Provider<T>() {

            @Override
            public T get() {
                assertInScope();

                final AttributeHolder ah = holder.get().peek();

                synchronized (ah.getAttributeLock()) {
                    T current = (T) ah.getAttribute(key);

                    if ((current == null) && !ah.hasAttribute(key)) {
                        current = outer.get();
                        ah.putAttribute(key, current);
                    }

                    return current;
                }
            }

            @Override
            public String toString() {
                return "Provider [scope=" + //NOI18N
                        AttributeHolderScope.this.getClass().getSimpleName() +
                        ", outer=" + outer.toString() + "]"; //NOI18N
            }

        };
    }

    private void assertInScope() throws OutOfScopeException {
        if (holder.get().isEmpty()) {
            throw new OutOfScopeException("not in "
                    + getClass().getSimpleName()); //NOI18N
        }
    }

}
