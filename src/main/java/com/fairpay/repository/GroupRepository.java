package com.fairpay.repository;

import com.fairpay.model.entity.Group;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface GroupRepository extends JpaRepository<Group, Long> {

    @Query("select gm.group from GroupMember gm where gm.user.id = :userId order by gm.group.createdAt desc")
    List<Group> findAllByMemberUserId(@Param("userId") Long userId);
}
