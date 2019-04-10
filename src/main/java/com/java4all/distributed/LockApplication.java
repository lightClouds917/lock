package com.java4all.distributed;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan(value = "com.java4all.distributed.dao")
public class LockApplication {

	public static void main(String[] args) {
		SpringApplication.run(LockApplication.class, args);
	}

}

