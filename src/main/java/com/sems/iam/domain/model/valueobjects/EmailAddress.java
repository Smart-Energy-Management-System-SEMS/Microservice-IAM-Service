package com.sems.iam.domain.model.valueobjects;

import java.util.Locale;
import java.util.regex.Pattern;

public record EmailAddress(String value) {
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");

    public EmailAddress {
        if (value == null || value.isBlank() || !EMAIL_PATTERN.matcher(value).matches()) {
            throw new IllegalArgumentException("Invalid email address");
        }
        value = value.trim().toLowerCase(Locale.ROOT);
    }
}
