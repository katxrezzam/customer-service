package com.bootcamp.customerservice.dto;

import com.bootcamp.customerservice.model.CustomerType;
import com.bootcamp.customerservice.model.DocumentType;
import java.time.Instant;

/**
 * DTO de salida. Nunca se expone la entidad {@code Customer} directamente en el contrato REST.
 */
public record CustomerResponse(
        String id,
        CustomerType customerType,
        DocumentType documentType,
        String documentNumber,
        String firstName,
        String lastName,
        String businessName,
        Instant createdAt,
        Instant updatedAt) {
}
