package com.guoshun.devsecai;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.guoshun.devsecai.mapper")
public class DevSecAIApplication {
    public static void main(String[] args) {
        SpringApplication.run(DevSecAIApplication.class, args);
    }
}
