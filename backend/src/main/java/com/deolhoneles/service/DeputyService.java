package com.deolhoneles.service;

import com.deolhoneles.dto.DeputyRequest;
import com.deolhoneles.dto.DeputyResponse;
import com.deolhoneles.dto.PageResponse;
import com.deolhoneles.entity.Deputy;
import com.deolhoneles.repository.DeputyRepository;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DeputyService {

    private final DeputyRepository deputyRepository;

    public DeputyService(DeputyRepository deputyRepository) {
        this.deputyRepository = deputyRepository;
    }

    @Transactional(readOnly = true)
    public PageResponse<DeputyResponse> listDeputies(int page, int size) {
        Page<Deputy> result = deputyRepository.findAll(PageRequest.of(page, size));
        var mapped = result.getContent().stream()
                .map(DeputyResponse::from)
                .toList();
        return PageResponse.from(result, mapped);
    }

    @Transactional(readOnly = true)
    public DeputyResponse getDeputy(Long id) {
        Deputy deputy = deputyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Deputy not found: " + id));
        return DeputyResponse.from(deputy);
    }

    @Transactional(readOnly = true)
    public Optional<DeputyResponse> findByExternalId(Long externalId) {
        return deputyRepository.findByExternalId(externalId)
                .map(DeputyResponse::from);
    }

    @Transactional
    public DeputyResponse createDeputy(DeputyRequest request) {
        Deputy deputy = new Deputy();
        applyRequest(deputy, request);
        deputyRepository.save(deputy);
        return DeputyResponse.from(deputy);
    }

    @Transactional
    public DeputyResponse updateDeputy(Long id, DeputyRequest request) {
        Deputy deputy = deputyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Deputy not found: " + id));
        applyRequest(deputy, request);
        deputyRepository.save(deputy);
        return DeputyResponse.from(deputy);
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
}
