package com.github.karixdev.warehouseservice.producer;

import com.github.karixdev.common.dto.warehouse.ItemDTO;
import com.github.karixdev.common.event.warehouse.WarehouseOutputEvent;
import com.github.karixdev.common.event.warehouse.WarehouseOutputEventType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class WarehouseOutputEventProducer {

    private final KafkaTemplate<String, WarehouseOutputEvent> kafkaTemplate;
    private final String topic;

    public WarehouseOutputEventProducer(
            KafkaTemplate<String, WarehouseOutputEvent> kafkaTemplate,
            @Value("${topics.warehouse.output}") String topic
    ) {
        this.kafkaTemplate = kafkaTemplate;
        this.topic = topic;
    }

    public void produceItemUnavailableEvent(String key, UUID orderId) {
        WarehouseOutputEvent event = new WarehouseOutputEvent(
                WarehouseOutputEventType.ITEM_UNAVAILABLE,
                orderId,
                null
        );
        kafkaTemplate.send(topic, key, event);
    }

    public void produceItemLockedEvent(String key, UUID orderId, ItemDTO itemDTO) {
        WarehouseOutputEvent event = new WarehouseOutputEvent(
                WarehouseOutputEventType.ITEM_LOCKED,
                orderId,
                itemDTO
        );
        kafkaTemplate.send(topic, key, event);
    }

}
