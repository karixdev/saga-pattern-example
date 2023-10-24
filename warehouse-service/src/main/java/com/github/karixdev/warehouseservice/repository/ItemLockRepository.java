package com.github.karixdev.warehouseservice.repository;

import com.github.karixdev.warehouseservice.entity.ItemLock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ItemLockRepository extends JpaRepository<ItemLock, UUID> {
    @Query("""
            SELECT COUNT(itemLock)
            FROM ItemLock itemLock
            WHERE itemLock.item.id = :id
            """)
    Integer countItemLocksByItemId(UUID id);
}
