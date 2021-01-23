package com.smalltalknow.service.message;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.smalltalknow.service.controller.websocket.ClientConstant;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor(onConstructor = @__(@JsonCreator))
public class UserModifyInfoMessage {
    @JsonProperty(ClientConstant.USER_MODIFY_INFO_USER_ID)
    private final int userId;
    @JsonProperty(ClientConstant.USER_MODIFY_INFO_USER_NAME)
    private final String userName;
    @JsonProperty(ClientConstant.USER_MODIFY_INFO_USER_PASSWORD)
    private final String userPassword;
    @JsonProperty(ClientConstant.USER_MODIFY_INFO_USER_GENDER)
    private final Integer userGender;
    @JsonProperty(ClientConstant.USER_MODIFY_INFO_USER_AVATAR_LINK)
    private final String userAvatarLink;
    @JsonProperty(ClientConstant.USER_MODIFY_INFO_USER_INFO)
    private final String userInfo;
    @JsonProperty(ClientConstant.USER_MODIFY_INFO_USER_LOCATION)
    private final String userLocation;
}
