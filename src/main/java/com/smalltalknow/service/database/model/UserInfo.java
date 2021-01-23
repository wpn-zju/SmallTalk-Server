package com.smalltalknow.service.database.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.smalltalknow.service.controller.websocket.EntityConstant;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public final class UserInfo {
    @JsonProperty(EntityConstant.USER_INFO_USER_ID)
    private final int userId;
    @JsonProperty(EntityConstant.USER_INFO_USER_SESSION)
    private final String userSession;
    @JsonProperty(EntityConstant.USER_INFO_USER_EMAIL)
    private final String userEmail;
    @JsonProperty(EntityConstant.USER_INFO_USER_NAME)
    private final String userName;
    @JsonProperty(EntityConstant.USER_INFO_USER_PASSWORD)
    private final String userPassword;
    @JsonProperty(EntityConstant.USER_INFO_USER_GENDER)
    private final int userGender;
    @JsonProperty(EntityConstant.USER_INFO_USER_AVATAR_LINK)
    private final String userAvatarLink;
    @JsonProperty(EntityConstant.USER_INFO_USER_INFO)
    private final String userInfo;
    @JsonProperty(EntityConstant.USER_INFO_USER_LOCATION)
    private final String userLocation;
    @JsonProperty(EntityConstant.USER_INFO_USER_CONTACT_LIST)
    private final List<Integer> contactList;
    @JsonProperty(EntityConstant.USER_INFO_USER_GROUP_LIST)
    private final List<Integer> groupList;
    @JsonProperty(EntityConstant.USER_INFO_USER_REQUEST_LIST)
    private final List<Integer> requestList;
}
