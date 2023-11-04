package com.github.karixdev.paymentservice.controller;

import com.github.karixdev.paymentservice.entity.BankAccount;
import com.github.karixdev.paymentservice.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;

@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
public class BankAccountController {

    private final PaymentService service;

    @GetMapping
    ResponseEntity<Collection<BankAccount>> findAll() {
        return ResponseEntity.ok(service.findAllBankAccounts());
    }

}
