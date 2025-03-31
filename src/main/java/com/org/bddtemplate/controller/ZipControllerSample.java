package com.org.bddtemplate.controller;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.*;
import java.nio.file.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@RequestMapping("/api/project")
@RestController
public class ZipControllerSample {

    @GetMapping("/generate-zip")
    public ResponseEntity<InputStreamResource> generateZip(@RequestParam String folderName) throws IOException {
        String sourcePath = "templates/" + folderName;
        ClassPathResource resource = new ClassPathResource(sourcePath);

        if (!resource.exists()) {
            throw new FileNotFoundException("Folder not found: " + folderName);
        }

        // Copy files to a temporary directory
        Path tempDir = Files.createTempDirectory("temp-folder");
        copyResources(sourcePath, tempDir);

        // Create the zip file
        File zipFile = createZip(tempDir);

        InputStreamResource resourceZip = new InputStreamResource(new FileInputStream(zipFile));

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + folderName + ".zip")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resourceZip);
    }

    public void copyResources(String resourcePath, Path targetPath) throws IOException {
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        Resource[] resources = resolver.getResources("classpath:" + resourcePath + "/**/*");

        if (resources.length == 0) {
            throw new FileNotFoundException("No files found in resource path: " + resourcePath);
        }

        for (Resource resource : resources) {
            if (!resource.isReadable()) {
                continue;
            }

            // Calculate the relative path inside the zip
            String relativePath = resource.getURL().getPath()
                    .replaceAll(".*/templates/", "")
                    .replaceAll(".*BOOT-INF/classes/templates/", ""); // Handle JAR paths

            Path outputFile = targetPath.resolve(relativePath);
            Files.createDirectories(outputFile.getParent());
            try (InputStream inputStream = resource.getInputStream()) {
                Files.copy(inputStream, outputFile, StandardCopyOption.REPLACE_EXISTING);
            }
        }
    }

    private File createZip(Path sourceDirPath) throws IOException {
        File zipFile = File.createTempFile("output", ".zip");

        try (ZipOutputStream zipOut = new ZipOutputStream(new FileOutputStream(zipFile))) {
            Files.walk(sourceDirPath)
                    .filter(path -> !Files.isDirectory(path))
                    .forEach(path -> {
                        ZipEntry zipEntry = new ZipEntry(sourceDirPath.relativize(path).toString().replace("\\", "/"));
                        try (InputStream inputStream = Files.newInputStream(path)) {
                            zipOut.putNextEntry(zipEntry);
                            inputStream.transferTo(zipOut);
                            zipOut.closeEntry();
                        } catch (IOException e) {
                            throw new RuntimeException("Error while zipping file: " + path, e);
                        }
                    });
        }
        return zipFile;
    }
}
