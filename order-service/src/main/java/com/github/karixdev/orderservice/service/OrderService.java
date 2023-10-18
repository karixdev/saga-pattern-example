package com.github.karixdev.orderservice.service;

import com.github.karixdev.common.dto.order.OrderDTO;
import com.github.karixdev.common.dto.order.OrderStatus;
import com.github.karixdev.common.event.payment.PaymentInputEvent;
import com.github.karixdev.common.event.payment.PaymentInputEventType;
import com.github.karixdev.common.event.payment.PaymentOutputEvent;
import com.github.karixdev.common.event.warehouse.WarehouseEventInputType;
import com.github.karixdev.common.event.warehouse.WarehouseInputEvent;
import com.github.karixdev.common.event.warehouse.WarehouseOutputEvent;
import com.github.karixdev.common.exception.ResourceNotFoundException;
import com.github.karixdev.orderservice.entity.Order;
import com.github.karixdev.orderservice.producer.PaymentInputEventProducer;
import com.github.karixdev.orderservice.producer.WarehouseInputEventProducer;
import com.github.karixdev.orderservice.repository.OrderRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@Service
public class OrderService {

    private final OrderRepository repository;
    private final WarehouseInputEventProducer warehouseInputEventProducer;
    private final PaymentInputEventProducer paymentInputEventProducer;

    public OrderService(
            OrderRepository repository,
            WarehouseInputEventProducer warehouseInputEventProducer,
            PaymentInputEventProducer paymentInputEventProducer
    ) {
        this.repository = repository;
        this.warehouseInputEventProducer = warehouseInputEventProducer;
        this.paymentInputEventProducer = paymentInputEventProducer;
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

        warehouseInputEventProducer.send(
                order.getId().toString(),
                new WarehouseInputEvent(
                        WarehouseEventInputType.LOCK_ITEM,
                        order.getId(),
                        order.getItemId()
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

    private void processItemLocked(WarehouseOutputEvent event) {
        try {
            Order order = findByIdOrElseThrow(event.orderId());
            order.setStatus(OrderStatus.AWAITING_PAYMENT);

            paymentInputEventProducer.send(
                    order.getId().toString(),
                    new PaymentInputEvent(
                            PaymentInputEventType.PAYMENT_REQUEST,
                            order.getId(),
                            order.getUserId(),
                            event.itemDTO().price()
                    )
            );
        } catch (ResourceNotFoundException ex) {
            log.error("Could not find order, item UNLOCK_ITEM is being sent");
            warehouseInputEventProducer.send(
                    event.orderId().toString(),
                    new WarehouseInputEvent(
                            WarehouseEventInputType.UNLOCK_ITEM,
                            event.orderId(),
                            null
                    )
            );
        }
    }

    @Transactional
    public void handleWarehouseOutputEvent(ConsumerRecord<String, WarehouseOutputEvent> record) {
        WarehouseOutputEvent value = record.value();

        switch (value.type()) {
            case ITEM_UNAVAILABLE -> cancelOrder(value.orderId());
            case ITEM_LOCKED -> processItemLocked(value);
            default -> log.error("Could not handle: {}", record);
        }
    }

    private void cancelOrderAfterPaymentFailed(UUID id) {
        Order order = findByIdOrElseThrow(id);
        order.setStatus(OrderStatus.CANCELED);

        warehouseInputEventProducer.send(
                order.getId().toString(),
                new WarehouseInputEvent(
                        WarehouseEventInputType.UNLOCK_ITEM,
                        order.getId(),
                        null
                )
        );
    }

    private void processPaymentSuccess(UUID id) {
        try {
            Order order = findByIdOrElseThrow(id);
            order.setStatus(OrderStatus.COMPLETED);

            warehouseInputEventProducer.send(
                    order.getId().toString(),
                    new WarehouseInputEvent(
                            WarehouseEventInputType.DELETE_LOCK_AND_DECREMENT_COUNT,
                            order.getId(),
                            null
                    )
            );
        } catch (ResourceNotFoundException ex) {
            paymentInputEventProducer.send(
                    id.toString(),
                    new PaymentInputEvent(
                            PaymentInputEventType.PAYMENT_REVOKE,
                            id,
                            null,
                            null
                    )
            );
        }
    }

    @Transactional
    public void handlePaymentOutputEvent(ConsumerRecord<String, PaymentOutputEvent> event) {
        PaymentOutputEvent value = event.value();

        switch (value.type()) {
            case PAYMENT_FAILED -> cancelOrderAfterPaymentFailed(value.orderId());
            case PAYMENT_SUCCESS -> processPaymentSuccess(value.orderId());
            default -> log.error("Could not handle: {}", event);
        }
    }

}
