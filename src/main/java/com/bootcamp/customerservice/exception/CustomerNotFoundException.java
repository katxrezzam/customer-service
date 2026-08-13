package com.bootcamp.customerservice.exception;

/** Se lanza cuando se busca/actualiza/elimina un cliente cuyo id no existe. Mapea a HTTP 404. */
public class CustomerNotFoundException extends RuntimeException {

    public CustomerNotFoundException(String id) {
        super("No existe un cliente con id " + id);
    }
}
