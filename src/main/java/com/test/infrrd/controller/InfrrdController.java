/*
 * Copyright (c) 2017 JCPenney Co. All rights reserved.
 */
package com.test.infrrd.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Optional;

/**
 * @author Pramit Karmakar
 * Created on 01/09/20
 */

@Controller
@Slf4j
public class InfrrdController {

    @Autowired
    RestTemplate restTemplate;

    @Bean
    RestTemplate getRestTemplate() {
        return new RestTemplate();
    }

    String FILE_PATH = "src/main/resources/";

    @GetMapping("/infrrd/download")
    public ResponseEntity downloadFile(@RequestParam(value = "fileName") String fileName) {

        Optional fileExtension = getFileExtension(fileName);

        File file = new File(FILE_PATH + fileName);
        boolean exists = file.exists();
        log.info("Is file exist: {}", exists);

        if (!fileExtension.isPresent() || !exists) {
            return new ResponseEntity<>("File not found", HttpStatus.INTERNAL_SERVER_ERROR);
        }

        Path path = Paths.get(FILE_PATH + fileName);
        Resource resource = null;
        try {
            resource = new UrlResource(path.toUri());

        } catch (Exception e) {
            log.error("Unable to process resource path");
            e.printStackTrace();
        }
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("application/txt"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }

    @DeleteMapping("/infrrd/delete")
    public ResponseEntity deleteFile(@RequestParam(value = "fileName") String fileName) {

        try {
            Path fileToDeletePath = Paths.get(FILE_PATH + fileName);
            log.info("Deleting file: {}", fileName);
            Files.delete(fileToDeletePath);
        } catch (IOException e) {
            e.printStackTrace();
            return new ResponseEntity( "File not present", HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity( "Deleted", HttpStatus.OK);
    }

    @GetMapping("/infrrd/copy")
    public ResponseEntity copyFile(@RequestParam(value = "fileName") String fileName) {

        log.info("Copying new file from source file {}", fileName);

        Optional fileExtension = getFileExtension(fileName);

        if (!fileExtension.isPresent()) {
            return new ResponseEntity<>("File extension required", HttpStatus.INTERNAL_SERVER_ERROR);
        }

        try {
            Path sourcePath = Paths.get(FILE_PATH + fileName);
            String file = fileName.replaceAll("."+fileExtension.get().toString(),"") +
                    System.currentTimeMillis() + "." + fileExtension.get().toString();
            Path destinationPath = Paths.get(FILE_PATH + file);
            Files.copy(sourcePath, destinationPath, StandardCopyOption.REPLACE_EXISTING);
            log.info("Created new file with filename {} from source file {}",file, fileName);
            return new ResponseEntity("Copied file name : "+ destinationPath.getFileName(), HttpStatus.OK);
        } catch (IOException e) {
            e.printStackTrace();
            return new ResponseEntity("Source File not found", HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }

    public Optional getFileExtension(String fileName) {

        return Optional.ofNullable(fileName)
                .filter(f -> f.contains("."))
                .map(f -> f.substring(fileName.lastIndexOf(".") + 1));
    }

}
