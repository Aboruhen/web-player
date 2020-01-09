package com.local.resource.webplayer.dto;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class MediaFile {

    @EqualsAndHashCode.Include
    private final String id;
    private final String title;
    private final String location;
    private final MediaSource source;

}
