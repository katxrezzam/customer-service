package com.bootcamp.customerservice.dto;

import com.bootcamp.customerservice.model.Customer;

/**
 * Mapeo manual entre {@link Customer} (entidad) y los DTOs de entrada/salida. No se introdujo
 * MapStruct para esto: con un solo mapeo de ida y vuelta no justifica todavia sumar una
 * dependencia nueva al proyecto: si el numero de entidades/DTOs crece y el mapeo manual se vuelve
 * repetitivo, vale la pena reconsiderarlo.
 */
public final class CustomerMapper {

    private CustomerMapper() {
    }

    /** Arma una entidad nueva (sin id/auditoria, los completa Mongo al guardar) desde el
     * request. */
    public static Customer toEntity(CustomerRequest request) {
        return Customer.builder()
                .customerType(request.customerType())
                .documentType(request.documentType())
                .documentNumber(request.documentNumber())
                .firstName(request.firstName())
                .lastName(request.lastName())
                .businessName(request.businessName())
                .build();
    }

    /** Aplica los campos editables del request sobre una entidad existente (usado en update). */
    public static void applyUpdate(Customer customer, CustomerRequest request) {
        customer.setCustomerType(request.customerType());
        customer.setDocumentType(request.documentType());
        customer.setDocumentNumber(request.documentNumber());
        customer.setFirstName(request.firstName());
        customer.setLastName(request.lastName());
        customer.setBusinessName(request.businessName());
    }

    /** Convierte la entidad al DTO de salida (nunca se expone {@link Customer} directamente). */
    public static CustomerResponse toResponse(Customer customer) {
        return new CustomerResponse(
                customer.getId(),
                customer.getCustomerType(),
                customer.getDocumentType(),
                customer.getDocumentNumber(),
                customer.getFirstName(),
                customer.getLastName(),
                customer.getBusinessName(),
                customer.getCreatedAt(),
                customer.getUpdatedAt());
    }
}
