package com.github.karixdev.orderservice.producer;

import com.github.karixdev.common.event.warehouse.WarehouseInputEvent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class WarehouseInputEventProducer {

    private final KafkaTemplate<String, WarehouseInputEvent> kafkaTemplate;
    private final String topic;

    public WarehouseInputEventProducer(
            KafkaTemplate<String, WarehouseInputEvent> kafkaTemplate,
            @Value("${topics.warehouse.input}") String topic
    ) {
        this.kafkaTemplate = kafkaTemplate;
        this.topic = topic;
    }

    public void send(String key, WarehouseInputEvent value) {
        kafkaTemplate.send(topic, key, value);
    }

}
