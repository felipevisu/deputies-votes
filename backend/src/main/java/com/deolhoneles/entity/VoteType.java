package com.deolhoneles.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum VoteType {

    SIM("SIM"),
    NAO("NÃO"),
    ABSTENCAO("ABSTENÇÃO"),
    AUSENTE("AUSENTE");

    private final String displayValue;

    VoteType(String displayValue) {
        this.displayValue = displayValue;
    }

    @JsonValue
    public String getDisplayValue() {
        return displayValue;
    }

    @JsonCreator
    public static VoteType fromValue(String value) {
        for (VoteType type : values()) {
            if (type.name().equals(value) || type.displayValue.equals(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown vote type: " + value);
    }
}
