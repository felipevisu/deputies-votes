package com.deolhoneles.dto;

import com.deolhoneles.entity.Deputy;

public record EventDeputySummary(
        Long deputyId,
        String name,
        String party,
        String state,
        String photo
) {

    public static EventDeputySummary from(Deputy d) {
        return new EventDeputySummary(d.getId(), d.getName(), d.getParty(), d.getState(), d.getAvatar());
    }
}
