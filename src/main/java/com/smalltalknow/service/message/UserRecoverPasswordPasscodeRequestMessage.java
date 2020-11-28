package com.smalltalknow.service.message;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.smalltalknow.service.controller.websocket.ClientConstant;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class UserRecoverPasswordPasscodeRequestMessage {
    @JsonProperty(ClientConstant.USER_RECOVER_PASSWORD_PASSCODE_REQUEST_USER_EMAIL)
    private final String userEmail;
}
