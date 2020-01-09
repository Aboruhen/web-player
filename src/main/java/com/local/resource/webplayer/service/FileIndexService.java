package com.local.resource.webplayer.service;

import com.local.resource.webplayer.dto.MediaFile;
import com.local.resource.webplayer.dto.MediaSource;
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
import java.util.Comparator;
import java.util.Map;
import java.util.NavigableSet;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicLong;

@RequiredArgsConstructor
@Slf4j
@Service
public class FileIndexService {

    @Value("${STORE_LOCATION_PATH}")
    private String locationPath;
    private final Map<Long, MediaFile> mediaFileById;
    private final Map<Path, MediaSource> groupByPath;
    private MediaSource mediaSourceGroup = null;
    private final AtomicLong atomicLong = new AtomicLong();

    @EventListener(ApplicationStartedEvent.class)
    public void indexingFiles() {
        try {
            atomicLong.set(1);
            Path path = Paths.get(locationPath);
            checkPathLocation(path);
            mediaSourceGroup = MediaSource.builder()
                    .sourceLocation(path)
                    .sourceName("root")
                    .mediaFiles(new TreeSet<>(Comparator.comparing(MediaFile::getLocation)))
                    .subSources(new ArrayList<>())
                    .build();
            generateFilesIndex(mediaSourceGroup, path);
            log.info("fileIndexMap: {}", mediaFileById);
            log.info("fileGroups: {}", mediaSourceGroup);
        } catch (Exception e) {
            log.error(e.getLocalizedMessage(), e);
        }
    }

    public MediaSource mediaSourceGroup(String group) {
        if (Objects.isNull(group) || group.trim().isBlank()) {
            return mediaSourceGroup;
        }
        MediaSource mediaSource = groupByPath.getOrDefault(Paths.get(group), this.mediaSourceGroup);
        mediaSource.getSubSources().sort(Comparator.comparing(Path::toString));
        return mediaSource;
    }

    private void generateFilesIndex(MediaSource root, Path path) throws IOException {
        for (Path childPath : Files.newDirectoryStream(path)) {
            createFilePathIndex(root, childPath);
        }
    }

    private void createFilePathIndex(MediaSource root, Path path) throws IOException {
        if (Files.isDirectory(path)) {
            MediaSource subGroup = MediaSource.builder()
                    .sourceLocation(path)
                    .sourceName(path.getFileName().toString())
                    .mediaFiles(new TreeSet<>(Comparator.comparing(MediaFile::getLocation)))
                    .subSources(new ArrayList<>())
                    .build();
            root.getSubSources().add(path);
            groupByPath.put(path, subGroup);
            generateFilesIndex(subGroup, path);
            return;
        }
        Set<MediaFile> sourceList = root.getMediaFiles();
        long index = atomicLong.incrementAndGet();
        MediaFile mediaFile = MediaFile.builder()
                .id(String.valueOf(index))
                .location(path.toString())
                .title(path.getFileName().toString())
                .source(root)
                .build();
        mediaFileById.put(index, mediaFile);

        sourceList.add(mediaFile);
    }

    private void checkPathLocation(Path path) {
        if (!Files.isReadable(path)) {
            throw new RuntimeException("Default path is not readable");
        }
        if (!Files.isDirectory(path)) {
            throw new RuntimeException("Default path is not valid directory");
        }
    }

    public MediaFile getMediaFile(Long mediaId) {
        return mediaFileById.get(mediaId);
    }

    public MediaFile findNextOrStartFromFirst(Long mediaId) {
        MediaFile mediaFile = getMediaFile(mediaId);
        MediaSource source = mediaFile.getSource();
        TreeSet<MediaFile> mediaFiles = source.getMediaFiles();
        NavigableSet<MediaFile> tail = mediaFiles.tailSet(mediaFile, false);
        return tail.isEmpty() ? mediaFiles.first() : tail.first();
    }

}
