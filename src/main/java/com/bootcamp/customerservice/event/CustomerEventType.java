package com.bootcamp.customerservice.event;

/** Tipo de evento de cliente publicado a Kafka (topic customer.events). */
public enum CustomerEventType {
    CREATED,
    UPDATED,
    DELETED
}
