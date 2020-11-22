package com.smalltalknow.service.message;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.smalltalknow.service.controller.websocket.ClientStrings;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class UserRecoverPasswordPasscodeRequestMessage {
    @JsonProperty(ClientStrings.USER_RECOVER_PASSWORD_PASSCODE_REQUEST_USER_EMAIL)
    private final String userEmail;
}
