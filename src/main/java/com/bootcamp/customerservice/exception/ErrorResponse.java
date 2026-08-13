package com.bootcamp.customerservice.exception;

import java.time.Instant;

/**
 * Cuerpo de error uniforme para toda respuesta no exitosa. Incluye {@code correlationId} siempre
 * (ver {@link com.bootcamp.customerservice.config.CorrelationIdWebFilter}), para poder rastrear
 * un error puntual en logs/auditoria.
 */
public record ErrorResponse(
        Instant timestamp,
        int status,
        String error,
        String message,
        String correlationId,
        String path) {
}
