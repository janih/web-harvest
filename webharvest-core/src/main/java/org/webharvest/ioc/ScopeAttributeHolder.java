package org.webharvest.ioc;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

// TODO missing documentation
// TODO Missing unit test
public class ScopeAttributeHolder implements AttributeHolder {

    private final Map<Object, Object> attributes = new HashMap<Object, Object>();

    /**
     * {@inheritDoc}
     */
    @Override
    // TODO Missing unit test
    public Object getAttribute(final Object key) {
        return attributes.get(key);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    // TODO Missing unit test
    public boolean hasAttribute(final Object key) {
        return attributes.containsKey(key);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    // TODO Missing unit test
    public void putAttribute(final Object key, final Object value) {
        attributes.put(key, value);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    // TODO Missing unit test
    public Set<Object> getAttributes() {
        // FIXME rbala What should go there?
        return attributes.keySet();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    // TODO Missing unit test
    public Object getAttributeLock() {
        return this.attributes;
    }

}
