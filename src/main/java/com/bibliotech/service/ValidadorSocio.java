package com.bibliotech.service;

import com.bibliotech.exception.ValidacionSocioException;
import java.util.regex.Pattern;

public class ValidadorSocio {

    // Expresión regular simple para validar formato de email
    private static final String EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@(.+)$";

    public void validarEmail(String email) throws ValidacionSocioException {
        if (email == null || !Pattern.matches(EMAIL_REGEX, email)) {
            throw new ValidacionSocioException("El formato del email no es válido: " + email);
        }
    }

    public void validarDni(String dni) throws ValidacionSocioException {
        if (dni == null || dni.trim().isEmpty()) {
            throw new ValidacionSocioException("El DNI no puede estar vacío.");
        }
        if (!dni.matches("\\d{7,8}")) {
            throw new ValidacionSocioException("El DNI debe contener entre 7 y 8 números.");
        }
    }
}