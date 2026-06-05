package com.sems.iam.domain.model.valueobjects;

import java.util.Locale;
import java.util.regex.Pattern;
/**
 * Objeto de Valor que representa una dirección de correo electrónico.
 * 
 * EmailAddress es un record de Java que encapsula la lógica de validación
 * y normalización de direcciones de correo electrónico dentro del dominio IAM.
 * 
 * Esta clase implementa el patrón Value Object del Domain-Driven Design (DDD),
 * asegurando que solo se pueden crear instancias válidas de EmailAddress.
 * 
 * @author SEMS IAM Service
 * @version 1.0
 * @since 1.0
 */
public record EmailAddress(String value) {
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");

    public EmailAddress {
        if (value == null || value.isBlank() || !EMAIL_PATTERN.matcher(value).matches()) {
            throw new IllegalArgumentException("Invalid email address");
        }
        value = value.trim().toLowerCase(Locale.ROOT);
    }
}
