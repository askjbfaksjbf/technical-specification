package com.application.wallet.model;

import lombok.Data;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.UUID;

@Data
@Entity
@Table(name = "wallets")
public class Wallet {
    @Id
    @Column(name = "id")
    private UUID id;

    @Column(name = "balance", nullable = false)
    private BigDecimal balance;

    @Version
    private Long version;
} 