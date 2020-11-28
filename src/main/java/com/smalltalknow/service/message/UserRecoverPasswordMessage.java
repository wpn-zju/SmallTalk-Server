package com.smalltalknow.service.message;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.smalltalknow.service.controller.websocket.ClientConstant;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class UserRecoverPasswordMessage {
    @JsonProperty(ClientConstant.USER_RECOVER_PASSWORD_USER_EMAIL)
    private final String userEmail;
    @JsonProperty(ClientConstant.USER_RECOVER_PASSWORD_USER_PASSWORD)
    private final String userPassword;
    @JsonProperty(ClientConstant.USER_RECOVER_PASSWORD_PASSCODE)
    private final String passcode;
}
