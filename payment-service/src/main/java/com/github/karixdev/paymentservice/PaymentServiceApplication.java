package com.github.karixdev.paymentservice;

import com.github.karixdev.common.event.payment.PaymentOutputEvent;
import com.github.karixdev.common.event.payment.PaymentOutputEventType;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

import java.util.UUID;

@SpringBootApplication
public class PaymentServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(PaymentServiceApplication.class, args);
	}

	@Bean
	CommandLineRunner commandLineRunner(KafkaProperties properties) {
		return args -> {
			ProducerFactory<String, PaymentOutputEvent> factory = new DefaultKafkaProducerFactory<>(properties.buildProducerProperties());
			KafkaTemplate<String, PaymentOutputEvent> kafkaTemplate = new KafkaTemplate<>(factory);

			kafkaTemplate.send("payment-output", "64ecf470-ba7b-4f91-b0a6-3e1c0a85a671", new PaymentOutputEvent(
					PaymentOutputEventType.PAYMENT_SUCCESS,
					UUID.fromString("64ecf470-ba7b-4f91-b0a6-3e1c0a85a671")
			));
		};
	}

}
