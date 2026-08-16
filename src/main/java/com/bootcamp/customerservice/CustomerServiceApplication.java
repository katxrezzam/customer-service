package com.bootcamp.customerservice;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/** Punto de entrada de customer-service: gestion de clientes personal/empresarial. Contrato
 * OpenAPI generado en /v3/api-docs, explorable en /swagger-ui.html. */
@OpenAPIDefinition(info = @Info(
        title = "customer-service",
        version = "v1",
        description = "Gestion de clientes personal/empresarial: CRUD, perfiles VIP/PYME."))
@SpringBootApplication
public class CustomerServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(CustomerServiceApplication.class, args);
    }
}
