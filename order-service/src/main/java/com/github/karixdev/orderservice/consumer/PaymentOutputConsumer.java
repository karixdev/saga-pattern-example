package com.github.karixdev.orderservice.consumer;

import com.github.karixdev.common.event.payment.PaymentOutputEvent;
import com.github.karixdev.orderservice.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PaymentOutputConsumer {

    private final OrderService orderService;

    @KafkaListener(topics = "${topics.payment.output}", groupId = "order-service")
    public void consumePaymentOutputEvent(ConsumerRecord<String, PaymentOutputEvent> event) {
        orderService.handlePaymentOutputEvent(event);
    }

}
