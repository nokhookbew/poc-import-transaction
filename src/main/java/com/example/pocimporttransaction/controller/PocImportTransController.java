package com.example.pocimporttransaction.controller;

import com.example.pocimporttransaction.service.PocImportTransService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
public class PocImportTransController {

    @Autowired
    private PocImportTransService pocImportTransService;

    @PostMapping("/upload/")
    public ResponseEntity<String> uploadFile(@RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(pocImportTransService.uploadFileBufferReader(file));
    }

    @PostMapping("/test")
    public ResponseEntity<String> test(@RequestBody String s) {
        System.out.println(s);
//        return ResponseEntity.ok(pocImportTransService.sendMessageTest(s));
        return ResponseEntity.ok(s);
    }
}
