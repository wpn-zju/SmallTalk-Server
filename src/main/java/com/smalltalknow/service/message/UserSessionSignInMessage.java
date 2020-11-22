package com.smalltalknow.service.message;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.smalltalknow.service.controller.websocket.ClientStrings;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class UserSessionSignInMessage {
    @JsonProperty(ClientStrings.USER_SESSION_SIGN_IN_SESSION_TOKEN)
    private final String sessionToken;
}
