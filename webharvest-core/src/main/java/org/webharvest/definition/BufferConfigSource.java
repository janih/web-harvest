package org.webharvest.definition;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

public final class BufferConfigSource implements ConfigSource {

    private final String content;

    public BufferConfigSource(final String content) {
        if (content == null) {
            throw new IllegalArgumentException("Configuration content is required");
        }
        this.content = content;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Reader getReader() throws IOException {
        return new StringReader(content);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getLocation() {
        return null;
    }

}
