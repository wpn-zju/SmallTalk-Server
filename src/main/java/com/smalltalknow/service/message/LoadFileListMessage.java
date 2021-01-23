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
public class LoadFileListMessage {
    @JsonProperty(ClientConstant.LOAD_FILE_LIST_FIRST_SELECTOR)
    private final int firstSelector;
    @JsonProperty(ClientConstant.LOAD_FILE_LIST_SECOND_SELECTOR)
    private final int secondSelector;
}
