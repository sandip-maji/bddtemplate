package com.org.bddtemplate.service;

import com.org.bddtemplate.dto.ProjectMetadata;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.*;
import java.util.Collections;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
public class ProjectGeneratorService {

    public ByteArrayOutputStream generateProject(ProjectMetadata metadata) throws IOException {
        Path tempDir = Files.createTempDirectory("generated-project");
        copyTemplateFromJar("template", tempDir);
        customizePomFile(tempDir, metadata);
        return zipDirectory(tempDir);
    }

    private void copyTemplateFromJar(String resourceFolder, Path targetDir) throws IOException {

        try {

            ClassPathResource resource = new ClassPathResource(resourceFolder);

            if (!resource.exists()) {
                throw new IOException("Resource folder not found: " + resourceFolder);
            }

            if (resource.getURI().toString().startsWith("jar")) {
                copyFromJar(resource, resourceFolder, targetDir);
            } else {
                copyFromFileSystem(resource.getFile().toPath(), targetDir);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void copyFromFileSystem(Path sourceDir, Path targetDir) throws IOException {
        Files.walk(sourceDir).forEach(sourcePath -> {
            try {
                Path targetPath = targetDir.resolve(sourceDir.relativize(sourcePath));
                if (Files.isDirectory(sourcePath)) {
                    Files.createDirectories(targetPath);
                } else {
                    Files.copy(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);
                }
            } catch (IOException e) {
                throw new RuntimeException("Error copying files", e);
            }
        });
    }

    private void copyFromJar(ClassPathResource resource, String resourceFolder, Path targetDir) throws IOException {
        String jarPath = resource.getURI().toString().split("!")[0].substring(4);
        try (JarFile jarFile = new JarFile(jarPath)) {
            for (JarEntry entry : Collections.list(jarFile.entries())) {
                if (entry.getName().startsWith(resourceFolder + "/")) {
                    Path targetPath = targetDir.resolve(entry.getName().substring(resourceFolder.length() + 1));
                    if (entry.isDirectory()) {
                        Files.createDirectories(targetPath);
                    } else {
                        Files.createDirectories(targetPath.getParent());
                        try (InputStream inputStream = jarFile.getInputStream(entry)) {
                            Files.copy(inputStream, targetPath, StandardCopyOption.REPLACE_EXISTING);
                        }
                    }
                }
            }
        }
    }

    private void customizePomFile(Path projectDir, ProjectMetadata metadata) throws IOException {
        Path pomPath = projectDir.resolve("pom.xml");
        if (Files.exists(pomPath)) {
            String content = Files.readString(pomPath);
            content = content.replace("${group}", metadata.getGroup())
                    .replace("${artifact}", metadata.getArtifact())
                    .replace("${name}", metadata.getName())
                    .replace("${description}", metadata.getDescription());
            Files.writeString(pomPath, content);
        }
    }

    private ByteArrayOutputStream zipDirectory(Path dirPath) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ZipOutputStream zos = new ZipOutputStream(baos)) {
            Files.walk(dirPath).filter(path -> !Files.isDirectory(path)).forEach(path -> {
                ZipEntry zipEntry = new ZipEntry(dirPath.relativize(path).toString());
                try {
                    zos.putNextEntry(zipEntry);
                    Files.copy(path, zos);
                    zos.closeEntry();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }
        return baos;
    }
}
