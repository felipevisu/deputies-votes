package com.deolhoneles.controller;

import com.deolhoneles.dto.DeputyRequest;
import com.deolhoneles.dto.DeputyResponse;
import com.deolhoneles.dto.PageResponse;
import com.deolhoneles.service.DeputyService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
@RequestMapping("/deputies")
public class DeputyController {

    private final DeputyService deputyService;

    public DeputyController(DeputyService deputyService) {
        this.deputyService = deputyService;
    }

    @GetMapping
    public PageResponse<DeputyResponse> listDeputies(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return deputyService.listDeputies(page, size);
    }

    @GetMapping("/{id}")
    public DeputyResponse getDeputy(@PathVariable Long id) {
        return deputyService.getDeputy(id);
    }

    @GetMapping("/external/{externalId}")
    public ResponseEntity<DeputyResponse> findByExternalId(@PathVariable Long externalId) {
        return deputyService.findByExternalId(externalId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public DeputyResponse createDeputy(@Valid @RequestBody DeputyRequest request) {
        return deputyService.createDeputy(request);
    }

    @PutMapping("/{id}")
    public DeputyResponse updateDeputy(@PathVariable Long id, @Valid @RequestBody DeputyRequest request) {
        return deputyService.updateDeputy(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteDeputy(@PathVariable Long id) {
        deputyService.deleteDeputy(id);
    }
}
