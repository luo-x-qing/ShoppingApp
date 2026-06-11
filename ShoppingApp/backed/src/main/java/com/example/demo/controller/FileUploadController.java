package com.example.demo.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@RestController
@RequestMapping("/api")
public class FileUploadController {

    private static final String UPLOAD_DIR = "C:/Users/HP/OneDrive/桌面/大学作业/小程序开发期末大作业/ShoppingApp/upload/";

    @PostMapping("/upload")
    public ResponseEntity<String> upload(@RequestParam("file") MultipartFile file) {
        try {
            String fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
            Path path = Paths.get(UPLOAD_DIR + fileName);
            Files.write(path, file.getBytes());
            // 改成返回 /file/ 开头的 URL，而不是 /images/
            return ResponseEntity.ok("/file/" + fileName);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("上传失败：" + e.getMessage());
        }
    }
}