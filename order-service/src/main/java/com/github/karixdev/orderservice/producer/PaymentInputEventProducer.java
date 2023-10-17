package com.github.karixdev.orderservice.producer;

import com.github.karixdev.common.event.payment.PaymentInputEvent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class PaymentInputEventProducer {

    private final KafkaTemplate<String, PaymentInputEvent> kafkaTemplate;
    private final String topic;

    public PaymentInputEventProducer(
            KafkaTemplate<String, PaymentInputEvent> kafkaTemplate,
            @Value("${topics.payment.input}") String topic
    ) {
        this.kafkaTemplate = kafkaTemplate;
        this.topic = topic;
    }

    public void send(String key, PaymentInputEvent value) {
        kafkaTemplate.send(topic, key, value);
    }

}
