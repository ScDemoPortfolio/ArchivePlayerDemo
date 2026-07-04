package com.archiveplayer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@RestController
public class ArchivePlayerApplication {

    public static void main(String[] args) {
        SpringApplication.run(ArchivePlayerApplication.class, args);
    }

    @GetMapping("/status")
    public String getStatus() {
        return "ArchivePlayer is running normally";
    }
}
