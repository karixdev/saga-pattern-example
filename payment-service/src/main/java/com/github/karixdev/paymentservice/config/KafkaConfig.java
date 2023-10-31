package com.github.karixdev.paymentservice.config;

import com.github.karixdev.common.event.payment.PaymentInputEvent;
import com.github.karixdev.common.event.payment.PaymentOutputEvent;
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
    ConsumerFactory<String, PaymentInputEvent> paymentInputEventConsumerFactory(KafkaProperties properties) {
        return new DefaultKafkaConsumerFactory<>(properties.buildConsumerProperties());
    }

    @Bean
    ConcurrentKafkaListenerContainerFactory<String, PaymentInputEvent> paymentInputEventConcurrentKafkaListenerContainerFactory(
            ConsumerFactory<String, PaymentInputEvent> consumerFactory
    ) {
        var factory = new ConcurrentKafkaListenerContainerFactory<String, PaymentInputEvent>();
        factory.setConsumerFactory(consumerFactory);

        return factory;
    }

    @Bean
    ProducerFactory<String, PaymentOutputEvent> paymentOutputEventProducerFactory(KafkaProperties properties) {
        return new DefaultKafkaProducerFactory<>(properties.buildProducerProperties());
    }

    @Bean
    KafkaTemplate<String, PaymentOutputEvent> paymentOutputEventKafkaTemplate(ProducerFactory<String, PaymentOutputEvent> factory) {
        return new KafkaTemplate<>(factory);
    }

}
