package com.bootcamp.customerservice.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.bootcamp.customerservice.dto.CustomerRequest;
import com.bootcamp.customerservice.exception.CustomerNotFoundException;
import com.bootcamp.customerservice.exception.DuplicateDocumentException;
import com.bootcamp.customerservice.exception.InvalidBusinessRuleException;
import com.bootcamp.customerservice.model.Customer;
import com.bootcamp.customerservice.model.CustomerType;
import com.bootcamp.customerservice.model.DocumentType;
import com.bootcamp.customerservice.repository.CustomerRepository;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
class CustomerServiceImplTest {

    @Mock
    private CustomerRepository repository;

    private CustomerServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new CustomerServiceImpl(repository);
    }

    private CustomerRequest personalRequest() {
        return new CustomerRequest(CustomerType.PERSONAL, DocumentType.DNI, "12345678", "Ana", "Perez", null);
    }

    private CustomerRequest businessRequest() {
        return new CustomerRequest(CustomerType.BUSINESS, DocumentType.RUC, "20123456789", null, null, "Acme SAC");
    }

    private Customer savedPersonal() {
        return Customer.builder()
                .id("abc123")
                .customerType(CustomerType.PERSONAL)
                .documentType(DocumentType.DNI)
                .documentNumber("12345678")
                .firstName("Ana")
                .lastName("Perez")
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }

    @Test
    void create_personalValido_creaElCliente() {
        when(repository.findByDocumentNumber("12345678")).thenReturn(Mono.empty());
        when(repository.save(any(Customer.class))).thenReturn(Mono.just(savedPersonal()));

        StepVerifier.create(service.create(personalRequest()))
                .expectNextMatches(response -> "abc123".equals(response.id())
                        && response.customerType() == CustomerType.PERSONAL
                        && "12345678".equals(response.documentNumber()))
                .verifyComplete();
    }

    @Test
    void create_businessValido_creaElCliente() {
        Customer savedBusiness = Customer.builder()
                .id("biz1")
                .customerType(CustomerType.BUSINESS)
                .documentType(DocumentType.RUC)
                .documentNumber("20123456789")
                .businessName("Acme SAC")
                .build();
        when(repository.findByDocumentNumber("20123456789")).thenReturn(Mono.empty());
        when(repository.save(any(Customer.class))).thenReturn(Mono.just(savedBusiness));

        StepVerifier.create(service.create(businessRequest()))
                .expectNextMatches(response -> response.customerType() == CustomerType.BUSINESS
                        && "Acme SAC".equals(response.businessName()))
                .verifyComplete();
    }

    @Test
    void create_personalConBusinessName_falla() {
        CustomerRequest invalid = new CustomerRequest(
                CustomerType.PERSONAL, DocumentType.DNI, "12345678", "Ana", "Perez", "No deberia ir");

        StepVerifier.create(service.create(invalid))
                .expectError(InvalidBusinessRuleException.class)
                .verify();
    }

    @Test
    void create_personalSinLastName_falla() {
        CustomerRequest invalid = new CustomerRequest(
                CustomerType.PERSONAL, DocumentType.DNI, "12345678", "Ana", null, null);

        StepVerifier.create(service.create(invalid))
                .expectError(InvalidBusinessRuleException.class)
                .verify();
    }

    @Test
    void create_personalConDocumentoRuc_falla() {
        CustomerRequest invalid = new CustomerRequest(
                CustomerType.PERSONAL, DocumentType.RUC, "12345678", "Ana", "Perez", null);

        StepVerifier.create(service.create(invalid))
                .expectError(InvalidBusinessRuleException.class)
                .verify();
    }

    @Test
    void create_businessConFirstName_falla() {
        CustomerRequest invalid = new CustomerRequest(
                CustomerType.BUSINESS, DocumentType.RUC, "20123456789", "Ana", null, "Acme SAC");

        StepVerifier.create(service.create(invalid))
                .expectError(InvalidBusinessRuleException.class)
                .verify();
    }

    @Test
    void create_businessSinBusinessName_falla() {
        CustomerRequest invalid = new CustomerRequest(
                CustomerType.BUSINESS, DocumentType.RUC, "20123456789", null, null, null);

        StepVerifier.create(service.create(invalid))
                .expectError(InvalidBusinessRuleException.class)
                .verify();
    }

    @Test
    void create_businessConDocumentoNoRuc_falla() {
        CustomerRequest invalid = new CustomerRequest(
                CustomerType.BUSINESS, DocumentType.DNI, "20123456789", null, null, "Acme SAC");

        StepVerifier.create(service.create(invalid))
                .expectError(InvalidBusinessRuleException.class)
                .verify();
    }

    @Test
    void create_documentNumberDuplicado_falla() {
        Customer otro = Customer.builder().id("otro-id").documentNumber("12345678").build();
        when(repository.findByDocumentNumber("12345678")).thenReturn(Mono.just(otro));

        StepVerifier.create(service.create(personalRequest()))
                .expectError(DuplicateDocumentException.class)
                .verify();
    }

    @Test
    void findById_existente_retornaElCliente() {
        when(repository.findById("abc123")).thenReturn(Mono.just(savedPersonal()));

        StepVerifier.create(service.findById("abc123"))
                .expectNextMatches(response -> "abc123".equals(response.id()))
                .verifyComplete();
    }

    @Test
    void findById_inexistente_falla() {
        when(repository.findById("no-existe")).thenReturn(Mono.empty());

        StepVerifier.create(service.findById("no-existe"))
                .expectError(CustomerNotFoundException.class)
                .verify();
    }

    @Test
    void findAll_retornaTodosMapeados() {
        when(repository.findAll()).thenReturn(Flux.just(savedPersonal()));

        StepVerifier.create(service.findAll())
                .expectNextCount(1)
                .verifyComplete();
    }

    @Test
    void update_existente_actualizaYGuarda() {
        Customer existente = savedPersonal();
        when(repository.findById("abc123")).thenReturn(Mono.just(existente));
        when(repository.findByDocumentNumber("12345678")).thenReturn(Mono.just(existente));
        when(repository.save(any(Customer.class))).thenReturn(Mono.just(existente));

        StepVerifier.create(service.update("abc123", personalRequest()))
                .expectNextMatches(response -> "abc123".equals(response.id()))
                .verifyComplete();
    }

    @Test
    void update_inexistente_falla() {
        when(repository.findById("no-existe")).thenReturn(Mono.empty());

        StepVerifier.create(service.update("no-existe", personalRequest()))
                .expectError(CustomerNotFoundException.class)
                .verify();
    }

    @Test
    void update_documentNumberUsadoPorOtroCliente_falla() {
        Customer existente = savedPersonal();
        Customer otro = Customer.builder().id("otro-id").documentNumber("12345678").build();
        when(repository.findById("abc123")).thenReturn(Mono.just(existente));
        when(repository.findByDocumentNumber("12345678")).thenReturn(Mono.just(otro));

        StepVerifier.create(service.update("abc123", personalRequest()))
                .expectError(DuplicateDocumentException.class)
                .verify();
    }

    @Test
    void delete_existente_loElimina() {
        Customer existente = savedPersonal();
        when(repository.findById("abc123")).thenReturn(Mono.just(existente));
        when(repository.delete(existente)).thenReturn(Mono.empty());

        StepVerifier.create(service.delete("abc123"))
                .verifyComplete();

        verify(repository).delete(existente);
    }

    @Test
    void delete_inexistente_falla() {
        when(repository.findById("no-existe")).thenReturn(Mono.empty());

        StepVerifier.create(service.delete("no-existe"))
                .expectError(CustomerNotFoundException.class)
                .verify();
    }
}
