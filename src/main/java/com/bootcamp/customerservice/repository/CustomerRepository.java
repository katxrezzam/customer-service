package com.bootcamp.customerservice.repository;

import com.bootcamp.customerservice.model.Customer;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Mono;

/**
 * Repositorio reactivo de {@link Customer}. Solo metodos derivados por nombre (Spring Data),
 * sin {@code @Query} ni SQL/Mongo query dinamico, segun convencion del proyecto.
 */
public interface CustomerRepository extends ReactiveMongoRepository<Customer, String> {

    /** Usado para validar unicidad de documentNumber antes de crear un cliente nuevo. */
    Mono<Boolean> existsByDocumentNumber(String documentNumber);

    /** Usado para validar unicidad de documentNumber al actualizar (excluyendo al propio
     * cliente). */
    Mono<Customer> findByDocumentNumber(String documentNumber);
}
