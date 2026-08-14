package com.bootcamp.customerservice.service;

import com.bootcamp.customerservice.dto.CustomerMapper;
import com.bootcamp.customerservice.dto.CustomerRequest;
import com.bootcamp.customerservice.dto.CustomerResponse;
import com.bootcamp.customerservice.event.CustomerEventPublisher;
import com.bootcamp.customerservice.event.CustomerEventType;
import com.bootcamp.customerservice.exception.CustomerNotFoundException;
import com.bootcamp.customerservice.exception.DuplicateDocumentException;
import com.bootcamp.customerservice.exception.InvalidBusinessRuleException;
import com.bootcamp.customerservice.model.Customer;
import com.bootcamp.customerservice.model.CustomerType;
import com.bootcamp.customerservice.model.DocumentType;
import com.bootcamp.customerservice.repository.CustomerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Implementacion de {@link CustomerService}. Las validaciones de negocio que pueden fallar van
 * siempre envueltas en {@code Mono.defer(...)}: si se lanzara la excepcion "a pelo" (fuera de un
 * Mono), escaparia del pipeline reactivo como una excepcion Java comun en lugar de propagarse
 * como error reactivo (leccion aprendida de la iteracion anterior del proyecto).
 */
@Service
public class CustomerServiceImpl implements CustomerService {

    private static final Logger log = LoggerFactory.getLogger(CustomerServiceImpl.class);

    private final CustomerRepository repository;
    private final CustomerEventPublisher eventPublisher;

    public CustomerServiceImpl(
            CustomerRepository repository, CustomerEventPublisher eventPublisher) {
        this.repository = repository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public Mono<CustomerResponse> create(CustomerRequest request) {
        return validateBusinessRules(request)
                .then(Mono.defer(() -> validateDocumentNotTaken(request.documentNumber(), null)))
                .then(Mono.defer(() -> repository.save(CustomerMapper.toEntity(request))))
                .doOnNext(saved -> {
                    log.info("Cliente creado id={} customerType={}",
                            saved.getId(), saved.getCustomerType());
                    eventPublisher.publish(saved, CustomerEventType.CREATED);
                })
                .map(CustomerMapper::toResponse);
    }

    @Override
    public Flux<CustomerResponse> findAll() {
        return repository.findAll().map(CustomerMapper::toResponse);
    }

    @Override
    public Mono<CustomerResponse> findById(String id) {
        return findEntityById(id).map(CustomerMapper::toResponse);
    }

    @Override
    public Mono<CustomerResponse> update(String id, CustomerRequest request) {
        return findEntityById(id)
                .flatMap(existing -> validateBusinessRules(request)
                        .then(Mono.defer(() ->
                                validateDocumentNotTaken(request.documentNumber(), id)))
                        .then(Mono.defer(() -> {
                            CustomerMapper.applyUpdate(existing, request);
                            return repository.save(existing);
                        })))
                .doOnNext(saved -> {
                    log.info("Cliente actualizado id={} customerType={}",
                            saved.getId(), saved.getCustomerType());
                    eventPublisher.publish(saved, CustomerEventType.UPDATED);
                })
                .map(CustomerMapper::toResponse);
    }

    @Override
    public Mono<Void> delete(String id) {
        return findEntityById(id)
                .flatMap(existing -> repository.delete(existing).thenReturn(existing))
                .doOnNext(existing -> {
                    log.info("Cliente eliminado id={}", existing.getId());
                    eventPublisher.publish(existing, CustomerEventType.DELETED);
                })
                .then();
    }

    private Mono<Customer> findEntityById(String id) {
        return repository.findById(id)
                .switchIfEmpty(Mono.defer(() -> Mono.error(new CustomerNotFoundException(id))));
    }

    /**
     * Reglas cruzadas segun {@link CustomerType} (ver enunciado y CONVENTIONS.md): PERSONAL exige
     * firstName+lastName, prohibe businessName, y solo admite documento DNI/CE/PASSPORT; BUSINESS
     * es al reves y exige documento RUC.
     */
    private Mono<Void> validateBusinessRules(CustomerRequest request) {
        return Mono.defer(() -> {
            if (request.customerType() == CustomerType.PERSONAL) {
                if (!StringUtils.hasText(request.firstName())
                        || !StringUtils.hasText(request.lastName())) {
                    return Mono.error(new InvalidBusinessRuleException(
                            "Cliente personal requiere firstName y lastName"));
                }
                if (StringUtils.hasText(request.businessName())) {
                    return Mono.error(new InvalidBusinessRuleException(
                            "Cliente personal no debe tener businessName"));
                }
                if (request.documentType() == DocumentType.RUC) {
                    return Mono.error(new InvalidBusinessRuleException(
                            "Cliente personal no puede usar documento RUC"));
                }
            } else {
                if (!StringUtils.hasText(request.businessName())) {
                    return Mono.error(new InvalidBusinessRuleException(
                            "Cliente empresarial requiere businessName"));
                }
                if (StringUtils.hasText(request.firstName())
                        || StringUtils.hasText(request.lastName())) {
                    return Mono.error(new InvalidBusinessRuleException(
                            "Cliente empresarial no debe tener firstName/lastName"));
                }
                if (request.documentType() != DocumentType.RUC) {
                    return Mono.error(new InvalidBusinessRuleException(
                            "Cliente empresarial requiere documento RUC"));
                }
            }
            return Mono.empty();
        });
    }

    /**
     * Verifica que documentNumber no este en uso por OTRO cliente. En update, excludeId es el id
     * del propio cliente (no cuenta como duplicado contra si mismo).
     */
    private Mono<Void> validateDocumentNotTaken(String documentNumber, String excludeId) {
        return repository.findByDocumentNumber(documentNumber)
                .flatMap(existing -> {
                    boolean isSameCustomer = excludeId != null
                            && existing.getId().equals(excludeId);
                    if (isSameCustomer) {
                        return Mono.<Void>empty();
                    }
                    return Mono.<Void>error(new DuplicateDocumentException(documentNumber));
                });
    }
}
