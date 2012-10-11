package org.webharvest.ioc;

import java.util.Set;

/**
 * Implementors of this interface can serve as the backing store for
 * Objects that are scoped within an (subclass of) {@link AttributeHolderScope}.
 * Based on work of Matthias Treydte <waldheinz at gmail.com>.
 *
 * @author Robert Bala
 * @since 2.1.0-SNAPSHOT
 * @version %I%, %G%
 */
public interface AttributeHolder {

    /**
     * Extracts the {@code Object} memorized for the specified key from this
     * {@code AttributeHolder}.
     *
     * @param key the identifier for the attribute to extract
     * @return the {@code Object} stored for the specified key, or {@code null}
     *      if either the {@code null} value was stored for this key or there
     *      is no attribute stored for the key
     * @see #hasAttribute(java.lang.Object) to discriminate the two reasons
     *      this method may return {@code null}
     */
    public Object getAttribute(Object key);

    /**
     * Decides if this {@code AttributeHolder} has an association for the
     * specified key.
     *
     * @param key the key to check if it's known to this {@code AttributeHolder}
     * @return if this key is known
     */
    public boolean hasAttribute(Object key);

    /**
     * Stores a new value in this {@code AttributeHolder}.
     *
     * @param key the key to identify the new attribute
     * @param value the new attribute
     */
    public void putAttribute(Object key, Object value);

    /**
     * Returns all values currently stored in this {@code AttributeHolder}. The
     * returned set can not be modified.
     *
     * @return all attributes of this holder
     */
    public Set<Object> getAttributes();

    /**
     * Returns an object on which to lock when access to multiple methods of
     * the {@code AttributeHolder} are to be made atomic.
     *
     * @return the {@code Object} to synchronize on
     */
    public Object getAttributeLock();

}
