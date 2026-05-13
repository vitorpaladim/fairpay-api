package com.fairpay.service;

import com.fairpay.exception.ResourceNotFoundException;
import com.fairpay.model.dto.ExpenseCreateRequest;
import com.fairpay.model.dto.ExpenseResponse;
import com.fairpay.model.entity.Expense;
import com.fairpay.model.entity.ExpenseSplit;
import com.fairpay.model.entity.Group;
import com.fairpay.model.entity.GroupMember;
import com.fairpay.model.entity.User;
import com.fairpay.model.mapper.ExpenseMapper;
import com.fairpay.repository.ExpenseRepository;
import com.fairpay.repository.ExpenseSplitRepository;
import com.fairpay.repository.GroupMemberRepository;
import com.fairpay.repository.GroupRepository;
import com.fairpay.repository.UserRepository;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.List;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final ExpenseSplitRepository expenseSplitRepository;
    private final GroupRepository groupRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final UserRepository userRepository;
    private final ExpenseMapper expenseMapper;

    public ExpenseService(
        ExpenseRepository expenseRepository,
        ExpenseSplitRepository expenseSplitRepository,
        GroupRepository groupRepository,
        GroupMemberRepository groupMemberRepository,
        UserRepository userRepository,
        ExpenseMapper expenseMapper
    ) {
        this.expenseRepository = expenseRepository;
        this.expenseSplitRepository = expenseSplitRepository;
        this.groupRepository = groupRepository;
        this.groupMemberRepository = groupMemberRepository;
        this.userRepository = userRepository;
        this.expenseMapper = expenseMapper;
    }

    @Transactional
    public ExpenseResponse createExpense(String currentEmail, ExpenseCreateRequest request) {
        User paidBy = getUserByEmail(currentEmail);
        Group group = getGroupById(request.groupId());
        requireMember(group, paidBy);

        List<GroupMember> members = groupMemberRepository.findByGroupIdOrderByJoinedAtAsc(group.getId());
        BigDecimal totalAmount = request.totalAmount().setScale(2, RoundingMode.HALF_UP);
        List<BigDecimal> splitAmounts = calculateEqualSplits(totalAmount, members.size());

        Expense expense = new Expense(
            group,
            paidBy,
            request.description().trim(),
            totalAmount,
            request.date()
        );

        for (int index = 0; index < members.size(); index++) {
            expense.addSplit(new ExpenseSplit(members.get(index).getUser(), splitAmounts.get(index)));
        }

        return expenseMapper.toResponse(expenseRepository.save(expense));
    }

    @Transactional(readOnly = true)
    public List<ExpenseResponse> listExpenses(String currentEmail, Long groupId) {
        User currentUser = getUserByEmail(currentEmail);
        Group group = getGroupById(groupId);
        requireMember(group, currentUser);

        return expenseRepository.findByGroupIdOrderByDateDescCreatedAtDesc(groupId).stream()
            .map(expenseMapper::toResponse)
            .toList();
    }

    @Transactional(readOnly = true)
    public ExpenseResponse getExpense(String currentEmail, Long expenseId) {
        User currentUser = getUserByEmail(currentEmail);
        Expense expense = getExpenseById(expenseId);
        requireMember(expense.getGroup(), currentUser);
        return expenseMapper.toResponse(expense);
    }

    @Transactional
    public ExpenseResponse settleExpense(String currentEmail, Long expenseId) {
        User currentUser = getUserByEmail(currentEmail);
        Expense expense = getExpenseById(expenseId);
        requireMember(expense.getGroup(), currentUser);

        ExpenseSplit split = expenseSplitRepository.findByExpenseIdAndUserId(expenseId, currentUser.getId())
            .orElseThrow(() -> new ResourceNotFoundException("Expense split not found for current user"));

        if (!split.isSettled()) {
            split.setSettled(true);
            split.setSettledAt(Instant.now());
        }

        return expenseMapper.toResponse(expense);
    }

    private List<BigDecimal> calculateEqualSplits(BigDecimal totalAmount, int membersCount) {
        BigInteger cents = totalAmount.movePointRight(2).toBigIntegerExact();
        BigInteger[] division = cents.divideAndRemainder(BigInteger.valueOf(membersCount));
        BigInteger base = division[0];
        int remainder = division[1].intValue();

        return java.util.stream.IntStream.range(0, membersCount)
            .mapToObj(index -> base.add(index < remainder ? BigInteger.ONE : BigInteger.ZERO))
            .map(value -> new BigDecimal(value).movePointLeft(2).setScale(2))
            .toList();
    }

    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private Group getGroupById(Long groupId) {
        return groupRepository.findById(groupId)
            .orElseThrow(() -> new ResourceNotFoundException("Group not found"));
    }

    private Expense getExpenseById(Long expenseId) {
        return expenseRepository.findById(expenseId)
            .orElseThrow(() -> new ResourceNotFoundException("Expense not found"));
    }

    private void requireMember(Group group, User user) {
        if (!groupMemberRepository.existsByGroupIdAndUserId(group.getId(), user.getId())) {
            throw new AccessDeniedException("User is not a member of this group");
        }
    }
}
