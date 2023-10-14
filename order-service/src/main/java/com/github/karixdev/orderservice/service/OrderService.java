package com.github.karixdev.orderservice.service;

import com.github.karixdev.common.event.warehouse.WarehouseInputEvent;
import com.github.karixdev.common.event.warehouse.WarehouseEventInputType;
import com.github.karixdev.common.dto.order.OrderDTO;
import com.github.karixdev.common.event.warehouse.WarehouseOutputEvent;
import com.github.karixdev.common.exception.ResourceNotFoundException;
import com.github.karixdev.orderservice.entity.Order;
import com.github.karixdev.common.dto.order.OrderStatus;
import com.github.karixdev.orderservice.repository.OrderRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@Service
public class OrderService {

    private final OrderRepository repository;
    private final KafkaTemplate<String, WarehouseInputEvent> kafkaTemplate;
    private final String warehouseInputTopic;

    public OrderService(
            OrderRepository repository,
            KafkaTemplate<String, WarehouseInputEvent> kafkaTemplate,
            @Value("${topics.warehouse.input}") String warehouseInputTopic
    ) {
        this.repository = repository;
        this.kafkaTemplate = kafkaTemplate;
        this.warehouseInputTopic = warehouseInputTopic;
    }

    @Transactional
    public OrderDTO create(OrderDTO orderDTO) {
        Order order = Order.builder()
                .itemId(orderDTO.itemId())
                .userId(orderDTO.userId())
                .status(OrderStatus.AWAITING_VERIFICATION)
                .build();

        repository.save(order);

        OrderDTO createdOrderDTO = mapToDTO(order);

        kafkaTemplate.send(
                warehouseInputTopic,
                order.getItemId().toString(),
                new WarehouseInputEvent(
                        WarehouseEventInputType.LOCK_ITEM,
                        createdOrderDTO
                )
        );

        return createdOrderDTO;
    }

    private OrderDTO mapToDTO(Order order) {
        return new OrderDTO(order.getId(), order.getItemId(), order.getUserId(), order.getStatus());
    }

    private Order findByIdOrElseThrow(UUID orderId) {
        return repository.findById(orderId).orElseThrow(() -> {
            log.error("Could not find order with id: {}", orderId);
            return new ResourceNotFoundException("Could not find order with id: %s".formatted(orderId));
        });
    }

    private void cancelOrder(UUID orderId) {
        Order order = findByIdOrElseThrow(orderId);
        order.setStatus(OrderStatus.CANCELED);
    }

    @Transactional
    public void handleWarehouseOutputEvent(ConsumerRecord<String, WarehouseOutputEvent> event) {
        WarehouseOutputEvent value = event.value();

        switch (value.type()) {
            case ITEM_UNAVAILABLE -> cancelOrder(value.orderId());
            default -> log.error("Could not handle: {}", event);
        }
    }
}
