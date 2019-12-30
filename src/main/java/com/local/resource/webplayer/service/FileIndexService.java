package com.local.resource.webplayer.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;

@RequiredArgsConstructor
@Slf4j
@Service
public class FileIndexService {

    @Value("${STORE_LOCATION_PATH}")
    private String locationPath;
    private final Map<Long, Path> fileIndexMap;
    private final Map<Path, Collection<Long>> fileGroups = new HashMap<>();
    private final AtomicLong atomicLong = new AtomicLong();

    @EventListener(ApplicationStartedEvent.class)
    public void indexingFiles() {
        try {
            atomicLong.set(1);
            Path path = Paths.get(locationPath);
            checkPathLocation(path);
            generateFilesIndex(path);
            log.info("fileIndexMap: {}", fileIndexMap);
            log.info("fileGroups: {}", fileGroups);
        } catch (Exception e) {
            log.error(e.getLocalizedMessage(), e);
        }
    }

    private void generateFilesIndex(Path path) throws IOException {
        for (Path childPath : Files.newDirectoryStream(path)) {
            createFilePathIndex(childPath);
        }
    }

    private void createFilePathIndex(Path path) throws IOException {
        if (Files.isDirectory(path)) {
            generateFilesIndex(path);
            return;
        }
        long index = atomicLong.incrementAndGet();
        fileIndexMap.put(index, path);
        fileGroups.compute(path, (path1, uuids) -> {
            if (Objects.isNull(uuids)) {
                uuids = new HashSet<>();
            }
            uuids.add(index);
            return uuids;
        });
    }

    private void checkPathLocation(Path path) {
        if (!Files.isReadable(path)) {
            throw new RuntimeException("Default path is not readable");
        }
        if (!Files.isDirectory(path)) {
            throw new RuntimeException("Default path is not valid directory");
        }
    }

}
