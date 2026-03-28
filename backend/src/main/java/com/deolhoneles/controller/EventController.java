package com.deolhoneles.controller;

import com.deolhoneles.dto.EventDeputyRequest;
import com.deolhoneles.dto.EventRequest;
import com.deolhoneles.dto.EventResponse;
import com.deolhoneles.service.EventService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/events")
public class EventController {

    private final EventService eventService;

    public EventController(EventService eventService) {
        this.eventService = eventService;
    }

    @GetMapping("/external/{externalId}")
    public ResponseEntity<EventResponse> findByExternalId(@PathVariable Long externalId) {
        return eventService.findByExternalId(externalId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EventResponse create(@Valid @RequestBody EventRequest request) {
        return eventService.create(request);
    }

    @PostMapping("/{id}/deputies")
    @ResponseStatus(HttpStatus.CREATED)
    public void addDeputy(@PathVariable Long id, @Valid @RequestBody EventDeputyRequest request) {
        eventService.addDeputy(id, request);
    }

    @PostMapping("/{id}/deputies/batch")
    @ResponseStatus(HttpStatus.CREATED)
    public void addDeputiesBatch(@PathVariable Long id, @Valid @RequestBody List<EventDeputyRequest> requests) {
        eventService.addDeputiesBatch(id, requests);
    }
}
