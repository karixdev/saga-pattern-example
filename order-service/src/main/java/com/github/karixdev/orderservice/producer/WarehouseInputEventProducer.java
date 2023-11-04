package com.github.karixdev.orderservice.producer;

import com.github.karixdev.common.event.warehouse.WarehouseEventInputType;
import com.github.karixdev.common.event.warehouse.WarehouseInputEvent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.UUID;

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

    public void produceItemLockEvent(UUID orderId, UUID itemId) {
        WarehouseInputEvent event = WarehouseInputEvent.builder().
                type(WarehouseEventInputType.LOCK_ITEM)
                .orderId(orderId)
                .itemId(itemId)
                .build();

        kafkaTemplate.send(topic, orderId.toString(), event);
    }

    public void produceUnlockItemEvent(UUID orderId) {
        WarehouseInputEvent event = WarehouseInputEvent.builder()
                .type(WarehouseEventInputType.UNLOCK_ITEM)
                .orderId(orderId)
                .build();

        kafkaTemplate.send(topic, orderId.toString(), event);
    }

    public void produceDeleteLockAndDecrementCountEvent(UUID orderId) {
        WarehouseInputEvent event = WarehouseInputEvent.builder()
                .type(WarehouseEventInputType.DELETE_LOCK_AND_DECREMENT_COUNT)
                .orderId(orderId)
                .build();

        kafkaTemplate.send(topic, orderId.toString(), event);
    }

}
