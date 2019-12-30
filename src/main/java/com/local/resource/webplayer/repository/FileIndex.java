package com.local.resource.webplayer.repository;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class FileIndex {

    @Bean("fileIndexMap")
    public Map<Long, Path> indexFile() {
        return new HashMap<>();
    }

}
