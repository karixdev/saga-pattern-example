package com.github.karixdev.warehouseservice.repository;

import com.github.karixdev.warehouseservice.entity.Item;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Repository
public interface ItemRepository extends JpaRepository<Item, UUID> {

    @Query("""
            SELECT item
            FROM Item item
            LEFT JOIN FETCH item.itemLocks
            """)
    List<Item> findAllWithLocks();

}
