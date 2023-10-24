package com.github.karixdev.warehouseservice;

import com.github.karixdev.common.dto.warehouse.ItemDTO;
import com.github.karixdev.common.event.warehouse.WarehouseOutputEvent;
import com.github.karixdev.common.event.warehouse.WarehouseOutputEventType;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

import java.math.BigDecimal;
import java.util.UUID;

@SpringBootApplication
public class WarehouseServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(WarehouseServiceApplication.class, args);
	}

	@Bean
	ProducerFactory<String, WarehouseOutputEvent> warehouseOutputEventProducerFactory(KafkaProperties properties) {
		return new DefaultKafkaProducerFactory<>(properties.buildProducerProperties());
	}

	@Bean
	KafkaTemplate<String, WarehouseOutputEvent> warehouseOutputEventKafkaTemplate(ProducerFactory<String, WarehouseOutputEvent> producerFactory) {
		return new KafkaTemplate<>(producerFactory);
	}

	@Bean
	CommandLineRunner commandLineRunner(KafkaTemplate<String, WarehouseOutputEvent> kafkaTemplate) {
		return args -> {
			kafkaTemplate.send(
					"warehouse-output",
					UUID.randomUUID().toString(),
					new WarehouseOutputEvent(
							WarehouseOutputEventType.ITEM_LOCKED,
							UUID.randomUUID(),
							new ItemDTO(
									UUID.fromString("afcf5438-4520-426e-b921-c4801bec1a6a"),
									new BigDecimal("20.01")
							)
					)
			);
		};
	}

}
