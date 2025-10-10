package io.mochaapi.client.multipart;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.UUID;

/**
 * Builder for creating multipart/form-data request bodies.
 * Handles the encoding of files and form fields into the multipart format.
 * 
 * <p><strong>Warning:</strong> This implementation loads entire files into memory.
 * For large files, use {@link StreamingMultipartBuilder} instead.</p>
 * 
 * @since 1.2.0
 */
public class MultipartBodyBuilder {
    
    private static final String BOUNDARY_PREFIX = "----MochaAPI-";
    private static final String CRLF = "\r\n";
    private static final long DEFAULT_MAX_FILE_SIZE = 50 * 1024 * 1024; // 50MB default limit
    
    /**
     * Builds a multipart body from a MultipartRequest.
     * 
     * @param request the multipart request
     * @return the encoded multipart body
     * @throws IOException if building fails
     * @throws IllegalArgumentException if request is null
     */
    public static MultipartBody build(MultipartRequest request) throws IOException {
        if (request == null) {
            throw new IllegalArgumentException("MultipartRequest cannot be null");
        }
        
        String boundary = generateBoundary();
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        
        // Add form fields
        for (Map.Entry<String, String> field : request.getFields().entrySet()) {
            writeField(output, boundary, field.getKey(), field.getValue());
        }
        
        // Add files
        for (MultipartRequest.FilePart filePart : request.getFiles().values()) {
            // Check file size before processing
            if (filePart.getContent().length > DEFAULT_MAX_FILE_SIZE) {
                throw new IOException(String.format(
                    "File size (%d bytes) exceeds maximum allowed size (%d bytes). Use StreamingMultipartBuilder for large files.",
                    filePart.getContent().length, DEFAULT_MAX_FILE_SIZE));
            }
            writeFile(output, boundary, filePart);
        }
        
        // Write final boundary
        output.write(("--" + boundary + "--" + CRLF).getBytes(StandardCharsets.UTF_8));
        
        byte[] body = output.toByteArray();
        String contentType = "multipart/form-data; boundary=" + boundary;
        
        return new MultipartBody(body, contentType);
    }
    
    /**
     * Writes a form field to the output stream.
     */
    private static void writeField(ByteArrayOutputStream output, String boundary, 
                                 String fieldName, String value) throws IOException {
        output.write(("--" + boundary + CRLF).getBytes(StandardCharsets.UTF_8));
        output.write(("Content-Disposition: form-data; name=\"" + fieldName + "\"" + CRLF).getBytes(StandardCharsets.UTF_8));
        output.write(CRLF.getBytes(StandardCharsets.UTF_8));
        output.write((value + CRLF).getBytes(StandardCharsets.UTF_8));
    }
    
    /**
     * Writes a file to the output stream.
     */
    private static void writeFile(ByteArrayOutputStream output, String boundary, 
                                MultipartRequest.FilePart filePart) throws IOException {
        output.write(("--" + boundary + CRLF).getBytes(StandardCharsets.UTF_8));
        output.write(("Content-Disposition: form-data; name=\"" + filePart.getFieldName() + 
                     "\"; filename=\"" + filePart.getFilename() + "\"" + CRLF).getBytes(StandardCharsets.UTF_8));
        output.write(("Content-Type: " + filePart.getContentType() + CRLF).getBytes(StandardCharsets.UTF_8));
        output.write(CRLF.getBytes(StandardCharsets.UTF_8));
        
        // Write file content
        byte[] content = filePart.getContent();
        output.write(content);
        output.write(CRLF.getBytes(StandardCharsets.UTF_8));
    }
    
    /**
     * Generates a unique boundary string.
     */
    private static String generateBoundary() {
        return BOUNDARY_PREFIX + UUID.randomUUID().toString().replace("-", "");
    }
    
    /**
     * Represents a built multipart body with content and content type.
     */
    public static class MultipartBody {
        private final byte[] content;
        private final String contentType;
        
        private MultipartBody(byte[] content, String contentType) {
            this.content = content;
            this.contentType = contentType;
        }
        
        /**
         * Returns the multipart body content.
         * 
         * @return body content
         */
        public byte[] getContent() {
            return content;
        }
        
        /**
         * Returns the content type header.
         * 
         * @return content type
         */
        public String getContentType() {
            return contentType;
        }
        
        /**
         * Returns the content length.
         * 
         * @return content length
         */
        public int getContentLength() {
            return content.length;
        }
    }
}
