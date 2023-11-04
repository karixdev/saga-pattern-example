package com.github.karixdev.orderservice.producer;

import com.github.karixdev.common.event.payment.PaymentInputEvent;
import com.github.karixdev.common.event.payment.PaymentInputEventType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.UUID;

@Component
public class PaymentInputEventProducer {

    private final KafkaTemplate<String, PaymentInputEvent> kafkaTemplate;
    private final String topic;

    public PaymentInputEventProducer(
            KafkaTemplate<String, PaymentInputEvent> kafkaTemplate,
            @Value("${topics.payment.input}") String topic
    ) {
        this.kafkaTemplate = kafkaTemplate;
        this.topic = topic;
    }

    public void producePaymentRequestEvent(UUID orderId, UUID userId, BigDecimal amount) {
        PaymentInputEvent event = PaymentInputEvent.builder()
                .type(PaymentInputEventType.PAYMENT_REQUEST)
                .orderId(orderId)
                .userId(userId)
                .amount(amount)
                .build();

        kafkaTemplate.send(topic, orderId.toString(), event);
    }

    public void producePaymentRevokeEvent(UUID orderId) {
        PaymentInputEvent event = PaymentInputEvent.builder()
                .type(PaymentInputEventType.PAYMENT_REVOKE)
                .orderId(orderId)
                .build();

        kafkaTemplate.send(topic, orderId.toString(), event);
    }

}
