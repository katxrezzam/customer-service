package com.bootcamp.customerservice.model;

import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Entidad de negocio Cliente. Persistida en la coleccion "customers" de customerdb (MongoDB,
 * database-per-service).
 *
 * <p>Segun {@link #customerType}: si es PERSONAL, {@link #firstName} y {@link #lastName} son
 * obligatorios y {@link #businessName} debe quedar vacio; si es BUSINESS, es al reves. Esa regla
 * se valida en la capa de servicio, no aqui (la entidad no valida invariantes de negocio).
 */
@Document(collection = "customers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Customer {

    @Id
    private String id;

    private CustomerType customerType;

    private DocumentType documentType;

    /** Unico en toda la coleccion, validado en la capa de servicio antes de guardar. */
    @Indexed(unique = true)
    private String documentNumber;

    private String firstName;

    private String lastName;

    private String businessName;

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;
}
