package com.bootcamp.customerservice.dto;

import com.bootcamp.customerservice.model.CustomerType;
import com.bootcamp.customerservice.model.DocumentType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * DTO de entrada para crear/actualizar un cliente. Solo valida presencia basica de campos
 * (Bean Validation); las reglas cruzadas segun {@link CustomerType} (que campos son obligatorios
 * u obligan un {@link DocumentType} especifico) se validan en la capa de servicio, porque
 * dependen de una combinacion de campos, no de uno solo.
 */
public record CustomerRequest(
        @NotNull(message = "customerType es obligatorio") CustomerType customerType,
        @NotNull(message = "documentType es obligatorio") DocumentType documentType,
        @NotBlank(message = "documentNumber es obligatorio") String documentNumber,
        String firstName,
        String lastName,
        String businessName) {
}
