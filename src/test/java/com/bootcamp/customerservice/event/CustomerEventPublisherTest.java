package com.bootcamp.customerservice.event;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.bootcamp.customerservice.model.Customer;
import com.bootcamp.customerservice.model.CustomerType;
import java.util.concurrent.CompletableFuture;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

@ExtendWith(MockitoExtension.class)
class CustomerEventPublisherTest {

    @Mock
    private KafkaTemplate<String, CustomerEvent> kafkaTemplate;

    private CustomerEventPublisher publisher;

    private Customer customer() {
        return Customer.builder().id("cust1").customerType(CustomerType.PERSONAL)
                .documentNumber("12345678").build();
    }

    @Test
    void publish_envuelveElEventoConLaClaveDelCliente() {
        publisher = new CustomerEventPublisher(kafkaTemplate);
        when(kafkaTemplate.send(eq(CustomerEventPublisher.TOPIC), eq("cust1"), any()))
                .thenReturn(CompletableFuture.completedFuture(mock(SendResult.class)));

        publisher.publish(customer(), CustomerEventType.CREATED);

        verify(kafkaTemplate).send(eq(CustomerEventPublisher.TOPIC), eq("cust1"), any());
    }

    @Test
    void publish_siKafkaFalla_noPropagaLaExcepcion() {
        publisher = new CustomerEventPublisher(kafkaTemplate);
        CompletableFuture<SendResult<String, CustomerEvent>> failed = new CompletableFuture<>();
        failed.completeExceptionally(new RuntimeException("kafka caido"));
        when(kafkaTemplate.send(eq(CustomerEventPublisher.TOPIC), eq("cust1"), any()))
                .thenReturn(failed);

        // no debe tirar: publish() es best-effort, un Kafka caido no puede tumbar la operacion
        // de negocio que lo dispara (ver comentario de la clase).
        publisher.publish(customer(), CustomerEventType.CREATED);
    }
}
