package com.bootcamp.customerservice.model;

/**
 * Tipo de cliente del banco: personal (persona natural) o empresarial (persona juridica).
 * Determina que campos son obligatorios (ver {@link DocumentType}) y las reglas de negocio de
 * productos (cuentas, creditos) definidas en el resto del sistema.
 */
public enum CustomerType {
    PERSONAL,
    BUSINESS
}
