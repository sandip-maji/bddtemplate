package com.org.bddtemplate.controller;

import com.org.bddtemplate.dto.ProjectMetadata;
import com.org.bddtemplate.service.ProjectGeneratorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.ByteArrayOutputStream;

@RestController
@RequestMapping("/api/project")
public class ProjectGeneratorController {

    @Autowired
    private ProjectGeneratorService projectGeneratorService;

    @PostMapping("/generate")
    public ResponseEntity<byte[]> generateProject(@RequestBody ProjectMetadata metadata) {
        try {
            ByteArrayOutputStream zipOutputStream = projectGeneratorService.generateProject(metadata);
            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + metadata.getArtifact() + ".zip");
            headers.add(HttpHeaders.CONTENT_TYPE, "application/zip");

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(zipOutputStream.toByteArray());
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


}
