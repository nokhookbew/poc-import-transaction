package com.example.pocimporttransaction.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.function.Consumer;

@Service
public class ConsumeService {

    @Autowired
    private FileValidateService fileValidateService;

    @Bean
    public Consumer<Message<String>> validateMessage() {
        return message -> {
            System.out.println("Received message: " + message.getPayload());
            fileValidateService.uploadFileBufferReader(message.getPayload());
            System.out.println("File validated");
        };
    }
}
