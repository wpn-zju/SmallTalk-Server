package com.smalltalknow.service.controller.enums;

public enum EnumMessageStatus {
    MESSAGE_STATUS_PENDING("message_pending"),
    MESSAGE_STATUS_POPPED("message_popped");

    private final String token;

    EnumMessageStatus(String token) { this.token = token; }

    @Override
    public String toString() {
        return token;
    }
}
