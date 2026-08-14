package com.bootcamp.customerservice.event;

import com.bootcamp.customerservice.model.CustomerType;
import java.time.Instant;

/**
 * Payload publicado al topic customer.events. Deliberadamente minimo (solo lo que un consumidor
 * necesita para saber "este customerId existe/dejo de existir, es de este tipo") - no expone
 * datos personales (nombre, documento) que un consumidor como auth-service no necesita para
 * validar referencias.
 */
public record CustomerEvent(
        String customerId,
        CustomerType customerType,
        CustomerEventType eventType,
        Instant timestamp) {
}
