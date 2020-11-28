package com.smalltalknow.service.message;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.smalltalknow.service.controller.websocket.ClientConstant;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor(onConstructor = @__(@JsonCreator))
public class LoadContactMessage {
    @JsonProperty(ClientConstant.LOAD_CONTACT_CONTACT_ID)
    private final int contactId;
}
