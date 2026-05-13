package com.fairpay.model.mapper;

import com.fairpay.model.dto.GroupMemberResponse;
import com.fairpay.model.dto.GroupResponse;
import com.fairpay.model.entity.Group;
import com.fairpay.model.entity.GroupMember;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class GroupMapper {

    private final UserMapper userMapper;

    public GroupMapper(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    public GroupResponse toResponse(Group group, List<GroupMember> members) {
        List<GroupMemberResponse> memberResponses = members.stream()
            .map(this::toMemberResponse)
            .toList();

        return new GroupResponse(
            group.getId(),
            group.getName(),
            group.getDescription(),
            group.getCreatedAt(),
            userMapper.toResponse(group.getOwner()),
            memberResponses
        );
    }

    private GroupMemberResponse toMemberResponse(GroupMember member) {
        return new GroupMemberResponse(
            userMapper.toResponse(member.getUser()),
            member.getRole(),
            member.getJoinedAt()
        );
    }
}
