package com.local.resource.webplayer.dto;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;

import java.io.Serializable;
import java.nio.file.Path;
import java.util.List;

@Data
@Getter
@Builder
public class MediaSourceGroup implements Serializable {

    private String groupName;
    private Path groupLocation;
    public List<MediaSourceGroup> subGroups;
    public List<MediaSource> sourceList;

}
