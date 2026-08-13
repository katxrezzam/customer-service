package com.bootcamp.customerservice.model;

/**
 * Tipo de documento de identificacion del cliente. DNI, CE (carnet de extranjeria) y PASSPORT
 * son validos solo para clientes {@link CustomerType#PERSONAL}; RUC es exclusivo de clientes
 * {@link CustomerType#BUSINESS}. La combinacion invalida se rechaza en la capa de servicio.
 */
public enum DocumentType {
    DNI,
    CE,
    PASSPORT,
    RUC
}
