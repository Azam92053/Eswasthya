package com.eswasthya.desktop.api;

/** Thrown when the backend returns an error or network fails. */
public class ApiException extends Exception {
    public ApiException(String message) { super(message); }
}
