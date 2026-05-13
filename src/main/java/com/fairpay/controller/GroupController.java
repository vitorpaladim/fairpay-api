package com.fairpay.controller;

import com.fairpay.model.dto.BalanceResponse;
import com.fairpay.model.dto.GroupCreateRequest;
import com.fairpay.model.dto.GroupMemberAddRequest;
import com.fairpay.model.dto.GroupResponse;
import com.fairpay.service.GroupService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@Tag(name = "Groups")
@RestController
@RequestMapping("/groups")
public class GroupController {

    private final GroupService groupService;

    public GroupController(GroupService groupService) {
        this.groupService = groupService;
    }

    @Operation(summary = "Create a group owned by the authenticated user")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Group created"),
        @ApiResponse(responseCode = "400", description = "Invalid request")
    })
    @PostMapping
    public ResponseEntity<GroupResponse> create(
        Authentication authentication,
        @Valid @RequestBody GroupCreateRequest request
    ) {
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(groupService.createGroup(authentication.getName(), request));
    }

    @Operation(summary = "List groups where the authenticated user is a member")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Groups returned"),
        @ApiResponse(responseCode = "401", description = "Missing or invalid JWT")
    })
    @GetMapping
    public ResponseEntity<List<GroupResponse>> list(Authentication authentication) {
        return ResponseEntity.ok(groupService.listGroups(authentication.getName()));
    }

    @Operation(summary = "Get a group by id")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Group returned"),
        @ApiResponse(responseCode = "403", description = "User is not a group member"),
        @ApiResponse(responseCode = "404", description = "Group not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<GroupResponse> get(
        Authentication authentication,
        @PathVariable @Positive Long id
    ) {
        return ResponseEntity.ok(groupService.getGroup(authentication.getName(), id));
    }

    @Operation(summary = "Add a member to a group")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Member added"),
        @ApiResponse(responseCode = "403", description = "Only the owner can manage members"),
        @ApiResponse(responseCode = "404", description = "Group or user not found")
    })
    @PostMapping("/{id}/members")
    public ResponseEntity<GroupResponse> addMember(
        Authentication authentication,
        @PathVariable @Positive Long id,
        @Valid @RequestBody GroupMemberAddRequest request
    ) {
        return ResponseEntity.ok(groupService.addMember(authentication.getName(), id, request));
    }

    @Operation(summary = "Remove a member from a group")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Member removed"),
        @ApiResponse(responseCode = "403", description = "Only the owner can manage members"),
        @ApiResponse(responseCode = "404", description = "Group member not found")
    })
    @DeleteMapping("/{id}/members/{userId}")
    public ResponseEntity<Void> removeMember(
        Authentication authentication,
        @PathVariable @Positive Long id,
        @PathVariable @Positive Long userId
    ) {
        groupService.removeMember(authentication.getName(), id, userId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Return simplified debts for a group")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Balances returned"),
        @ApiResponse(responseCode = "403", description = "User is not a group member"),
        @ApiResponse(responseCode = "404", description = "Group not found")
    })
    @GetMapping("/{id}/balances")
    public ResponseEntity<BalanceResponse> balances(
        Authentication authentication,
        @PathVariable @Positive Long id
    ) {
        return ResponseEntity.ok(groupService.getBalances(authentication.getName(), id));
    }
}
