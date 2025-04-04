package com.org.bddtemplate.service;

import com.org.bddtemplate.dto.ProjectMetadata;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
public class ProjectGeneratorService {

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
            }catch (Exception e) {
                throw new RuntimeException("Error while Files.copy(inputStream, outputFile, StandardCopyOption.REPLACE_EXISTING): ", e);
            }
        }
    }



    public void modifyPomFile(Path pomPath, ProjectMetadata metadata) throws IOException {
        String content = Files.readString(pomPath);
        content = content.replace("${group}", metadata.getGroup())
                .replace("${artifact}", metadata.getArtifact())
                .replace("${name}", metadata.getName())
                .replace("${description}", metadata.getDescription())
                .replace("${packageName}", metadata.getPackageName())
                .replace("${packaging}", metadata.getPackaging());
        Files.writeString(pomPath, content);
    }

    public File createZip(Path sourceDirPath) throws IOException {
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
        }catch (Exception e) {
            throw new RuntimeException("Error while zipping file: ", e);
        }
        return zipFile;
    }
}
