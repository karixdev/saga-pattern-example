package com.github.karixdev.paymentservice.producer;

import com.github.karixdev.common.event.payment.PaymentOutputEvent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.UUID;

import static com.github.karixdev.common.event.payment.PaymentOutputEventType.PAYMENT_FAILED;
import static com.github.karixdev.common.event.payment.PaymentOutputEventType.PAYMENT_SUCCESS;

@Component
public class PaymentOutputEventProducer {

    private final String topic;
    private final KafkaTemplate<String, PaymentOutputEvent> kafkaTemplate;

    public PaymentOutputEventProducer(
            @Value("${topics.payment.output}") String topic,
            KafkaTemplate<String, PaymentOutputEvent> kafkaTemplate
    ) {
        this.topic = topic;
        this.kafkaTemplate = kafkaTemplate;
    }

    public void producePaymentFailedEvent(UUID orderId) {
        PaymentOutputEvent event = new PaymentOutputEvent(PAYMENT_FAILED, orderId);
        kafkaTemplate.send(topic, orderId.toString(), event);
    }

    public void producePaymentSuccessEvent(UUID orderId) {
        PaymentOutputEvent event = new PaymentOutputEvent(PAYMENT_SUCCESS, orderId);
        kafkaTemplate.send(topic, orderId.toString(), event);
    }

}
