package com.org.bddtemplate.controller;

import com.org.bddtemplate.dto.ProjectMetadata;
import com.org.bddtemplate.service.ProjectGeneratorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@RequestMapping("/api/project")
@RestController
public class ProjectGeneratorController {

    @Autowired
    ProjectGeneratorService projectGeneratorService;


    @PostMapping("/generate")
    public ResponseEntity<InputStreamResource> generateZip(
            @RequestParam String folderName,
            @RequestBody ProjectMetadata metadata) throws IOException {

        String sourcePath = "templates/" + folderName;
        Path tempDir = Files.createTempDirectory("temp-folder");
        projectGeneratorService.copyResources(sourcePath, tempDir);

        // Modify pom.xml
        Path pomPath = tempDir.resolve(folderName+"/pom.xml");
        System.out.println("Looking for pom.xml at: " + pomPath.toAbsolutePath());
        if (Files.exists(pomPath)) {
            projectGeneratorService.modifyPomFile(pomPath, metadata);
        }

        // Create zip
        File zipFile = projectGeneratorService.createZip(tempDir);

        InputStreamResource resourceZip = new InputStreamResource(new FileInputStream(zipFile));

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + folderName + ".zip")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resourceZip);
    }


}
