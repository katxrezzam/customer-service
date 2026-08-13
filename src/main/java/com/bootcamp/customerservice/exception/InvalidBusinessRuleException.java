package com.bootcamp.customerservice.exception;

/**
 * Se lanza cuando el request viola una regla de negocio cruzada entre campos (por ejemplo,
 * cliente PERSONAL con businessName, o BUSINESS con un documentType distinto de RUC). Mapea a
 * HTTP 400.
 */
public class InvalidBusinessRuleException extends RuntimeException {

    public InvalidBusinessRuleException(String message) {
        super(message);
    }
}
