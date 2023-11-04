package com.github.karixdev.warehouseservice.controller;

import com.github.karixdev.warehouseservice.entity.Item;
import com.github.karixdev.warehouseservice.service.ItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;

@RestController
@RequestMapping("/api/items")
@RequiredArgsConstructor
public class ItemController {

    private final ItemService service;

    @GetMapping
    ResponseEntity<Collection<Item>> findAll() {
        return ResponseEntity.ok(service.findAll());
    }

}
