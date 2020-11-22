package com.smalltalknow.service.message;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.smalltalknow.service.controller.websocket.ClientStrings;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class WebRTCCallMessage {
    @JsonProperty(ClientStrings.CHAT_WEBRTC_CALL_SENDER)
    private final int sender;
    @JsonProperty(ClientStrings.CHAT_WEBRTC_CALL_RECEIVER)
    private final int receiver;
    @JsonProperty(ClientStrings.CHAT_WEBRTC_CALL_WEBRTC_COMMAND)
    private final String webRTCCommand;
    @JsonProperty(ClientStrings.CHAT_WEBRTC_CALL_WEBRTC_SESSION_DESCRIPTION)
    private final String webRTCSessionDescription;
}
