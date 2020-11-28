package com.smalltalknow.service.message;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.smalltalknow.service.controller.websocket.ClientConstant;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class UserModifyPasswordMessage {
    @JsonProperty(ClientConstant.USER_MODIFY_USER_PASSWORD_NEW_USER_PASSWORD)
    private final String newUserPassword;
}
