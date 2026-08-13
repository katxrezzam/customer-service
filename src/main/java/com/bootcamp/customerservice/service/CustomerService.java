package com.bootcamp.customerservice.service;

import com.bootcamp.customerservice.dto.CustomerRequest;
import com.bootcamp.customerservice.dto.CustomerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/** Casos de uso de negocio sobre clientes: CRUD + reglas de tipo personal/empresarial. */
public interface CustomerService {

    Mono<CustomerResponse> create(CustomerRequest request);

    Flux<CustomerResponse> findAll();

    Mono<CustomerResponse> findById(String id);

    Mono<CustomerResponse> update(String id, CustomerRequest request);

    Mono<Void> delete(String id);
}
