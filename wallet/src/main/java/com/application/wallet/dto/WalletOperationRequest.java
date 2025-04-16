package com.application.wallet.dto;

import lombok.Data;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.util.UUID;

@Data
public class WalletOperationRequest {
    @NotNull(message = "Wallet ID is required")
    private UUID walletId;

    @NotNull(message = "Operation type is required")
    private OperationType operationType;

    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be positive")
    private BigDecimal amount;
} 