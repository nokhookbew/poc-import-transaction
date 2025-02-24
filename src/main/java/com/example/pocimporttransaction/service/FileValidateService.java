package com.example.pocimporttransaction.service;

import com.example.pocimporttransaction.model.Record;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@Service
public class FileValidateService {
    private static final int BATCH_SIZE = 300;

    @Autowired
    private StreamBridge streamBridge;

    public void uploadFileBufferReader(String filePath) {
        String executeTime = "execution time : ";
        long startTime = System.currentTimeMillis();
        List<Record> batch = new ArrayList<>();
        try {
            // Download file
            Path path = Paths.get(filePath);

            // Optionally: Read the file stream to check line length and row count (streaming using BufferedReader)
            try (InputStream fileInputStream = new FileInputStream(path.toFile());
                 BufferedReader reader = new BufferedReader(new InputStreamReader(fileInputStream))) {
                String line;
                Integer rowCount = 0;
                while ((line = reader.readLine()) != null) {
                    rowCount++;
                    Record record = parshLineToRecord(line);
                    record.setNumberOfTransactions(rowCount);
                    batch.add(record);
                    if (batch.size() >= BATCH_SIZE) {
                        // send kafka
                        sendMessage(batch);
                        batch.clear();
                    }
                }
                if (!batch.isEmpty()) {
                    // send kafka
                    sendMessage(batch);
                }
            }
            long endTime = System.currentTimeMillis() - startTime;
            System.out.println(executeTime + (endTime) + " ms");
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    private Record parshLineToRecord(String line) {
        Record record = new Record();
        record.setTransactionId(line.substring(0, 5));
        record.setTransactionType(line.substring(5, 7));
        record.setTransactionAmount(line.substring(7, 40));
        record.setTransactionDate(line.substring(40, 41));
        return record;
    }

    private void sendMessage(List<Record> batch) {
        streamBridge.send("recordOutput", batch);
        System.out.println("Send record to kafka : " + batch);
    }
}
