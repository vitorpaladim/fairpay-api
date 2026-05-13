package com.fairpay.controller;

import com.fairpay.model.dto.ExpenseCreateRequest;
import com.fairpay.model.dto.ExpenseResponse;
import com.fairpay.service.ExpenseService;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@Tag(name = "Expenses")
@RestController
@RequestMapping("/expenses")
public class ExpenseController {

    private final ExpenseService expenseService;

    public ExpenseController(ExpenseService expenseService) {
        this.expenseService = expenseService;
    }

    @Operation(summary = "Create an expense and split it equally among group members")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Expense created"),
        @ApiResponse(responseCode = "403", description = "User is not a group member"),
        @ApiResponse(responseCode = "404", description = "Group not found")
    })
    @PostMapping
    public ResponseEntity<ExpenseResponse> create(
        Authentication authentication,
        @Valid @RequestBody ExpenseCreateRequest request
    ) {
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(expenseService.createExpense(authentication.getName(), request));
    }

    @Operation(summary = "List expenses by group")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Expenses returned"),
        @ApiResponse(responseCode = "403", description = "User is not a group member"),
        @ApiResponse(responseCode = "404", description = "Group not found")
    })
    @GetMapping
    public ResponseEntity<List<ExpenseResponse>> list(
        Authentication authentication,
        @RequestParam @Positive Long groupId
    ) {
        return ResponseEntity.ok(expenseService.listExpenses(authentication.getName(), groupId));
    }

    @Operation(summary = "Get an expense by id")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Expense returned"),
        @ApiResponse(responseCode = "403", description = "User is not a group member"),
        @ApiResponse(responseCode = "404", description = "Expense not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<ExpenseResponse> get(
        Authentication authentication,
        @PathVariable @Positive Long id
    ) {
        return ResponseEntity.ok(expenseService.getExpense(authentication.getName(), id));
    }

    @Operation(summary = "Settle the authenticated user's split for an expense")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Split settled"),
        @ApiResponse(responseCode = "403", description = "User is not a group member"),
        @ApiResponse(responseCode = "404", description = "Expense split not found")
    })
    @PostMapping("/{id}/settle")
    public ResponseEntity<ExpenseResponse> settle(
        Authentication authentication,
        @PathVariable @Positive Long id
    ) {
        return ResponseEntity.ok(expenseService.settleExpense(authentication.getName(), id));
    }
}
