package com.deolhoneles.service;

import com.deolhoneles.dto.EventDeputyRequest;
import com.deolhoneles.dto.EventRequest;
import com.deolhoneles.dto.EventResponse;
import com.deolhoneles.entity.Event;
import com.deolhoneles.entity.EventDeputy;
import com.deolhoneles.repository.EventDeputyRepository;
import com.deolhoneles.repository.EventRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EventService {

    private final EventRepository eventRepository;
    private final EventDeputyRepository eventDeputyRepository;

    public EventService(EventRepository eventRepository,
                        EventDeputyRepository eventDeputyRepository) {
        this.eventRepository = eventRepository;
        this.eventDeputyRepository = eventDeputyRepository;
    }

    @Transactional(readOnly = true)
    public Optional<EventResponse> findByExternalId(Long externalId) {
        return eventRepository.findByExternalId(externalId)
                .map(EventResponse::from);
    }

    @Transactional
    public EventResponse create(EventRequest request) {
        Event event = new Event();
        event.setExternalId(request.externalId());
        event.setEventType(request.eventType());
        event.setDescription(request.description());
        event.setAgendaSummary(request.agendaSummary());
        event.setSituation(request.situation());
        event.setStartTime(LocalDateTime.parse(request.startTime()));
        event.setEndTime(request.endTime() != null ? LocalDateTime.parse(request.endTime()) : null);
        event.setLocation(request.location());
        event.setOrganCode(request.organCode());
        event.setOrganName(request.organName());
        event.setVideoUrl(request.videoUrl());
        event.setEventDate(LocalDate.parse(request.eventDate()));
        eventRepository.save(event);
        return EventResponse.from(event);
    }

    @Transactional
    public void addDeputy(Long eventId, EventDeputyRequest request) {
        EventDeputy ed = new EventDeputy();
        ed.setEventId(eventId);
        ed.setDeputyId(request.deputyId());
        eventDeputyRepository.save(ed);
    }

    @Transactional
    public void addDeputiesBatch(Long eventId, List<EventDeputyRequest> requests) {
        List<EventDeputy> entities = requests.stream()
                .filter(r -> !eventDeputyRepository.existsByEventIdAndDeputyId(eventId, r.deputyId()))
                .map(r -> {
                    EventDeputy ed = new EventDeputy();
                    ed.setEventId(eventId);
                    ed.setDeputyId(r.deputyId());
                    return ed;
                })
                .toList();
        eventDeputyRepository.saveAll(entities);
    }
}
