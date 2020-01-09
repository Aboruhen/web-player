package com.local.resource.webplayer.dto;

import lombok.Builder;
import lombok.Getter;

import java.nio.file.Path;
import java.util.List;
import java.util.TreeSet;


@Builder
@Getter
public class MediaSource {

    private String sourceName;
    private Path sourceLocation;
    private List<Path> subSources;
    private TreeSet<MediaFile> mediaFiles;

}
