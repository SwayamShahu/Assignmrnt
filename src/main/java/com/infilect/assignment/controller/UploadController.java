package com.infilect.assignment.controller;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.infilect.assignment.dto.UploadResponse;
import com.infilect.assignment.service.MappingUploadService;
import com.infilect.assignment.service.StoreUploadService;
import com.infilect.assignment.service.UserUploadService;

@RestController
@RequestMapping("/api/upload")
public class UploadController {

    @Autowired
    private StoreUploadService storeUploadService;

    @Autowired
    private UserUploadService userUploadService;

    @Autowired
    private MappingUploadService mappingUploadService;

    @PostMapping("/stores")
    public ResponseEntity<UploadResponse> uploadStores(@RequestParam("file") MultipartFile file) {
        try {
            UploadResponse response = storeUploadService.uploadStores(
                    file.getInputStream(),
                    file.getOriginalFilename()
            );

            HttpStatus status = response.getStatus().equals("FAILURE") ? HttpStatus.BAD_REQUEST : HttpStatus.OK;
            return new ResponseEntity<>(response, status);

        } catch (IOException e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/users")
    public ResponseEntity<UploadResponse> uploadUsers(@RequestParam("file") MultipartFile file) {
        try {
            UploadResponse response = userUploadService.uploadUsers(
                    file.getInputStream(),
                    file.getOriginalFilename()
            );

            HttpStatus status = response.getStatus().equals("FAILURE") ? HttpStatus.BAD_REQUEST : HttpStatus.OK;
            return new ResponseEntity<>(response, status);

        } catch (IOException e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/mappings")
    public ResponseEntity<UploadResponse> uploadMappings(@RequestParam("file") MultipartFile file) {
        try {
            UploadResponse response = mappingUploadService.uploadMappings(
                    file.getInputStream(),
                    file.getOriginalFilename()
            );

            HttpStatus status = response.getStatus().equals("FAILURE") ? HttpStatus.BAD_REQUEST : HttpStatus.OK;
            return new ResponseEntity<>(response, status);

        } catch (IOException e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "UP");
        response.put("message", "Upload service is running");
        return ResponseEntity.ok(response);
    }
}
