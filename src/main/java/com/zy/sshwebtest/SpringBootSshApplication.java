package com.zy.sshwebtest;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"com.zy.sshwebtest"})
public class SpringBootSshApplication {
	
	public static void main(String[] args) {
		SpringApplication.run(SpringBootSshApplication.class, args);
	}

}
