package com.github.karixdev.orderservice.service;

import com.github.karixdev.common.event.warehouse.WarehouseEvent;
import com.github.karixdev.common.event.warehouse.WarehouseEventType;
import com.github.karixdev.common.dto.order.OrderDTO;
import com.github.karixdev.orderservice.entity.Order;
import com.github.karixdev.common.dto.order.OrderStatus;
import com.github.karixdev.orderservice.repository.OrderRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class OrderService {

    private final OrderRepository repository;
    private final KafkaTemplate<String, WarehouseEvent> kafkaTemplate;
    private final String warehouseInputTopic;

    public OrderService(
            OrderRepository repository,
            KafkaTemplate<String, WarehouseEvent> kafkaTemplate,
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
                .status(OrderStatus.AWAITING_VERIFICATION)
                .build();

        repository.save(order);

        OrderDTO createdOrderDTO = new OrderDTO(order.getId(), order.getItemId(), order.getStatus());

        kafkaTemplate.send(
                warehouseInputTopic,
                order.getItemId().toString(),
                new WarehouseEvent(
                        WarehouseEventType.LOCK_ITEM,
                        createdOrderDTO
                )
        );

        return createdOrderDTO;
    }

}
