package com.bootcamp.customerservice.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import com.bootcamp.customerservice.dto.CustomerRequest;
import com.bootcamp.customerservice.dto.CustomerResponse;
import com.bootcamp.customerservice.exception.CustomerNotFoundException;
import com.bootcamp.customerservice.exception.GlobalExceptionHandler;
import com.bootcamp.customerservice.model.CustomerType;
import com.bootcamp.customerservice.model.DocumentType;
import com.bootcamp.customerservice.service.CustomerService;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webflux.test.autoconfigure.WebFluxTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Slice test de contrato REST: no levanta Mongo real (solo el contexto WebFlux de este
 * controller + el GlobalExceptionHandler), el service se mockea.
 */
@WebFluxTest(controllers = CustomerController.class)
@org.springframework.context.annotation.Import(GlobalExceptionHandler.class)
class CustomerControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockitoBean
    private CustomerService customerService;

    private CustomerResponse sampleResponse() {
        return new CustomerResponse(
                "abc123", CustomerType.PERSONAL, DocumentType.DNI, "12345678",
                "Ana", "Perez", null, Instant.now(), Instant.now());
    }

    @Test
    void post_requestValido_retorna201() {
        when(customerService.create(any(CustomerRequest.class))).thenReturn(Mono.just(sampleResponse()));

        CustomerRequest request = new CustomerRequest(
                CustomerType.PERSONAL, DocumentType.DNI, "12345678", "Ana", "Perez", null);

        webTestClient.post().uri("/customers")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.id").isEqualTo("abc123")
                .jsonPath("$.documentNumber").isEqualTo("12345678");
    }

    @Test
    void post_sinCustomerType_retorna400() {
        // customerType es @NotNull en el DTO - un body sin ese campo debe fallar Bean Validation
        // antes de llegar al service.
        String bodyInvalido = """
                {"documentType":"DNI","documentNumber":"12345678","firstName":"Ana","lastName":"Perez"}
                """;

        webTestClient.post().uri("/customers")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(bodyInvalido)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.correlationId").exists();
    }

    @Test
    void get_existente_retorna200() {
        when(customerService.findById("abc123")).thenReturn(Mono.just(sampleResponse()));

        webTestClient.get().uri("/customers/{id}", "abc123")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo("abc123");
    }

    @Test
    void get_inexistente_retorna404() {
        when(customerService.findById(eq("no-existe")))
                .thenReturn(Mono.error(new CustomerNotFoundException("no-existe")));

        webTestClient.get().uri("/customers/{id}", "no-existe")
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.correlationId").exists();
    }

    @Test
    void getAll_retornaLista() {
        when(customerService.findAll()).thenReturn(Flux.just(sampleResponse()));

        webTestClient.get().uri("/customers")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(CustomerResponse.class)
                .hasSize(1);
    }

    @Test
    void delete_existente_retorna204() {
        when(customerService.delete("abc123")).thenReturn(Mono.empty());

        webTestClient.delete().uri("/customers/{id}", "abc123")
                .exchange()
                .expectStatus().isNoContent();
    }
}
