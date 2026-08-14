package com.bootcamp.customerservice.event;

import com.bootcamp.customerservice.model.Customer;
import java.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * Publica a Kafka (topic customer.events) tras crear/actualizar/eliminar un cliente. Es un
 * efecto secundario best-effort, no bloqueante y fuera del pipeline Reactor de la operacion
 * principal (mismo criterio que el resto del proyecto: un hipo de Kafka no debe poder tumbar la
 * capacidad de dar de alta un cliente, que es la operacion de negocio real). Si la publicacion
 * falla, se loguea como error para que quede visible, pero no revierte ni bloquea la respuesta ya
 * dada al cliente HTTP.
 *
 * <p>Trade-off aceptado y dejado constancia: esto NO es un patron Outbox transaccional (guardar
 * el evento en la misma transaccion que el documento Mongo y publicarlo aparte con garantia de
 * entrega). Si el proceso muere justo entre el save() y el publish(), el evento se pierde y el
 * modelo de lectura de un consumidor (ej. auth-service) queda desactualizado hasta la proxima
 * escritura sobre ese cliente. Es una inconsistencia menor y recuperable (no es dinero), no una
 * perdida de integridad - se acepta por ahora en vez de sumar la complejidad de un Outbox real.
 */
@Component
public class CustomerEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(CustomerEventPublisher.class);
    static final String TOPIC = "customer.events";

    private final KafkaTemplate<String, CustomerEvent> kafkaTemplate;

    public CustomerEventPublisher(KafkaTemplate<String, CustomerEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    /** La clave del mensaje es el customerId: garantiza que todos los eventos de un mismo
     * cliente caigan en la misma particion y se procesen en orden (CREATED antes que UPDATED). */
    public void publish(Customer customer, CustomerEventType eventType) {
        CustomerEvent event = new CustomerEvent(
                customer.getId(), customer.getCustomerType(), eventType, Instant.now());
        kafkaTemplate.send(TOPIC, customer.getId(), event).whenComplete((result, ex) -> {
            if (ex != null) {
                log.error("No se pudo publicar evento {} del cliente {}",
                        eventType, customer.getId(), ex);
            } else {
                log.info("Evento {} publicado para cliente {}", eventType, customer.getId());
            }
        });
    }
}
