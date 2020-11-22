package com.smalltalknow.service.controller.enums;

public enum EnumSessionStatus {
    SESSION_STATUS_VALID("session_valid"),
    SESSION_STATUS_EXPIRED("session_expired"),
    SESSION_STATUS_REVOKED("session_revoked");

    private String token;

    EnumSessionStatus(String token) { this.token = token; }

    @Override
    public String toString() {
        return token;
    }
}
