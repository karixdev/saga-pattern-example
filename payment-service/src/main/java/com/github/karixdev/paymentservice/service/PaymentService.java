package com.github.karixdev.paymentservice.service;

import com.github.karixdev.common.event.payment.PaymentInputEvent;
import com.github.karixdev.common.event.payment.PaymentOutputEvent;
import com.github.karixdev.common.exception.ResourceNotFoundException;
import com.github.karixdev.paymentservice.entity.BankAccount;
import com.github.karixdev.paymentservice.entity.Payment;
import com.github.karixdev.paymentservice.producer.PaymentOutputEventProducer;
import com.github.karixdev.paymentservice.repository.BankAccountRepository;
import com.github.karixdev.paymentservice.repository.PaymentRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final BankAccountRepository bankAccountRepository;
    private final PaymentRepository paymentRepository;
    private final PaymentOutputEventProducer producer;

    private BankAccount findBankAccountByUserId(UUID userId) {
        return bankAccountRepository.findByUserId(userId).orElseThrow(() -> {
            String message = "Could not find bank account with user id: %s".formatted(userId);
            log.error(message);
            return new ResourceNotFoundException(message);
        });
    }

    private void handlePaymentRequest(PaymentInputEvent event) {
        try {
            BankAccount bankAccount = findBankAccountByUserId(event.userId());

            BigDecimal newBalance = bankAccount.getBalance().subtract(event.amount());
            if (newBalance.compareTo(new BigDecimal(0)) < 0) {
                producer.producePaymentFailedEvent(event.orderId());
                return;
            }

            bankAccount.setBalance(newBalance);

            Payment payment = Payment.builder()
                    .orderId(event.orderId())
                    .amount(event.amount())
                    .build();

            paymentRepository.save(payment);

            producer.producePaymentSuccessEvent(event.orderId());

        } catch (ResourceNotFoundException ex) {
            producer.producePaymentFailedEvent(event.orderId());
        }
    }

    @Transactional
    public void handlePaymentInputEvent(ConsumerRecord<String, PaymentInputEvent> record) {
        PaymentInputEvent event = record.value();

        switch (event.type()) {
            case PAYMENT_REQUEST -> handlePaymentRequest(event);
            default -> log.error("Could not handle: {}", record);
        }
    }


}
