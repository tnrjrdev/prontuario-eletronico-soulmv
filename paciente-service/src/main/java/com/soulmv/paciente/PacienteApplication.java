package com.soulmv.paciente;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EntityScan(basePackages = "com.soulmv.paciente.entity")
@EnableJpaRepositories(basePackages = "com.soulmv.paciente.repository")
public class PacienteApplication {

    public static void main(String[] args) {
        SpringApplication.run(PacienteApplication.class, args);
    }

}
