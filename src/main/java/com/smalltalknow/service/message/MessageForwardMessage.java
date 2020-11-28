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
public class MessageForwardMessage {
    @JsonProperty(ClientConstant.CHAT_MESSAGE_FORWARD_SENDER)
    private final int sender;
    @JsonProperty(ClientConstant.CHAT_MESSAGE_FORWARD_RECEIVER)
    private final int receiver;
    @JsonProperty(ClientConstant.CHAT_MESSAGE_FORWARD_CONTENT)
    private final String content;
    @JsonProperty(ClientConstant.CHAT_MESSAGE_FORWARD_CONTENT_TYPE)
    private final String contentType;
    @JsonProperty(ClientConstant.TIMESTAMP)
    private final String timestamp;
}
