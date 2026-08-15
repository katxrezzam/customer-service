package com.bootcamp.customerservice.event;

import com.bootcamp.customerservice.model.CustomerType;
import java.time.Instant;

/**
 * Payload publicado al topic customer.events. Deliberadamente minimo (solo lo que un consumidor
 * necesita) - no expone nombre/apellido u otros datos personales.
 *
 * <p>documentNumber se sumo para yanki-service (Entrega 2, D8 Fase III): necesita cruzar el
 * documento del dueño de un monedero contra el del dueño de una tarjeta de debito al asociarlas,
 * sin poder consultarlo por REST (microservicio nuevo). Es seguro agregarlo: cada consumidor
 * deserializa a su propia copia local del DTO con @JsonIgnoreProperties(ignoreUnknown = true)
 * (ver CONVENTIONS.md), asi que un consumidor viejo (auth-service) que no lo necesita lo ignora
 * sin romperse.
 */
public record CustomerEvent(
        String customerId,
        CustomerType customerType,
        String documentNumber,
        CustomerEventType eventType,
        Instant timestamp) {
}
