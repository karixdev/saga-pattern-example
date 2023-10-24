package com.github.karixdev.warehouseservice.config;

import com.github.karixdev.common.event.warehouse.WarehouseInputEvent;
import com.github.karixdev.common.event.warehouse.WarehouseOutputEvent;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

@Configuration
public class KafkaConfig {

    @Bean
    ConsumerFactory<String, WarehouseInputEvent> warehouseInputEventConsumerFactory(KafkaProperties properties) {
        return new DefaultKafkaConsumerFactory<>(properties.buildConsumerProperties());
    }

    @Bean
    ConcurrentKafkaListenerContainerFactory<String, WarehouseInputEvent> warehouseInputEventConcurrentKafkaListenerContainerFactory(
            ConsumerFactory<String, WarehouseInputEvent> consumerFactory
    ) {
        var factory = new ConcurrentKafkaListenerContainerFactory<String, WarehouseInputEvent>();
        factory.setConsumerFactory(consumerFactory);

        return factory;
    }

    @Bean
    ProducerFactory<String, WarehouseOutputEvent> warehouseOutputEventProducerFactory(KafkaProperties properties) {
        return new DefaultKafkaProducerFactory<>(properties.buildProducerProperties());
    }

    @Bean
    KafkaTemplate<String, WarehouseOutputEvent> warehouseOutputEventKafkaTemplate(ProducerFactory<String, WarehouseOutputEvent> producerFactory) {
        return new KafkaTemplate<>(producerFactory);
    }

}
