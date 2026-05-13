package com.fairpay.service;

import com.fairpay.exception.BusinessException;
import com.fairpay.exception.ResourceNotFoundException;
import com.fairpay.model.dto.BalanceResponse;
import com.fairpay.model.dto.DebtResponse;
import com.fairpay.model.dto.GroupCreateRequest;
import com.fairpay.model.dto.GroupMemberAddRequest;
import com.fairpay.model.dto.GroupResponse;
import com.fairpay.model.entity.Expense;
import com.fairpay.model.entity.ExpenseSplit;
import com.fairpay.model.entity.Group;
import com.fairpay.model.entity.GroupMember;
import com.fairpay.model.entity.GroupMemberRole;
import com.fairpay.model.entity.User;
import com.fairpay.model.mapper.GroupMapper;
import com.fairpay.model.mapper.UserMapper;
import com.fairpay.repository.ExpenseSplitRepository;
import com.fairpay.repository.GroupMemberRepository;
import com.fairpay.repository.GroupRepository;
import com.fairpay.repository.UserRepository;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GroupService {

    private static final BigDecimal ZERO = BigDecimal.ZERO.setScale(2);

    private final GroupRepository groupRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final UserRepository userRepository;
    private final ExpenseSplitRepository expenseSplitRepository;
    private final GroupMapper groupMapper;
    private final UserMapper userMapper;

    public GroupService(
        GroupRepository groupRepository,
        GroupMemberRepository groupMemberRepository,
        UserRepository userRepository,
        ExpenseSplitRepository expenseSplitRepository,
        GroupMapper groupMapper,
        UserMapper userMapper
    ) {
        this.groupRepository = groupRepository;
        this.groupMemberRepository = groupMemberRepository;
        this.userRepository = userRepository;
        this.expenseSplitRepository = expenseSplitRepository;
        this.groupMapper = groupMapper;
        this.userMapper = userMapper;
    }

    @Transactional
    public GroupResponse createGroup(String currentEmail, GroupCreateRequest request) {
        User owner = getUserByEmail(currentEmail);
        Group group = groupRepository.save(new Group(
            request.name().trim(),
            request.description(),
            owner
        ));

        groupMemberRepository.save(new GroupMember(group, owner, GroupMemberRole.OWNER));
        return toResponse(group);
    }

    @Transactional(readOnly = true)
    public List<GroupResponse> listGroups(String currentEmail) {
        User currentUser = getUserByEmail(currentEmail);
        return groupRepository.findAllByMemberUserId(currentUser.getId()).stream()
            .map(this::toResponse)
            .toList();
    }

    @Transactional(readOnly = true)
    public GroupResponse getGroup(String currentEmail, Long groupId) {
        User currentUser = getUserByEmail(currentEmail);
        Group group = getGroupById(groupId);
        requireMember(group, currentUser);
        return toResponse(group);
    }

    @Transactional
    public GroupResponse addMember(String currentEmail, Long groupId, GroupMemberAddRequest request) {
        User currentUser = getUserByEmail(currentEmail);
        Group group = getGroupById(groupId);
        requireOwner(group, currentUser);

        User userToAdd = userRepository.findById(request.userId())
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (groupMemberRepository.existsByGroupIdAndUserId(group.getId(), userToAdd.getId())) {
            throw new BusinessException("User is already a group member");
        }

        groupMemberRepository.save(new GroupMember(group, userToAdd, GroupMemberRole.MEMBER));
        return toResponse(group);
    }

    @Transactional
    public void removeMember(String currentEmail, Long groupId, Long userId) {
        User currentUser = getUserByEmail(currentEmail);
        Group group = getGroupById(groupId);
        requireOwner(group, currentUser);

        if (group.getOwner().getId().equals(userId)) {
            throw new BusinessException("Group owner cannot be removed");
        }

        GroupMember member = groupMemberRepository.findByGroupIdAndUserId(groupId, userId)
            .orElseThrow(() -> new ResourceNotFoundException("Group member not found"));

        groupMemberRepository.delete(member);
    }

    @Transactional(readOnly = true)
    public BalanceResponse getBalances(String currentEmail, Long groupId) {
        User currentUser = getUserByEmail(currentEmail);
        Group group = getGroupById(groupId);
        requireMember(group, currentUser);

        List<ExpenseSplit> unsettledSplits = expenseSplitRepository.findByExpenseGroupIdAndSettledFalse(groupId);
        Map<Long, User> usersById = new HashMap<>();
        Map<Long, BigDecimal> netByUserId = new HashMap<>();

        for (ExpenseSplit split : unsettledSplits) {
            Expense expense = split.getExpense();
            User debtor = split.getUser();
            User creditor = expense.getPaidBy();

            usersById.put(debtor.getId(), debtor);
            usersById.put(creditor.getId(), creditor);
            netByUserId.putIfAbsent(debtor.getId(), ZERO);
            netByUserId.putIfAbsent(creditor.getId(), ZERO);

            if (!debtor.getId().equals(creditor.getId())) {
                BigDecimal amount = split.getAmountOwed();
                netByUserId.compute(debtor.getId(), (id, current) -> current.subtract(amount));
                netByUserId.compute(creditor.getId(), (id, current) -> current.add(amount));
            }
        }

        return new BalanceResponse(groupId, simplifyDebts(usersById, netByUserId));
    }

    private List<DebtResponse> simplifyDebts(Map<Long, User> usersById, Map<Long, BigDecimal> netByUserId) {
        List<BalanceSide> debtors = new ArrayList<>();
        List<BalanceSide> creditors = new ArrayList<>();

        netByUserId.forEach((userId, amount) -> {
            if (amount.signum() < 0) {
                debtors.add(new BalanceSide(usersById.get(userId), amount.abs()));
            } else if (amount.signum() > 0) {
                creditors.add(new BalanceSide(usersById.get(userId), amount));
            }
        });

        Comparator<BalanceSide> largestFirst = Comparator.comparing(BalanceSide::amount).reversed();
        debtors.sort(largestFirst);
        creditors.sort(largestFirst);

        List<DebtResponse> debts = new ArrayList<>();
        int debtorIndex = 0;
        int creditorIndex = 0;

        while (debtorIndex < debtors.size() && creditorIndex < creditors.size()) {
            BalanceSide debtor = debtors.get(debtorIndex);
            BalanceSide creditor = creditors.get(creditorIndex);
            BigDecimal amount = debtor.amount().min(creditor.amount());

            debts.add(new DebtResponse(
                userMapper.toResponse(debtor.user()),
                userMapper.toResponse(creditor.user()),
                amount
            ));

            debtor.subtract(amount);
            creditor.subtract(amount);

            if (debtor.isSettled()) {
                debtorIndex++;
            }
            if (creditor.isSettled()) {
                creditorIndex++;
            }
        }

        return debts;
    }

    private GroupResponse toResponse(Group group) {
        return groupMapper.toResponse(group, groupMemberRepository.findByGroupIdOrderByJoinedAtAsc(group.getId()));
    }

    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private Group getGroupById(Long groupId) {
        return groupRepository.findById(groupId)
            .orElseThrow(() -> new ResourceNotFoundException("Group not found"));
    }

    private void requireMember(Group group, User user) {
        if (!groupMemberRepository.existsByGroupIdAndUserId(group.getId(), user.getId())) {
            throw new AccessDeniedException("User is not a member of this group");
        }
    }

    private void requireOwner(Group group, User user) {
        if (!group.getOwner().getId().equals(user.getId())) {
            throw new AccessDeniedException("Only the group owner can manage members");
        }
    }

    private static class BalanceSide {

        private final User user;
        private BigDecimal amount;

        BalanceSide(User user, BigDecimal amount) {
            this.user = user;
            this.amount = amount;
        }

        User user() {
            return user;
        }

        BigDecimal amount() {
            return amount;
        }

        void subtract(BigDecimal value) {
            this.amount = amount.subtract(value);
        }

        boolean isSettled() {
            return amount.compareTo(ZERO) == 0;
        }
    }
}
