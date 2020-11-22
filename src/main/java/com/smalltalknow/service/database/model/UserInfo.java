package com.smalltalknow.service.database.model;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public final class UserInfo {
    private final int userId;
    private final String userEmail;
    private final String userName;
    private final String userPassword;
    private final List<Integer> contactList;
    private final List<Integer> groupList;
    private final List<Integer> requestList;
}
