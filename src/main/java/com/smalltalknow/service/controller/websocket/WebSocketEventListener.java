package com.smalltalknow.service.controller.websocket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.*;

import java.util.Objects;

@Component
public class WebSocketEventListener {
    private static final Logger logger = LoggerFactory.getLogger(WebSocketEventListener.class);

    @EventListener
    private void handleSessionConnect(SessionConnectEvent event) {
        logger.info(String.format("Connect - Session ID = %s", Objects.requireNonNull(event.getUser()).getName()));
    }

    @EventListener
    private void handleSessionConnected(SessionConnectedEvent event) {
        logger.info(String.format("Connected - Session ID = %s", Objects.requireNonNull(event.getUser()).getName()));
    }

    @EventListener
    private void handleSessionDisconnect(SessionDisconnectEvent event) {
        logger.info(String.format("Disconnected - Session ID = %s", Objects.requireNonNull(event.getUser()).getName()));
    }

    @EventListener
    private void handleSessionSubscribed(SessionSubscribeEvent event) {
        // logger.info(String.format("Subscribed - Session ID = %s", Objects.requireNonNull(event.getUser()).getName()));
    }

    @EventListener
    private void handleSessionUnsubscribed(SessionUnsubscribeEvent event) {
        // logger.info(String.format("Unsubscribed - Session ID = %s", Objects.requireNonNull(event.getUser()).getName()));
    }
}
