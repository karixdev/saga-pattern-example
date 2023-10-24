package com.github.karixdev.warehouseservice.consumer;

import com.github.karixdev.common.event.warehouse.WarehouseInputEvent;
import com.github.karixdev.warehouseservice.service.ItemService;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class WarehouseInputEventConsumer {

    private final ItemService itemService;

    @KafkaListener(topics = "${topics.warehouse.input}", groupId = "warehouse-service")
    public void consumeWarehouseInputEvent(ConsumerRecord<String, WarehouseInputEvent> record) {
        itemService.handleWarehouseInputEvent(record);
    }

}
