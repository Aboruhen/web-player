package com.local.resource.webplayer.repository;

import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

@Component
public class LastSessionStore {

    private final Map<String, Path> lastViewSession = new HashMap<>();

    public void updateLastView(String sessionId, Path path) {
        lastViewSession.put(sessionId, path);
    }

    public Path findLastView(String sessionId) {
        return lastViewSession.get(sessionId);
    }

}
