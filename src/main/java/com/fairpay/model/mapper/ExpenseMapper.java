package com.fairpay.model.mapper;

import com.fairpay.model.dto.ExpenseResponse;
import com.fairpay.model.dto.ExpenseSplitResponse;
import com.fairpay.model.entity.Expense;
import com.fairpay.model.entity.ExpenseSplit;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class ExpenseMapper {

    private final UserMapper userMapper;

    public ExpenseMapper(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    public ExpenseResponse toResponse(Expense expense) {
        List<ExpenseSplitResponse> splits = expense.getSplits().stream()
            .map(this::toSplitResponse)
            .toList();

        return new ExpenseResponse(
            expense.getId(),
            expense.getGroup().getId(),
            userMapper.toResponse(expense.getPaidBy()),
            expense.getDescription(),
            expense.getTotalAmount(),
            expense.getDate(),
            expense.getCreatedAt(),
            splits
        );
    }

    private ExpenseSplitResponse toSplitResponse(ExpenseSplit split) {
        return new ExpenseSplitResponse(
            split.getId(),
            userMapper.toResponse(split.getUser()),
            split.getAmountOwed(),
            split.isSettled(),
            split.getSettledAt()
        );
    }
}
