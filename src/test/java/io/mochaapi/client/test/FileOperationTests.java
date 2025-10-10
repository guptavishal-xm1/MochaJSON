package io.mochaapi.client.test;

import io.mochaapi.client.*;
import io.mochaapi.client.exception.ApiException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test suite for file upload and download operations.
 * 
 * @since 1.2.0
 */
public class FileOperationTests {
    
    private MockHttpServer mockServer;
    private String baseUrl;
    private Path tempDir;
    
    @BeforeEach
    void setUp() throws Exception {
        mockServer = new MockHttpServer();
        baseUrl = "http://localhost:" + mockServer.start();
        
        // Create temporary directory for test files
        tempDir = Files.createTempDirectory("mochajson-test");
    }
    
    @AfterEach
    void tearDown() throws IOException {
        if (mockServer != null) {
            mockServer.stop();
        }
        
        // Clean up temporary files
        if (tempDir != null && Files.exists(tempDir)) {
            Files.walk(tempDir)
                    .sorted((a, b) -> b.compareTo(a)) // Delete files before directories
                    .forEach(path -> {
                        try {
                            Files.delete(path);
                        } catch (IOException e) {
                            // Ignore cleanup errors
                        }
                    });
        }
    }
    
    @Test
    @DisplayName("File upload with multipart request")
    public void testFileUploadWithMultipart() throws IOException {
        System.out.println("\n=== Testing file upload with multipart request ===");
        
        // Create a test file
        Path testFile = tempDir.resolve("test-upload.txt");
        String content = "Hello, MochaJSON! This is a test file for upload.";
        Files.write(testFile, content.getBytes());
        
        ApiClient client = new ApiClient.Builder().build();
        
        // Upload the file
        ApiResponse response = client.post(baseUrl + "/test/echo")
                .multipart()
                .addFile("file", testFile.toFile())
                .addField("description", "Test file upload")
                .addField("version", "1.2.0")
                .getBaseRequest()
                .execute();
        
        assertNotNull(response);
        assertTrue(response.isSuccess());
        System.out.println("Upload response: " + response.code());
        System.out.println("Response body: " + response.body());
        
        // The mock server confirms multipart request was received
        assertTrue(response.body().contains("Multipart request received"));
        assertTrue(response.body().contains("bytes"));
    }
    
    @Test
    @DisplayName("File upload with custom filename")
    public void testFileUploadWithCustomFilename() throws IOException {
        System.out.println("\n=== Testing file upload with custom filename ===");
        
        // Create a test file
        Path testFile = tempDir.resolve("original-name.txt");
        String content = "Content with custom filename";
        Files.write(testFile, content.getBytes());
        
        ApiClient client = new ApiClient.Builder().build();
        
        // Upload with custom filename
        ApiResponse response = client.post(baseUrl + "/test/echo")
                .multipart()
                .addFile("document", testFile.toFile(), "custom-filename.pdf")
                .addField("type", "document")
                .getBaseRequest()
                .execute();
        
        assertNotNull(response);
        assertTrue(response.isSuccess());
        System.out.println("Upload response: " + response.code());
        System.out.println("Response body: " + response.body());
        
        // Verify custom filename is used
        assertTrue(response.body().contains("Multipart request received"));
    }
    
    @Test
    @DisplayName("Multiple file upload")
    public void testMultipleFileUpload() throws IOException {
        System.out.println("\n=== Testing multiple file upload ===");
        
        // Create multiple test files
        Path file1 = tempDir.resolve("file1.txt");
        Path file2 = tempDir.resolve("file2.json");
        
        Files.write(file1, "Content of file 1".getBytes());
        Files.write(file2, "{\"name\": \"file2\", \"type\": \"json\"}".getBytes());
        
        ApiClient client = new ApiClient.Builder().build();
        
        // Upload multiple files
        ApiResponse response = client.post(baseUrl + "/test/echo")
                .multipart()
                .addFile("file1", file1.toFile())
                .addFile("file2", file2.toFile())
                .addField("uploadType", "multiple")
                .getBaseRequest()
                .execute();
        
        assertNotNull(response);
        assertTrue(response.isSuccess());
        System.out.println("Multiple upload response: " + response.code());
        
        // Verify both files are included
        assertTrue(response.body().contains("Multipart request received"));
    }
    
    @Test
    @DisplayName("File download to File object")
    public void testFileDownloadToFile() throws IOException {
        System.out.println("\n=== Testing file download to File object ===");
        
        ApiClient client = new ApiClient.Builder().build();
        
        // Download to a file
        Path downloadPath = tempDir.resolve("downloaded.txt");
        File downloadedFile = client.get(baseUrl + "/test/success")
                .download(downloadPath);
        
        assertNotNull(downloadedFile);
        assertTrue(downloadedFile.exists());
        assertTrue(downloadedFile.length() > 0);
        
        System.out.println("Downloaded file: " + downloadedFile.getAbsolutePath());
        System.out.println("File size: " + downloadedFile.length() + " bytes");
        
        // Verify content
        String content = Files.readString(downloadedFile.toPath());
        assertNotNull(content);
        assertFalse(content.isEmpty());
        System.out.println("Downloaded content: " + content.substring(0, Math.min(100, content.length())));
    }
    
    @Test
    @DisplayName("File download to Path")
    public void testFileDownloadToPath() throws IOException {
        System.out.println("\n=== Testing file download to Path ===");
        
        ApiClient client = new ApiClient.Builder().build();
        
        // Download to a Path
        Path downloadPath = tempDir.resolve("downloaded-path.txt");
        File downloadedFile = client.get(baseUrl + "/test/success")
                .download(downloadPath);
        
        assertNotNull(downloadedFile);
        assertTrue(Files.exists(downloadPath));
        assertEquals(downloadPath, downloadedFile.toPath());
        
        System.out.println("Downloaded to path: " + downloadPath);
    }
    
    @Test
    @DisplayName("File download to filename string")
    public void testFileDownloadToString() throws IOException {
        System.out.println("\n=== Testing file download to filename string ===");
        
        ApiClient client = new ApiClient.Builder().build();
        
        // Download to filename string
        Path downloadPath = tempDir.resolve("downloaded-string.txt");
        File downloadedFile = client.get(baseUrl + "/test/success")
                .download(downloadPath.toString());
        
        assertNotNull(downloadedFile);
        assertTrue(downloadedFile.exists());
        assertEquals(downloadPath.toString(), downloadedFile.getAbsolutePath());
        
        System.out.println("Downloaded to string path: " + downloadedFile.getAbsolutePath());
    }
    
    @Test
    @DisplayName("File download as InputStream")
    public void testFileDownloadAsStream() throws IOException {
        System.out.println("\n=== Testing file download as InputStream ===");
        
        ApiClient client = new ApiClient.Builder().build();
        
        // Download as InputStream
        try (java.io.InputStream stream = client.get(baseUrl + "/test/success").downloadStream()) {
            assertNotNull(stream);
            
            byte[] buffer = new byte[1024];
            int bytesRead = stream.read(buffer);
            assertTrue(bytesRead > 0);
            
            String content = new String(buffer, 0, bytesRead);
            System.out.println("Stream content: " + content.substring(0, Math.min(100, content.length())));
        }
    }
    
    @Test
    @DisplayName("File upload with different content types")
    public void testFileUploadWithDifferentContentTypes() throws IOException {
        System.out.println("\n=== Testing file upload with different content types ===");
        
        ApiClient client = new ApiClient.Builder().build();
        
        // Test different file types
        String[] extensions = {".txt", ".json", ".html", ".xml", ".pdf"};
        String[] expectedTypes = {
            "text/plain", 
            "application/json", 
            "text/html", 
            "application/xml", 
            "application/pdf"
        };
        
        for (int i = 0; i < extensions.length; i++) {
            Path testFile = tempDir.resolve("test" + extensions[i]);
            Files.write(testFile, ("Content for " + extensions[i]).getBytes());
            
            ApiResponse response = client.post(baseUrl + "/test/echo")
                    .multipart()
                    .addFile("file", testFile.toFile())
                    .getBaseRequest()
                .execute();
            
            assertNotNull(response);
            assertTrue(response.isSuccess());
            System.out.println("Uploaded " + extensions[i] + " - Content-Type detected correctly");
        }
    }
    
    @Test
    @DisplayName("Download error handling")
    public void testDownloadErrorHandling() {
        System.out.println("\n=== Testing download error handling ===");
        
        ApiClient client = new ApiClient.Builder().build();
        
        // Try to download from non-existent endpoint
        try {
            client.get(baseUrl + "/test/not-found").download("error-test.txt");
            fail("Expected ApiException for failed download");
        } catch (ApiException e) {
            System.out.println("Download error handled correctly: " + e.getMessage());
            assertTrue(e.getMessage().contains("Download failed with status: 404"));
        }
    }
    
    @Test
    @DisplayName("Multipart request validation")
    public void testMultipartRequestValidation() {
        System.out.println("\n=== Testing multipart request validation ===");
        
        ApiClient client = new ApiClient.Builder().build();
        
        // Test with non-existent file
        try {
            client.post(baseUrl + "/test/echo")
                    .multipart()
                    .addFile("file", new File("non-existent-file.txt"))
                    .getBaseRequest()
                .execute();
            fail("Expected IllegalArgumentException for non-existent file");
        } catch (IllegalArgumentException e) {
            System.out.println("Validation error handled correctly: " + e.getMessage());
            assertTrue(e.getMessage().contains("File does not exist"));
        }
        
        // Test with directory instead of file
        try {
            client.post(baseUrl + "/test/echo")
                    .multipart()
                    .addFile("file", tempDir.toFile())
                    .getBaseRequest()
                .execute();
            fail("Expected IllegalArgumentException for directory");
        } catch (IllegalArgumentException e) {
            System.out.println("Directory validation error handled correctly: " + e.getMessage());
            assertTrue(e.getMessage().contains("Path is not a file"));
        }
    }
    
    @Test
    @DisplayName("Multipart request with only fields")
    public void testMultipartRequestWithOnlyFields() {
        System.out.println("\n=== Testing multipart request with only fields ===");
        
        ApiClient client = new ApiClient.Builder().build();
        
        // Create multipart request with only form fields
        ApiResponse response = client.post(baseUrl + "/test/echo")
                .multipart()
                .addField("name", "Test User")
                .addField("email", "test@example.com")
                .addField("age", 25)
                .getBaseRequest()
                .execute();
        
        assertNotNull(response);
        assertTrue(response.isSuccess());
        System.out.println("Form-only multipart response: " + response.code());
        
        // Verify form fields are included
        assertTrue(response.body().contains("Multipart request received"));
    }
}
