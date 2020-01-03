package com.local.resource.webplayer.controller;

import com.local.resource.webplayer.repository.LastSessionStore;
import com.local.resource.webplayer.service.FileIndexService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.UrlResource;
import org.springframework.core.io.support.ResourceRegion;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRange;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.MediaTypeFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

import java.io.IOException;
import java.nio.file.Path;

@RequiredArgsConstructor
@Slf4j
@Controller
public class MediaController {

    private static final long RESOURCE_LOAD_BYTE_CHUNK_SIZE = 1_000_000L;

    private final LastSessionStore lastSessionStore;
    private final FileIndexService fileIndexService;

    @GetMapping("refresh")
    public String refreshIndexes() {
        fileIndexService.indexingFiles();
        return "redirect:/";
    }


    @GetMapping("media/{mediaId}")
    public ResponseEntity<ResourceRegion> playVideo(
            @PathVariable Long mediaId,
            @RequestHeader HttpHeaders headers,
            @CookieValue(name = "JSESSIONID", required = false) String sessionId) throws IOException {

        log.info("Play video");

        Path path = fileIndexService.getMedia(mediaId);
        lastSessionStore.updateLastView(sessionId, mediaId);
        var video = new UrlResource(String.format("file:%s", path));
        var region = resourceRegion(video, headers);
        return ResponseEntity.status(HttpStatus.PARTIAL_CONTENT)
                .contentType(MediaTypeFactory.getMediaType(video).orElse(MediaType.APPLICATION_OCTET_STREAM))
                .body(region);
    }

    private ResourceRegion resourceRegion(UrlResource video, HttpHeaders headers) throws IOException {
        var contentLength = video.contentLength();
        var ranges = headers.getRange();
        if (!ranges.isEmpty()) {
            HttpRange range = ranges.get(0);
            var start = range.getRangeStart(contentLength);
            var end = range.getRangeEnd(contentLength);
            var rangeLength = Math.min(RESOURCE_LOAD_BYTE_CHUNK_SIZE, end - start + 1);
            return new ResourceRegion(video, start, rangeLength);
        } else {
            var rangeLength = Math.min(RESOURCE_LOAD_BYTE_CHUNK_SIZE, contentLength);
            return new ResourceRegion(video, 0, rangeLength);
        }
    }

}
