package com.org.bddtemplate.service;

import com.org.bddtemplate.dto.ProjectMetadata;
import org.springframework.stereotype.Service;

import java.io.*;
import java.net.URISyntaxException;
import java.nio.file.*;
import java.util.Objects;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
public class ProjectGeneratorService {

    public ByteArrayOutputStream generateProject(ProjectMetadata metadata) throws IOException, URISyntaxException {
        Path tempDir = Files.createTempDirectory("generated-project");
        //Path templateDir = Paths.get("src/main/resources/template");
        Path templateDir = Paths.get(new File(Objects.requireNonNull(getClass().getClassLoader().getResource("template")).toURI()).toURI());

        copyTemplate(templateDir, tempDir);
        customizePomFile(tempDir, metadata);
        return zipDirectory(tempDir);
    }

    private void copyTemplate(Path sourceDir, Path targetDir) throws IOException {
        Files.walk(sourceDir).forEach(sourcePath -> {
            try {
                Path targetPath = targetDir.resolve(sourceDir.relativize(sourcePath));
                if (Files.isDirectory(sourcePath)) {
                    Files.createDirectories(targetPath);
                } else {
                    Files.copy(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    private void customizePomFile(Path projectDir, ProjectMetadata metadata) throws IOException {
        Path pomPath = projectDir.resolve("pom.xml");
        String content = Files.readString(pomPath);
        content = content.replace("${group}", metadata.getGroup())
                          .replace("${artifact}", metadata.getArtifact())
                          .replace("${name}", metadata.getName())
                          .replace("${description}", metadata.getDescription());
        Files.writeString(pomPath, content);
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