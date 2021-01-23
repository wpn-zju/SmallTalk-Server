package com.smalltalknow.service.controller.enums;

public enum EnumRequestStatus {
    REQUEST_STATUS_PENDING("request_pending"),
    REQUEST_STATUS_ACCEPTED("request_accepted"),
    REQUEST_STATUS_REFUSED("request_refused"),
    REQUEST_STATUS_REVOKED("request_revoked");

    private final String token;

    EnumRequestStatus(String token) { this.token = token; }

    @Override
    public String toString() {
        return token;
    }
}
