package com.smalltalknow.service.controller.enums;

public enum EnumPasscodeStatus {
    PASSCODE_STATUS_PENDING("passcode_pending"),
    PASSCODE_STATUS_EXPIRED("passcode_expired"),
    PASSCODE_STATUS_REVOKED("passcode_revoked");

    private String token;

    EnumPasscodeStatus(String token) { this.token = token; }

    @Override
    public String toString() {
        return token;
    }
}
