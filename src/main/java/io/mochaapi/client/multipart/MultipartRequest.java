package io.mochaapi.client.multipart;

import io.mochaapi.client.ApiRequest;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents a multipart/form-data request for file uploads.
 * Extends ApiRequest to support file attachments and form fields.
 * 
 * <p>Example usage:</p>
 * <pre>
 * ApiResponse response = client.post("https://api.example.com/upload")
 *     .multipart()
 *     .addFile("file", new File("document.pdf"))
 *     .addField("description", "My file")
 *     .execute();
 * </pre>
 * 
 * @since 1.2.0
 */
public class MultipartRequest {
    
    private final ApiRequest baseRequest;
    private final Map<String, FilePart> files;
    private final Map<String, String> fields;
    
    private MultipartRequest(ApiRequest baseRequest) {
        this.baseRequest = baseRequest;
        this.files = new HashMap<>();
        this.fields = new HashMap<>();
        
        // Set content type for multipart
        baseRequest.header("Content-Type", "multipart/form-data");
    }
    
    /**
     * Creates a new multipart request from an ApiRequest.
     * 
     * @param baseRequest the base request to convert
     * @return multipart request
     */
    public static MultipartRequest of(ApiRequest baseRequest) {
        return new MultipartRequest(baseRequest);
    }
    
    /**
     * Adds a file to the multipart request.
     * 
     * @param fieldName the form field name
     * @param file the file to upload
     * @return this multipart request for chaining
     */
    public MultipartRequest addFile(String fieldName, File file) {
        if (file == null || !file.exists()) {
            throw new IllegalArgumentException("File does not exist: " + file);
        }
        if (!file.isFile()) {
            throw new IllegalArgumentException("Path is not a file: " + file);
        }
        
        files.put(fieldName, new FilePart(fieldName, file));
        return this;
    }
    
    /**
     * Adds a file to the multipart request with a custom filename.
     * 
     * @param fieldName the form field name
     * @param file the file to upload
     * @param filename the custom filename
     * @return this multipart request for chaining
     */
    public MultipartRequest addFile(String fieldName, File file, String filename) {
        if (file == null || !file.exists()) {
            throw new IllegalArgumentException("File does not exist: " + file);
        }
        if (!file.isFile()) {
            throw new IllegalArgumentException("Path is not a file: " + file);
        }
        
        files.put(fieldName, new FilePart(fieldName, file, filename));
        return this;
    }
    
    /**
     * Adds a file from a Path to the multipart request.
     * 
     * @param fieldName the form field name
     * @param path the file path
     * @return this multipart request for chaining
     */
    public MultipartRequest addFile(String fieldName, Path path) {
        if (path == null || !Files.exists(path)) {
            throw new IllegalArgumentException("Path does not exist: " + path);
        }
        if (!Files.isRegularFile(path)) {
            throw new IllegalArgumentException("Path is not a regular file: " + path);
        }
        
        files.put(fieldName, new FilePart(fieldName, path.toFile()));
        return this;
    }
    
    /**
     * Adds a form field to the multipart request.
     * 
     * @param fieldName the field name
     * @param value the field value
     * @return this multipart request for chaining
     */
    public MultipartRequest addField(String fieldName, String value) {
        if (fieldName == null) {
            throw new IllegalArgumentException("Field name cannot be null");
        }
        fields.put(fieldName, value != null ? value : "");
        return this;
    }
    
    /**
     * Adds a form field with an object value (converted to string).
     * 
     * @param fieldName the field name
     * @param value the field value
     * @return this multipart request for chaining
     */
    public MultipartRequest addField(String fieldName, Object value) {
        return addField(fieldName, value != null ? value.toString() : null);
    }
    
    /**
     * Returns the base ApiRequest.
     * 
     * @return base request
     */
    public ApiRequest getBaseRequest() {
        return baseRequest;
    }
    
    /**
     * Returns the files in this multipart request.
     * 
     * @return map of field names to file parts
     */
    public Map<String, FilePart> getFiles() {
        return files;
    }
    
    /**
     * Returns the form fields in this multipart request.
     * 
     * @return map of field names to values
     */
    public Map<String, String> getFields() {
        return fields;
    }
    
    /**
     * Checks if this multipart request has any files.
     * 
     * @return true if has files
     */
    public boolean hasFiles() {
        return !files.isEmpty();
    }
    
    /**
     * Checks if this multipart request has any form fields.
     * 
     * @return true if has fields
     */
    public boolean hasFields() {
        return !fields.isEmpty();
    }
    
    /**
     * Returns the total size of all files in bytes.
     * 
     * @return total file size
     */
    public long getTotalFileSize() {
        return files.values().stream()
                .mapToLong(FilePart::getSize)
                .sum();
    }
    
    @Override
    public String toString() {
        return String.format("MultipartRequest{files=%d, fields=%d, totalSize=%d bytes}",
                files.size(), fields.size(), getTotalFileSize());
    }
    
    /**
     * Represents a file part in a multipart request.
     */
    public static class FilePart {
        private final String fieldName;
        private final File file;
        private final String filename;
        private final String contentType;
        
        private FilePart(String fieldName, File file) {
            this(fieldName, file, file.getName());
        }
        
        private FilePart(String fieldName, File file, String filename) {
            this.fieldName = fieldName;
            this.file = file;
            this.filename = filename;
            this.contentType = detectContentType(file);
        }
        
        /**
         * Returns the form field name.
         * 
         * @return field name
         */
        public String getFieldName() {
            return fieldName;
        }
        
        /**
         * Returns the file.
         * 
         * @return file
         */
        public File getFile() {
            return file;
        }
        
        /**
         * Returns the filename.
         * 
         * @return filename
         */
        public String getFilename() {
            return filename;
        }
        
        /**
         * Returns the content type.
         * 
         * @return content type
         */
        public String getContentType() {
            return contentType;
        }
        
        /**
         * Returns the file size in bytes.
         * 
         * @return file size
         */
        public long getSize() {
            return file.length();
        }
        
        /**
         * Reads the file content as bytes.
         * 
         * @return file content
         * @throws IOException if reading fails
         */
        public byte[] getContent() throws IOException {
            return Files.readAllBytes(file.toPath());
        }
        
        /**
         * Detects the content type based on file extension.
         * 
         * @param file the file
         * @return detected content type
         */
        private String detectContentType(File file) {
            String name = file.getName().toLowerCase();
            
            if (name.endsWith(".txt")) return "text/plain";
            if (name.endsWith(".html") || name.endsWith(".htm")) return "text/html";
            if (name.endsWith(".css")) return "text/css";
            if (name.endsWith(".js")) return "application/javascript";
            if (name.endsWith(".json")) return "application/json";
            if (name.endsWith(".xml")) return "application/xml";
            if (name.endsWith(".pdf")) return "application/pdf";
            if (name.endsWith(".zip")) return "application/zip";
            if (name.endsWith(".jpg") || name.endsWith(".jpeg")) return "image/jpeg";
            if (name.endsWith(".png")) return "image/png";
            if (name.endsWith(".gif")) return "image/gif";
            if (name.endsWith(".svg")) return "image/svg+xml";
            if (name.endsWith(".mp4")) return "video/mp4";
            if (name.endsWith(".mp3")) return "audio/mpeg";
            
            return "application/octet-stream";
        }
        
        @Override
        public String toString() {
            return String.format("FilePart{fieldName='%s', filename='%s', contentType='%s', size=%d}",
                    fieldName, filename, contentType, getSize());
        }
    }
}
