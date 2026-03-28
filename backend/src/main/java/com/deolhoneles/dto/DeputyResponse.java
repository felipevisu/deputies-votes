package com.deolhoneles.dto;

import com.deolhoneles.entity.Deputy;

public record DeputyResponse(
        Long id,
        String name,
        String party,
        String legend,
        String avatar,
        Long externalId
) {

    public static DeputyResponse from(Deputy entity) {
        return new DeputyResponse(
                entity.getId(),
                entity.getName(),
                entity.getParty(),
                entity.getState(),
                entity.getAvatar(),
                entity.getExternalId()
        );
    }
}
