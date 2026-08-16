# Diagramas de secuencia — customer-service

Requerimiento no funcional (Parte I): *"Elaborar diagramas de secuencia de cada microservicio."*

## Alta de cliente (con publicación a Kafka)

`customer-service` es el origen del modelo de lectura que consumen `auth-service` y
`yanki-service` — cada alta/baja/modificación publica un evento best-effort a `customer.events`
(no bloquea la respuesta si Kafka está caído).

```mermaid
sequenceDiagram
    actor Cliente
    participant GW as api-gateway
    participant CS as customer-service
    participant Mongo as customerdb
    participant Kafka

    Cliente->>GW: POST /customers
    GW->>CS: forward (ruta pública, sin JWT)
    CS->>CS: valida Bean Validation (PERSONAL sin businessName / BUSINESS con RUC)
    CS->>Mongo: existsByDocumentNumber(documentNumber)
    alt documento ya registrado
        Mongo-->>CS: true
        CS-->>GW: 409 Conflict
        GW-->>Cliente: 409 Conflict
    else documento libre
        Mongo-->>CS: false
        CS->>Mongo: save(customer)
        Mongo-->>CS: customer (id generado)
        CS->>Kafka: publish CustomerEvent CREATED (topic customer.events)
        Note right of Kafka: best-effort - auth-service y yanki-service<br/>lo consumen para sus directorios locales
        CS-->>GW: 201 Created
        GW-->>Cliente: 201 Created
    end
```

## Baja de cliente

`DELETE /customers/{id}` también publica el evento correspondiente (`DELETED`), para que los
directorios locales de los microservicios nuevos se mantengan consistentes.

```mermaid
sequenceDiagram
    actor Cliente
    participant GW as api-gateway
    participant CS as customer-service
    participant Mongo as customerdb
    participant Kafka

    Cliente->>GW: DELETE /customers/{id}
    GW->>CS: forward
    CS->>Mongo: findById(id)
    alt no existe
        Mongo-->>CS: vacío
        CS-->>GW: 404 Not Found
    else existe
        Mongo-->>CS: customer
        CS->>Mongo: delete(customer)
        CS->>Kafka: publish CustomerEvent DELETED (topic customer.events)
        CS-->>GW: 204 No Content
    end
    GW-->>Cliente: respuesta
```
