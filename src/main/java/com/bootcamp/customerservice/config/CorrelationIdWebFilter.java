package com.bootcamp.customerservice.config;

import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

/**
 * Genera (o propaga, si ya viene en el header) un correlationId por request y lo deja disponible
 * como atributo del {@link ServerWebExchange} para el resto del pipeline reactivo.
 *
 * <p>Leccion aprendida de la iteracion anterior del proyecto: WebFlux cambia de hilo durante el
 * pipeline reactivo, asi que el MDC ({@code ThreadLocal}) no sirve para propagar este valor. Por
 * eso se usa un atributo del exchange en lugar de MDC.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class CorrelationIdWebFilter implements WebFilter {

    public static final String CORRELATION_ID_HEADER = "X-Correlation-Id";
    public static final String CORRELATION_ID_ATTRIBUTE = "correlationId";

    private static final Logger log = LoggerFactory.getLogger(CorrelationIdWebFilter.class);

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String correlationId = exchange.getRequest().getHeaders().getFirst(CORRELATION_ID_HEADER);
        if (correlationId == null || correlationId.isBlank()) {
            correlationId = UUID.randomUUID().toString();
        }
        exchange.getAttributes().put(CORRELATION_ID_ATTRIBUTE, correlationId);
        exchange.getResponse().getHeaders().add(CORRELATION_ID_HEADER, correlationId);

        log.info("Request {} {} correlationId={}",
                exchange.getRequest().getMethod(), exchange.getRequest().getPath(), correlationId);

        return chain.filter(exchange);
    }
}
