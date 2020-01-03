package com.local.resource.webplayer.service;

import com.local.resource.webplayer.dto.MediaSource;
import com.local.resource.webplayer.dto.MediaSourceGroup;
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
import java.util.ArrayList;
import java.util.List;
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
    private final Map<Path, MediaSourceGroup> groupByPath;
    private MediaSourceGroup mediaSourceGroup = null;
    private final AtomicLong atomicLong = new AtomicLong();

    @EventListener(ApplicationStartedEvent.class)
    public void indexingFiles() {
        try {
            atomicLong.set(1);
            Path path = Paths.get(locationPath);
            checkPathLocation(path);
            mediaSourceGroup = MediaSourceGroup.builder()
                    .groupLocation(path)
                    .groupName("root")
                    .sourceList(new ArrayList<>())
                    .subGroups(new ArrayList<>())
                    .build();
            generateFilesIndex(mediaSourceGroup, path);
            log.info("fileIndexMap: {}", fileIndexMap);
            log.info("fileGroups: {}", mediaSourceGroup);
        } catch (Exception e) {
            log.error(e.getLocalizedMessage(), e);
        }
    }

    public MediaSourceGroup mediaSourceGroup(String group) {
        if (Objects.isNull(group) || group.trim().isBlank()){
            return mediaSourceGroup;
        }
        return groupByPath.getOrDefault(Paths.get(group), mediaSourceGroup);
    }

    private void generateFilesIndex(MediaSourceGroup root, Path path) throws IOException {
        for (Path childPath : Files.newDirectoryStream(path)) {
            createFilePathIndex(root, childPath);
        }
    }

    private void createFilePathIndex(MediaSourceGroup root, Path path) throws IOException {
        if (Files.isDirectory(path)) {
            MediaSourceGroup subGroup = MediaSourceGroup.builder()
                    .groupLocation(path)
                    .groupName(path.getFileName().toString())
                    .sourceList(new ArrayList<>())
                    .subGroups(new ArrayList<>())
                    .build();
            root.getSubGroups().add(subGroup);
            groupByPath.put(path, subGroup);
            generateFilesIndex(subGroup, path);
            return;
        }
        List<MediaSource> sourceList = root.getSourceList();
        long index = atomicLong.incrementAndGet();
        fileIndexMap.put(index, path);
        MediaSource mediaSource = MediaSource.builder()
                .title(path.getFileName().toString())
                .location(path.toString())
                .id(String.valueOf(index))
                .build();
        sourceList.add(mediaSource);
    }

    private void checkPathLocation(Path path) {
        if (!Files.isReadable(path)) {
            throw new RuntimeException("Default path is not readable");
        }
        if (!Files.isDirectory(path)) {
            throw new RuntimeException("Default path is not valid directory");
        }
    }

    public Path getMedia(Long mediaId) {
        return fileIndexMap.get(mediaId);
    }

}
