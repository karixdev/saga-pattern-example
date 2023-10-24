package com.github.karixdev.warehouseservice.service;

import com.github.karixdev.common.dto.warehouse.ItemDTO;
import com.github.karixdev.common.event.warehouse.WarehouseInputEvent;
import com.github.karixdev.common.exception.ResourceNotFoundException;
import com.github.karixdev.warehouseservice.entity.Item;
import com.github.karixdev.warehouseservice.entity.ItemLock;
import com.github.karixdev.warehouseservice.producer.WarehouseOutputEventProducer;
import com.github.karixdev.warehouseservice.repository.ItemLockRepository;
import com.github.karixdev.warehouseservice.repository.ItemRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ItemService {

    private final ItemRepository itemRepository;
    private final ItemLockRepository itemLockRepository;
    private final WarehouseOutputEventProducer warehouseOutputEventProducer;

    private Item findItemByIdOrElseThrow(UUID id) {
        return itemRepository.findById(id).orElseThrow(() -> {
            log.error("Could not find item with id: {}", id);
            return new ResourceNotFoundException("Could not find item with id: %s".formatted(id));
        });
    }

    private ItemDTO mapToDTO(Item item) {
        return new ItemDTO(item.getId(), item.getPrice());
    }

    public void lockItem(WarehouseInputEvent event) {
        try {
            Item item = findItemByIdOrElseThrow(event.itemId());

            int itemLocksCount = itemLockRepository.countItemLocksByItemId(item.getId());
            int countOfAvailable = item.getQuantity() - itemLocksCount;

            if (countOfAvailable <= 0) {
                warehouseOutputEventProducer.produceItemUnavailableEvent(event.orderId().toString(), event.orderId());
                return;
            }

            ItemLock itemLock = ItemLock.builder()
                    .item(item)
                    .orderId(event.orderId())
                    .build();
            itemLockRepository.save(itemLock);

            warehouseOutputEventProducer.produceItemLockedEvent(
                    event.orderId().toString(),
                    event.orderId(),
                    mapToDTO(item)
            );

        } catch (ResourceNotFoundException ex) {
            warehouseOutputEventProducer.produceItemUnavailableEvent(event.orderId().toString(), event.orderId());
        }
    }

    private void unlockItem(WarehouseInputEvent event) {
        itemLockRepository.deleteByOrderId(event.orderId());
    }

    @Transactional
    public void handleWarehouseInputEvent(ConsumerRecord<String, WarehouseInputEvent> record) {
        WarehouseInputEvent event = record.value();

        switch (event.type()) {
            case LOCK_ITEM -> lockItem(event);
            case UNLOCK_ITEM -> unlockItem(event);
            default -> log.error("Could not handle: {}", record);
        }
    }
}
