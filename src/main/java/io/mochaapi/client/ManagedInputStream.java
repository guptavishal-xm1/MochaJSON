package io.mochaapi.client;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * A managed InputStream wrapper that implements AutoCloseable for proper resource management.
 * This class ensures that streams are properly closed and helps prevent memory leaks.
 * 
 * <p>Usage example:</p>
 * <pre>
 * try (ManagedInputStream stream = apiRequest.downloadStream()) {
 *     // Use the stream
 *     byte[] data = stream.readAllBytes();
 * } // Stream is automatically closed
 * </pre>
 * 
 * @since 1.2.0
 * @since 1.3.0 Enhanced for simplified API
 */
public class ManagedInputStream extends InputStream implements AutoCloseable {
    
    private final ByteArrayInputStream delegate;
    private boolean closed = false;
    
    /**
     * Creates a new ManagedInputStream wrapping the provided byte array.
     * 
     * @param data the byte array to wrap
     * @throws IllegalArgumentException if data is null
     */
    public ManagedInputStream(byte[] data) {
        if (data == null) {
            throw new IllegalArgumentException("Data cannot be null");
        }
        this.delegate = new ByteArrayInputStream(data);
    }
    
    @Override
    public int read() throws IOException {
        checkClosed();
        return delegate.read();
    }
    
    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        checkClosed();
        return delegate.read(b, off, len);
    }
    
    @Override
    public int read(byte[] b) throws IOException {
        checkClosed();
        return delegate.read(b);
    }
    
    @Override
    public long skip(long n) throws IOException {
        checkClosed();
        return delegate.skip(n);
    }
    
    @Override
    public int available() throws IOException {
        checkClosed();
        return delegate.available();
    }
    
    @Override
    public void close() throws IOException {
        if (!closed) {
            delegate.close();
            closed = true;
        }
    }
    
    @Override
    public synchronized void mark(int readlimit) {
        try {
            checkClosed();
        } catch (IOException e) {
            throw new RuntimeException("Stream has been closed", e);
        }
        delegate.mark(readlimit);
    }
    
    @Override
    public synchronized void reset() throws IOException {
        checkClosed();
        delegate.reset();
    }
    
    @Override
    public boolean markSupported() {
        return delegate.markSupported();
    }
    
    /**
     * Returns the number of bytes that can be read from this stream.
     * 
     * @return the number of bytes available
     */
    public int size() {
        return delegate.available();
    }
    
    /**
     * Checks if the stream has been closed and throws an IOException if it has.
     * 
     * @throws IOException if the stream has been closed
     */
    private void checkClosed() throws IOException {
        if (closed) {
            throw new IOException("Stream has been closed");
        }
    }
    
    /**
     * Returns true if this stream has been closed.
     * 
     * @return true if closed, false otherwise
     */
    public boolean isClosed() {
        return closed;
    }
}
