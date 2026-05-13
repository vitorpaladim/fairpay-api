package com.fairpay.repository;

import com.fairpay.model.entity.ExpenseSplit;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExpenseSplitRepository extends JpaRepository<ExpenseSplit, Long> {

    Optional<ExpenseSplit> findByExpenseIdAndUserId(Long expenseId, Long userId);

    List<ExpenseSplit> findByExpenseGroupIdAndSettledFalse(Long groupId);
}
