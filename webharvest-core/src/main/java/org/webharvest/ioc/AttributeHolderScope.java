package org.webharvest.ioc;

import com.google.inject.Key;
import com.google.inject.OutOfScopeException;
import com.google.inject.Provider;
import com.google.inject.Scope;

/**
 * A {@code Scope} that uses an {@link AttributeHolder} as the backing store
 * for it's scoped objects.
 * Based on work of Matthias Treydte <waldheinz at gmail.com>.
 *
 * @author Robert Bala
 * @since 2.1.0-SNAPSHOT
 * @version %I%, %G%
 */
public class AttributeHolderScope<AHT extends AttributeHolder>
        implements Scope, Provider<AHT> {

    private final ThreadLocal<AHT> holder = new ThreadLocal<AHT>();

    /**
     * Lets the current {@code Thread} enter this {@code Scope}.
     *
     * @param holder the {@link AttributeHolder} instance for the {@code Scope}
     * @throws IllegalStateException if the current {@code Thread} is already
     *      in this {@code Scope}
     */
    public void enter(final AHT holder) {
        if (holder == null) {
            throw new IllegalArgumentException();
        }

        if (this.holder.get() != null) {
            throw new IllegalStateException("already in "
                    + getClass().getSimpleName() + " scope");
        }

        System.out.println("******************** Entering scope!!!");

        this.holder.set(holder);
    }

    /**
     * Lets the current {@code Thread} leave this {@code Scope}.
     *
     * @throws OutOfScopeException if the current thread is not in
     *      this {@code Scope}
     */
    public void exit() throws OutOfScopeException {
        assertInScope();
        this.holder.remove();
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
        return this.holder.get();
    }

    @Override
    public final <T> Provider<T> scope(
            final Key<T> key, final Provider<T> outer) {

        return new Provider<T>() {

            @Override
            public T get() {
                 System.out.println("******************** Attempt to get from scope!!!");

                assertInScope();

                final AttributeHolder ah = holder.get();

                synchronized (ah.getAttributeLock()) {
                    T current = (T) ah.getAttribute(key);

                    if ((current == null) && !ah.hasAttribute(key)) {
                        current = outer.get();
                        ah.putAttribute(key, current);
                        System.out.println("%%%%%%%% PUT IN SCOPE" + current.getClass().getSimpleName() + "%%%%%%%%");
                    } else {
                        System.out.println("%%%%%%%% FOUND IN SCOPE" + current.getClass().getSimpleName() + "%%%%%%%%");
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
        if (holder.get() == null) {
            throw new OutOfScopeException("not in "
                    + getClass().getSimpleName()); //NOI18N
        }
    }

}
