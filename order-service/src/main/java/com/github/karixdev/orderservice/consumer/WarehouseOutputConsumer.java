package com.github.karixdev.orderservice.consumer;

import com.github.karixdev.common.event.warehouse.WarehouseOutputEvent;
import com.github.karixdev.orderservice.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class WarehouseOutputConsumer {

    private final OrderService orderService;

    @KafkaListener(topics = "${topics.warehouse.output}", groupId = "order-service")
    public void consumeWarehouseOutputEvent(ConsumerRecord<String, WarehouseOutputEvent> event) {
        log.info("Consumed warehouse output event: {}", event);
        orderService.handleWarehouseOutputEvent(event);
    }

}
