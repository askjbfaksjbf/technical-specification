package com.application.wallet.controller;

import com.application.wallet.dto.WalletOperationRequest;
import com.application.wallet.model.Wallet;
import com.application.wallet.repository.WalletRepository;
import com.application.wallet.service.WalletService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class WalletController {

    private final WalletService walletService;
    private final WalletRepository walletRepository;

    @PostMapping("/wallets")
    public ResponseEntity<UUID> createWallet() {
        Wallet wallet = new Wallet();
        wallet.setId(UUID.randomUUID());
        wallet.setBalance(BigDecimal.ZERO);
        wallet.setVersion(0L);
        walletRepository.save(wallet);
        return ResponseEntity.ok(wallet.getId());
    }

    @PostMapping("/wallet")
    public ResponseEntity<Void> processOperation(@Valid @RequestBody WalletOperationRequest request) {
        walletService.processOperation(request);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/wallets/{walletId}")
    public ResponseEntity<BigDecimal> getBalance(@PathVariable UUID walletId) {
        return ResponseEntity.ok(walletService.getBalance(walletId));
    }
} 