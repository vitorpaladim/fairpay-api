package com.fairpay;

import static org.assertj.core.api.Assertions.assertThat;

import com.fairpay.model.dto.AuthResponse;
import com.fairpay.model.dto.ExpenseCreateRequest;
import com.fairpay.model.dto.ExpenseResponse;
import com.fairpay.model.dto.GroupCreateRequest;
import com.fairpay.model.dto.GroupMemberAddRequest;
import com.fairpay.model.dto.GroupMemberResponse;
import com.fairpay.model.dto.GroupResponse;
import com.fairpay.model.dto.LoginRequest;
import com.fairpay.model.dto.RegisterRequest;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class FairPayApplicationTests {

    private static final String PASSWORD = "strong-password";

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void shouldRegisterUserWithoutExposingPassword() {
        RegisteredUser registered = registerUser("register");

        assertThat(registered.token()).isNotBlank();
        assertThat(registered.id()).isPositive();
        assertThat(registered.email()).endsWith("@fairpay.test");
    }

    @Test
    void shouldLoginAndReturnJwt() {
        RegisteredUser registered = registerUser("login");

        ResponseEntity<AuthResponse> response = restTemplate.postForEntity(
            "/auth/login",
            new LoginRequest(registered.email(), PASSWORD),
            AuthResponse.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().token()).isNotBlank();
        assertThat(response.getBody().user().email()).isEqualTo(registered.email());
    }

    @Test
    void shouldCreateGroupWithOwnerMember() {
        RegisteredUser owner = registerUser("group-owner");

        ResponseEntity<GroupResponse> response = restTemplate.exchange(
            "/groups",
            HttpMethod.POST,
            authenticated(owner.token(), new GroupCreateRequest("Viagem", "Despesas da viagem")),
            GroupResponse.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().name()).isEqualTo("Viagem");
        assertThat(response.getBody().owner().email()).isEqualTo(owner.email());
        assertThat(response.getBody().members())
            .extracting(GroupMemberResponse::role)
            .containsExactly(com.fairpay.model.entity.GroupMemberRole.OWNER);
    }

    @Test
    void shouldCreateExpenseWithEqualSplitsForAllMembers() {
        RegisteredUser owner = registerUser("expense-owner");
        RegisteredUser member = registerUser("expense-member");
        GroupResponse group = createGroup(owner);

        ResponseEntity<GroupResponse> addMemberResponse = restTemplate.exchange(
            "/groups/" + group.id() + "/members",
            HttpMethod.POST,
            authenticated(owner.token(), new GroupMemberAddRequest(member.id())),
            GroupResponse.class
        );

        assertThat(addMemberResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(addMemberResponse.getBody()).isNotNull();
        assertThat(addMemberResponse.getBody().members()).hasSize(2);

        ResponseEntity<ExpenseResponse> expenseResponse = restTemplate.exchange(
            "/expenses",
            HttpMethod.POST,
            authenticated(
                owner.token(),
                new ExpenseCreateRequest(
                    group.id(),
                    "Jantar",
                    new BigDecimal("30.00"),
                    LocalDate.now()
                )
            ),
            ExpenseResponse.class
        );

        assertThat(expenseResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(expenseResponse.getBody()).isNotNull();
        assertThat(expenseResponse.getBody().totalAmount()).isEqualByComparingTo("30.00");
        assertThat(expenseResponse.getBody().splits()).hasSize(2);
        assertThat(expenseResponse.getBody().splits())
            .allSatisfy(split -> assertThat(split.amountOwed()).isEqualByComparingTo("15.00"));
    }

    private RegisteredUser registerUser(String prefix) {
        String email = prefix + "-" + UUID.randomUUID() + "@fairpay.test";
        ResponseEntity<AuthResponse> response = restTemplate.postForEntity(
            "/auth/register",
            new RegisterRequest("Test User", email, PASSWORD),
            AuthResponse.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().user()).isNotNull();

        return new RegisteredUser(
            response.getBody().user().id(),
            response.getBody().user().email(),
            response.getBody().token()
        );
    }

    private GroupResponse createGroup(RegisteredUser owner) {
        ResponseEntity<GroupResponse> response = restTemplate.exchange(
            "/groups",
            HttpMethod.POST,
            authenticated(owner.token(), new GroupCreateRequest("Casa", "Despesas compartilhadas")),
            GroupResponse.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        return response.getBody();
    }

    private <T> HttpEntity<T> authenticated(String token, T body) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        return new HttpEntity<>(body, headers);
    }

    private record RegisteredUser(Long id, String email, String token) {
    }
}
