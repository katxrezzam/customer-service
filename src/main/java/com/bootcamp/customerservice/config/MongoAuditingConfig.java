package com.bootcamp.customerservice.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.EnableReactiveMongoAuditing;

/**
 * Habilita el llenado automatico de {@code @CreatedDate}/{@code @LastModifiedDate} en las
 * entidades reactivas de Mongo.
 *
 * <p>Leccion aprendida de la iteracion anterior del proyecto: esta anotacion puesta directo en la
 * clase {@code @SpringBootApplication} rompia los slice tests ({@code @WebFluxTest}), porque
 * esos tests no levantan el contexto de Mongo que esta anotacion necesita. Por eso vive en una
 * clase de configuracion aparte, para poder excluirla de esos tests con
 * {@code @WebFluxTest(controllers = ..., excludeAutoConfiguration = ...)} si hiciera falta.
 */
@Configuration
@EnableReactiveMongoAuditing
public class MongoAuditingConfig {
}
