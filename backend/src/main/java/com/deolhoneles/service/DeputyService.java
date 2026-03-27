package com.deolhoneles.service;

import com.deolhoneles.dto.DeputyRequest;
import com.deolhoneles.dto.DeputyResponse;
import com.deolhoneles.dto.PageResponse;
import com.deolhoneles.entity.Deputy;
import com.deolhoneles.repository.AccountRepository;
import com.deolhoneles.repository.DeputyRepository;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DeputyService {

    private final DeputyRepository deputyRepository;
    private final AccountRepository accountRepository;

    public DeputyService(DeputyRepository deputyRepository, AccountRepository accountRepository) {
        this.deputyRepository = deputyRepository;
        this.accountRepository = accountRepository;
    }

    @Transactional(readOnly = true)
    public PageResponse<DeputyResponse> listDeputies(Long accountId, Boolean followed, int page, int size) {
        Set<Long> followedIds = accountId != null
                ? accountRepository.findFollowedDeputyIds(accountId)
                : Collections.emptySet();

        Page<Deputy> result = deputyRepository.findAll(PageRequest.of(page, size));

        var mapped = result.getContent().stream()
                .map(d -> toResponse(d, followedIds))
                .toList();

        if (followed != null) {
            var filtered = mapped.stream()
                    .filter(d -> d.follow() == followed)
                    .toList();
            return new PageResponse<>(filtered, page, size,
                    filtered.size(), 1, true);
        }

        return PageResponse.from(result, mapped);
    }

    @Transactional(readOnly = true)
    public DeputyResponse getDeputy(Long id, Long accountId) {
        Deputy deputy = deputyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Deputy not found: " + id));
        Set<Long> followedIds = accountId != null
                ? accountRepository.findFollowedDeputyIds(accountId)
                : Collections.emptySet();
        return toResponse(deputy, followedIds);
    }

    @Transactional(readOnly = true)
    public Optional<DeputyResponse> findByExternalId(Long externalId) {
        return deputyRepository.findByExternalId(externalId)
                .map(d -> toResponse(d, Collections.emptySet()));
    }

    @Transactional
    public DeputyResponse createDeputy(DeputyRequest request) {
        Deputy deputy = new Deputy();
        applyRequest(deputy, request);
        deputyRepository.save(deputy);
        return toResponse(deputy, Collections.emptySet());
    }

    @Transactional
    public DeputyResponse updateDeputy(Long id, DeputyRequest request) {
        Deputy deputy = deputyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Deputy not found: " + id));
        applyRequest(deputy, request);
        deputyRepository.save(deputy);
        return toResponse(deputy, Collections.emptySet());
    }

    @Transactional
    public void deleteDeputy(Long id) {
        if (!deputyRepository.existsById(id)) {
            throw new RuntimeException("Deputy not found: " + id);
        }
        deputyRepository.deleteById(id);
    }

    private void applyRequest(Deputy deputy, DeputyRequest request) {
        deputy.setName(request.name());
        deputy.setParty(request.party());
        deputy.setState(request.state());
        deputy.setAvatar(request.avatar());
        deputy.setExternalId(request.externalId());
    }

    private DeputyResponse toResponse(Deputy deputy, Set<Long> followedIds) {
        return DeputyResponse.from(deputy, followedIds.contains(deputy.getId()));
    }
}
