package com.github.karixdev.orderservice.config;

import com.github.karixdev.common.event.warehouse.WarehouseEvent;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

@Configuration
public class KafkaConfig {

    @Bean
    ProducerFactory<String, WarehouseEvent> orderDTOProducerFactory(KafkaProperties properties) {
        return new DefaultKafkaProducerFactory<>(properties.buildProducerProperties());
    }

    @Bean
    KafkaTemplate<String, WarehouseEvent> orderDTOKafkaTemplate(ProducerFactory<String, WarehouseEvent> producerFactory) {
        return new KafkaTemplate<>(producerFactory);
    }

}