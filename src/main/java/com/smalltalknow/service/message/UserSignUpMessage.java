package com.smalltalknow.service.message;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.smalltalknow.service.controller.websocket.ClientConstant;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class UserSignUpMessage {
    @JsonProperty(ClientConstant.USER_SIGN_UP_USER_EMAIL)
    private final String userEmail;
    @JsonProperty(ClientConstant.USER_SIGN_UP_USER_PASSWORD)
    private final String userPassword;
    @JsonProperty(ClientConstant.USER_SIGN_UP_PASSCODE)
    private final String passcode;
}
