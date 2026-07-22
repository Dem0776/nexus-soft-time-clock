package com.condor.nexussoft.timeclock.identity.domain.model;

import java.util.Objects;
import java.util.regex.Pattern;

/** Value Object de email normalizado (minúsculas) y validado. */
public record Email(String value) {

    private static final Pattern PATTERN =
            Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    public Email {
        Objects.requireNonNull(value, "email requerido");
        value = value.trim().toLowerCase();
        if (!PATTERN.matcher(value).matches()) {
            throw new IllegalArgumentException("email inválido: " + value);
        }
    }

    public static Email of(String value) {
        return new Email(value);
    }

    @Override
    public String toString() {
        return value;
    }
}
