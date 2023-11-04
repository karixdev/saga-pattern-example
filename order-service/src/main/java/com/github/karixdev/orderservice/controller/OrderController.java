package com.github.karixdev.orderservice.controller;

import com.github.karixdev.common.dto.order.OrderDTO;
import com.github.karixdev.orderservice.entity.Order;
import com.github.karixdev.orderservice.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService service;

    @PostMapping
    ResponseEntity<Order> create(@RequestBody OrderDTO orderDTO) {
        return new ResponseEntity<>(service.create(orderDTO), HttpStatus.CREATED);
    }

    @GetMapping
    ResponseEntity<Collection<Order>> findAll() {
        return ResponseEntity.ok(service.findAll());
    }

}
