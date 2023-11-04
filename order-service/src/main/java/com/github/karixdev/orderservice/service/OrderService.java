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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository repository;
    private final WarehouseInputEventProducer warehouseInputEventProducer;
    private final PaymentInputEventProducer paymentInputEventProducer;

    @Transactional
    public Order create(OrderDTO orderDTO) {
        Order order = Order.builder()
                .itemId(orderDTO.itemId())
                .userId(orderDTO.userId())
                .status(OrderStatus.AWAITING_VERIFICATION)
                .build();

        repository.save(order);

        log.info("Created order {}. Sending LOCK_ITEM event", order.getId());
        warehouseInputEventProducer.sendItemLockEvent(order.getId(), order.getItemId());

        return order;
    }

    private Order findByIdOrElseThrow(UUID orderId) {
        return repository.findById(orderId).orElseThrow(() -> {
            log.error("Could not find order with id: {}", orderId);
            return new ResourceNotFoundException("Could not find order with id: %s".formatted(orderId));
        });
    }

    private void cancelOrder(UUID orderId) {
        try {
            Order order = findByIdOrElseThrow(orderId);
            order.setStatus(OrderStatus.CANCELED);

            log.info("Item from order {} is unavailable. Order is being canceled", orderId);

        } catch (ResourceNotFoundException ex) {
            log.error("Order {} cannot be canceled after item unavailable because it does not exist", orderId);
        }
    }

    private void processItemLocked(WarehouseOutputEvent event) {
        try {
            Order order = findByIdOrElseThrow(event.orderId());
            order.setStatus(OrderStatus.AWAITING_PAYMENT);

            log.info("Item from order {} locked. Sending PAYMENT_REQUEST event", event.orderId());
            paymentInputEventProducer.sendPaymentRequestEvent(order.getId(), order.getUserId(), event.itemDTO().price());

        } catch (ResourceNotFoundException ex) {
            log.error("Could not find order, item UNLOCK_ITEM is being sent");
            warehouseInputEventProducer.sendUnlockItemEvent(event.orderId());
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
        try {
            Order order = findByIdOrElseThrow(id);
            order.setStatus(OrderStatus.CANCELED);

            log.info("Payment for order {} failed. Canceling order and sending UNLOCK_ITEM event", id);
            warehouseInputEventProducer.sendUnlockItemEvent(id);

        } catch (ResourceNotFoundException ex) {
            log.error("Order {} cannot be canceled after payment failure because it does not exist", id);
        }
    }

    private void processPaymentSuccess(UUID id) {
        try {
            Order order = findByIdOrElseThrow(id);
            order.setStatus(OrderStatus.COMPLETED);

            log.info("Payment success for order {}. Sending DELETE_LOCK_AND_DECREMENT_COUNT event", id);
            warehouseInputEventProducer.sendDeleteLockAndDecrementCountEvent(id);

        } catch (ResourceNotFoundException ex) {
            log.error("Could not find order, item PAYMENT_REVOKE is being sent");
            paymentInputEventProducer.sendPaymentRevokeEvent(id);
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

    public Collection<Order> findAll() {
        return repository.findAll();
    }
}
