package com.application.wallet.service;

import com.application.wallet.dto.WalletOperationRequest;
import com.application.wallet.dto.OperationType;
import com.application.wallet.exception.WalletNotFoundException;
import com.application.wallet.exception.InsufficientFundsException;
import com.application.wallet.model.Wallet;
import com.application.wallet.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.persistence.OptimisticLockException;
import jakarta.persistence.PessimisticLockException;
import java.math.BigDecimal;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class WalletService {
    private final WalletRepository walletRepository;

    @Transactional
    public UUID createWallet() {
        Wallet wallet = new Wallet();
        wallet.setId(UUID.randomUUID());
        wallet.setBalance(BigDecimal.ZERO);
        wallet.setVersion(0L);
        walletRepository.save(wallet);
        return wallet.getId();
    }

    @Transactional
    @Retryable(
        value = {
            OptimisticLockException.class,
            PessimisticLockException.class,
            CannotAcquireLockException.class
        },
        maxAttempts = 3,
        backoff = @Backoff(delay = 100, multiplier = 2)
    )
    public void processOperation(WalletOperationRequest request) {
        try {
            log.debug("Processing operation for wallet: {}, type: {}, amount: {}", 
                     request.getWalletId(), request.getOperationType(), request.getAmount());

            Wallet wallet = walletRepository.findByIdWithLock(request.getWalletId())
                    .orElseThrow(() -> new WalletNotFoundException("Wallet not found"));

            BigDecimal newBalance;
            if (request.getOperationType() == OperationType.DEPOSIT) {
                newBalance = wallet.getBalance().add(request.getAmount());
            } else {
                if (wallet.getBalance().compareTo(request.getAmount()) < 0) {
                    throw new InsufficientFundsException("Insufficient funds");
                }
                newBalance = wallet.getBalance().subtract(request.getAmount());
            }

            wallet.setBalance(newBalance);
            walletRepository.save(wallet);
            
            log.info("Successfully processed operation for wallet: {}, type: {}, amount: {}, new balance: {}", 
                     request.getWalletId(), request.getOperationType(), request.getAmount(), newBalance);
        } catch (Exception e) {
            log.error("Error processing operation for wallet: {}, type: {}, amount: {}", 
                      request.getWalletId(), request.getOperationType(), request.getAmount(), e);
            throw e;
        }
    }

    @Transactional(readOnly = true)
    public BigDecimal getBalance(UUID walletId) {
        return walletRepository.findById(walletId)
                .map(Wallet::getBalance)
                .orElseThrow(() -> new WalletNotFoundException("Wallet not found"));
    }
} 