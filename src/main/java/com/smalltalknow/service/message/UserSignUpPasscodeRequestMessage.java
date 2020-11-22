package com.smalltalknow.service.message;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.smalltalknow.service.controller.websocket.ClientStrings;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class UserSignUpPasscodeRequestMessage {
    @JsonProperty(ClientStrings.USER_SIGN_UP_PASSCODE_REQUEST_USER_EMAIL)
    private final String userEmail;
}
