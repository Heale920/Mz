package org.example.blogplarform;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@MapperScan("org.example.blogplarform.mapper")
@EnableTransactionManagement
public class BlogPlarformApplication {

    public static void main(String[] args) {
        SpringApplication.run(BlogPlarformApplication.class, args);
    }

}
