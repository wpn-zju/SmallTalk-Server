package com.smalltalknow.service.message;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.smalltalknow.service.controller.websocket.ClientStrings;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class MessageForwardMessage {
    @JsonProperty(ClientStrings.CHAT_MESSAGE_FORWARD_SENDER)
    private final int sender;
    @JsonProperty(ClientStrings.CHAT_MESSAGE_FORWARD_RECEIVER)
    private final int receiver;
    @JsonProperty(ClientStrings.CHAT_MESSAGE_FORWARD_CONTENT)
    private final String content;
    @JsonProperty(ClientStrings.CHAT_MESSAGE_FORWARD_CONTENT_TYPE)
    private final String contentType;
    @JsonProperty(ClientStrings.TIMESTAMP)
    private final String timestamp;
}
