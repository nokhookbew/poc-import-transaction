package com.example.pocimporttransaction.service;

import com.example.pocimporttransaction.model.Record;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
public class PocImportTransService {

    private static final long MAX_SIZE = 1000000000;
    private static final int MAX_LINE_LENGTH = 41;
    private static final int MAX_ROW_COUNT = 1000000;
    private static final String FILE_EXTENSION = "txt";

    @Autowired
    private StreamBridge streamBridge;

    private static final String UPLOAD_DIR = "/data/shared/";

    public String uploadFileBufferReader(MultipartFile file) {
        String executeTime = "execution time : ";
        long startTime = System.currentTimeMillis();
        try {
            // Basic validations
            // Valida file size
            if (file.getSize() > MAX_SIZE) {
                System.out.println("file size : " + file.getSize());
                return "File size exceeds limit.";
            }
            // Validate file extension
            if (!FILE_EXTENSION.equalsIgnoreCase(FilenameUtils.getExtension(file.getOriginalFilename()))) {
                System.out.println("file extension : " + file.getOriginalFilename());
                return "File extension doesn't match to allow.";
            }
            // Read the file stream to check line length and row count (streaming using BufferedReader)
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(file.getInputStream()))) {
                String line;
                Integer rowCount = 0;
                while ((line = reader.readLine()) != null) {
                    rowCount++;
                    System.out.println("line : " + rowCount + " data : " + line);
                    if (line.length() > MAX_LINE_LENGTH) {
                        return "A line exceeds allowed length.";
                    }
                }
                if (rowCount > MAX_ROW_COUNT) {
                    return "File has too many rows.";
                }
            }

            // Upload file
            String uuid = UUID.randomUUID().toString();
            Path uploadPath = Paths.get(UPLOAD_DIR + uuid);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // Convert file to byte array
            byte[] bytes = file.getBytes();

            // Write byte array to file
            Path filePath = uploadPath.resolve(
                    Objects.requireNonNull(file.getOriginalFilename()));
            try (FileOutputStream fos = new FileOutputStream(filePath.toFile())) {
                fos.write(bytes);
            }
            System.out.println("File uploaded successfully: " + filePath.toString());
            sendMessage(filePath.toString());
            long endTime = System.currentTimeMillis() - startTime;
            System.out.println(executeTime + (endTime) + " ms");
            return executeTime + (endTime);
        } catch (Exception e) {
            System.out.println(e);
            return "Error";
        }
    }


    private void sendMessage(String path) {
        streamBridge.send("validateOutput", path);
        System.out.println("Send record to kafka validate : " + path);
    }
}
