package com.github.karixdev.warehouseservice.service;

import com.github.karixdev.common.dto.warehouse.ItemDTO;
import com.github.karixdev.common.event.warehouse.WarehouseEventInputType;
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

import java.util.Collection;
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

    private ItemLock findItemLockByOrderIdOrElseThrow(UUID orderId) {
        return itemLockRepository.findByOrderId(orderId).orElseThrow(() -> {
            log.error("Could not find item lock with order id: {}", orderId);
            return new ResourceNotFoundException("Could not find item lock with order id: %s".formatted(orderId));
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
                log.warn("Item {} is out of stock. Item UNAVAILABLE event is being sent.", event.itemId());
                warehouseOutputEventProducer.produceItemUnavailableEvent(event.orderId());
                return;
            }

            ItemLock itemLock = ItemLock.builder()
                    .item(item)
                    .orderId(event.orderId())
                    .build();
            itemLockRepository.save(itemLock);

            warehouseOutputEventProducer.produceItemLockedEvent(event.orderId(), mapToDTO(item));
            log.info("Item {} is locked. Item LOCKED event is being sent.", event.itemId());

        } catch (ResourceNotFoundException ex) {
            log.error("Could not find item. Item UNAVAILABLE event is being sent.");
            warehouseOutputEventProducer.produceItemUnavailableEvent(event.orderId());
        }
    }

    private void unlockItem(WarehouseInputEvent event) {
        itemLockRepository.deleteByOrderId(event.orderId());
        log.info("Removed lock from order {}", event.orderId());
    }

    private void deleteLockAndDecrementQuantity(WarehouseInputEvent event) {
        try {
            ItemLock itemLock = findItemLockByOrderIdOrElseThrow(event.orderId());
            Item item = itemLock.getItem();

            item.setQuantity(item.getQuantity() - 1);
            itemLockRepository.delete(itemLock);
        } catch (ResourceNotFoundException ex) {
            log.error("Could not find item lock from order {}", event.orderId());
        }
    }

    @Transactional
    public void handleWarehouseInputEvent(ConsumerRecord<String, WarehouseInputEvent> record) {
        WarehouseInputEvent event = record.value();

        switch (event.type()) {
            case LOCK_ITEM -> lockItem(event);
            case UNLOCK_ITEM -> unlockItem(event);
            case DELETE_LOCK_AND_DECREMENT_COUNT -> deleteLockAndDecrementQuantity(event);
            default -> log.error("Could not handle: {}", record);
        }
    }

    public Collection<Item> findAll() {
        return itemRepository.findAllWithLocks();
    }

}
