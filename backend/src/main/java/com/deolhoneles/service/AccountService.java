package com.deolhoneles.service;

import com.deolhoneles.dto.AccountRequest;
import com.deolhoneles.dto.AccountResponse;
import com.deolhoneles.dto.DeputyResponse;
import com.deolhoneles.dto.PageResponse;
import com.deolhoneles.entity.Account;
import com.deolhoneles.entity.Deputy;
import com.deolhoneles.repository.AccountRepository;
import com.deolhoneles.repository.DeputyRepository;
import java.util.List;
import java.util.Set;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AccountService {

    private final AccountRepository accountRepository;
    private final DeputyRepository deputyRepository;

    public AccountService(AccountRepository accountRepository, DeputyRepository deputyRepository) {
        this.accountRepository = accountRepository;
        this.deputyRepository = deputyRepository;
    }

    @Transactional(readOnly = true)
    public PageResponse<AccountResponse> listAccounts(int page, int size) {
        Page<Account> result = accountRepository.findAll(PageRequest.of(page, size));
        return PageResponse.from(result, result.getContent().stream()
                .map(AccountResponse::from)
                .toList());
    }

    @Transactional(readOnly = true)
    public AccountResponse getAccount(Long id) {
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Account not found: " + id));
        return AccountResponse.from(account);
    }

    @Transactional
    public AccountResponse createAccount(AccountRequest request) {
        Account account = new Account();
        account.setName(request.name());
        account.setLastName(request.lastName());
        account.setEmail(request.email());
        accountRepository.save(account);
        return AccountResponse.from(account);
    }

    @Transactional
    public AccountResponse updateAccount(Long id, AccountRequest request) {
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Account not found: " + id));
        account.setName(request.name());
        account.setLastName(request.lastName());
        account.setEmail(request.email());
        accountRepository.save(account);
        return AccountResponse.from(account);
    }

    @Transactional
    public void deleteAccount(Long id) {
        if (!accountRepository.existsById(id)) {
            throw new RuntimeException("Account not found: " + id);
        }
        accountRepository.deleteById(id);
    }

    @Transactional
    public DeputyResponse toggleFollow(Long accountId, Long deputyId) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new RuntimeException("Account not found: " + accountId));
        Deputy deputy = deputyRepository.findById(deputyId)
                .orElseThrow(() -> new RuntimeException("Deputy not found: " + deputyId));

        Set<Deputy> followed = account.getFollowedDeputies();
        boolean nowFollowing;
        if (followed.contains(deputy)) {
            followed.remove(deputy);
            nowFollowing = false;
        } else {
            followed.add(deputy);
            nowFollowing = true;
        }
        accountRepository.save(account);

        return DeputyResponse.from(deputy, nowFollowing);
    }

    @Transactional(readOnly = true)
    public List<DeputyResponse> getFollowedDeputies(Long accountId) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new RuntimeException("Account not found: " + accountId));
        return account.getFollowedDeputies().stream()
                .map(d -> DeputyResponse.from(d, true))
                .toList();
    }

    @Transactional(readOnly = true)
    public Set<Long> getFollowedDeputyIds(Long accountId) {
        return accountRepository.findFollowedDeputyIds(accountId);
    }
}
