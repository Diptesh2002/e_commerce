package com.zefrotech.ecom;

import com.zefrotech.ecom.entity.Role;
import com.zefrotech.ecom.entity.User;
import com.zefrotech.ecom.service.AuthService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class EcomApplication {

	public static void main(String[] args) {
		SpringApplication.run(EcomApplication.class, args);
	}
    @Bean
    CommandLineRunner createAdmin(AuthService authService) {
        return args -> {
            if (authService.findByUserName("admin").isEmpty()) {
                User admin = new User();
                admin.setUserName("admin");
                admin.setPassword("admin123");
                admin.setEmail("admin@example.com");
                admin.setRole(Role.ROLE_ADMIN); // Use updated enum name
                authService.registerUser(admin);
            }
        };
    }
}
