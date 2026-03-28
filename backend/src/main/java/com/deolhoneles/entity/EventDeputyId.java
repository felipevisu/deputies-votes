package com.deolhoneles.entity;

import java.io.Serializable;
import java.util.Objects;

public class EventDeputyId implements Serializable {

    private Long eventId;
    private Long deputyId;

    public EventDeputyId() {
    }

    public EventDeputyId(Long eventId, Long deputyId) {
        this.eventId = eventId;
        this.deputyId = deputyId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EventDeputyId that = (EventDeputyId) o;
        return Objects.equals(eventId, that.eventId)
                && Objects.equals(deputyId, that.deputyId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(eventId, deputyId);
    }
}
