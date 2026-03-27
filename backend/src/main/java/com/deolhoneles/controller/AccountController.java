package com.deolhoneles.controller;

import com.deolhoneles.dto.AccountRequest;
import com.deolhoneles.dto.AccountResponse;
import com.deolhoneles.dto.DeputyResponse;
import com.deolhoneles.dto.PageResponse;
import com.deolhoneles.service.AccountService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/accounts")
public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @GetMapping
    public PageResponse<AccountResponse> listAccounts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return accountService.listAccounts(page, size);
    }

    @GetMapping("/{id}")
    public AccountResponse getAccount(@PathVariable Long id) {
        return accountService.getAccount(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AccountResponse createAccount(@Valid @RequestBody AccountRequest request) {
        return accountService.createAccount(request);
    }

    @PutMapping("/{id}")
    public AccountResponse updateAccount(@PathVariable Long id, @Valid @RequestBody AccountRequest request) {
        return accountService.updateAccount(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteAccount(@PathVariable Long id) {
        accountService.deleteAccount(id);
    }

    @PutMapping("/{accountId}/deputies/{deputyId}/follow")
    public DeputyResponse toggleFollow(@PathVariable Long accountId, @PathVariable Long deputyId) {
        return accountService.toggleFollow(accountId, deputyId);
    }

    @GetMapping("/{accountId}/deputies")
    public List<DeputyResponse> getFollowedDeputies(@PathVariable Long accountId) {
        return accountService.getFollowedDeputies(accountId);
    }
}
