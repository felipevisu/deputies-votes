package com.deolhoneles.dto;

import com.deolhoneles.entity.Account;

public record AccountResponse(
        Long id,
        String name,
        String lastName,
        String email
) {

    public static AccountResponse from(Account entity) {
        return new AccountResponse(
                entity.getId(),
                entity.getName(),
                entity.getLastName(),
                entity.getEmail()
        );
    }
}
