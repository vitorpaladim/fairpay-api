package com.fairpay.repository;

import com.fairpay.model.entity.Expense;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExpenseRepository extends JpaRepository<Expense, Long> {

    List<Expense> findByGroupIdOrderByDateDescCreatedAtDesc(Long groupId);
}
