package com.tong;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableTransactionManagement
public class DolphinCommentApplication {

    public static void main(String[] args) {
        SpringApplication.run(DolphinCommentApplication.class, args);
    }
}
