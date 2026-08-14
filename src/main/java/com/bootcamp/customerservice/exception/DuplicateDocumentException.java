package com.bootcamp.customerservice.exception;

/** Se lanza al crear/actualizar un cliente con un documentNumber que ya usa otro cliente.
 * Mapea a HTTP 409. */
public class DuplicateDocumentException extends RuntimeException {

    public DuplicateDocumentException(String documentNumber) {
        super("Ya existe un cliente con documentNumber " + documentNumber);
    }
}
