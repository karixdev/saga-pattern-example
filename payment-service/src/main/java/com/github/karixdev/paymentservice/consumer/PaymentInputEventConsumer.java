package com.github.karixdev.paymentservice.consumer;

import com.github.karixdev.common.event.payment.PaymentInputEvent;
import com.github.karixdev.paymentservice.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PaymentInputEventConsumer {

    private final PaymentService paymentService;

    @KafkaListener(topics = "${topics.payment.input}", groupId = "payment-service")
    public void consumePaymentInputEvent(ConsumerRecord<String, PaymentInputEvent> record) {
        paymentService.handlePaymentInputEvent(record);
    }

}
