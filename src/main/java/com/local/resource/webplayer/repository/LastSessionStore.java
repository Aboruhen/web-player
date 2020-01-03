package com.local.resource.webplayer.repository;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class LastSessionStore {

    private final Map<String, Long> lastViewSession = new HashMap<>();

    public void updateLastView(String sessionId, Long mediaId) {
        lastViewSession.put(sessionId, mediaId);
    }

    public Long findLastView(String sessionId) {
        return lastViewSession.get(sessionId);
    }

}
