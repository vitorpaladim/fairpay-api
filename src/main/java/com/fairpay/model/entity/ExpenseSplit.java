package com.fairpay.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.math.BigDecimal;
import java.time.Instant;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(
    name = "expense_splits",
    uniqueConstraints = @UniqueConstraint(name = "uk_expense_splits_expense_user", columnNames = {"expense_id", "user_id"})
)
public class ExpenseSplit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "expense_id", nullable = false)
    private Expense expense;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "amount_owed", nullable = false, precision = 19, scale = 2)
    private BigDecimal amountOwed;

    @Column(nullable = false)
    private boolean settled;

    @Column(name = "settled_at")
    private Instant settledAt;

    public ExpenseSplit(User user, BigDecimal amountOwed) {
        this.user = user;
        this.amountOwed = amountOwed;
        this.settled = false;
    }
}
