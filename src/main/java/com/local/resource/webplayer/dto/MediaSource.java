package com.local.resource.webplayer.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MediaSource {

    private final String id;
    private final String title;
    private final String location;

}
