package com.local.resource.webplayer.controller;

import com.local.resource.webplayer.dto.MediaSource;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.thymeleaf.spring5.context.webflux.ReactiveDataDriverContextVariable;
import reactor.core.publisher.Flux;

import java.nio.file.Path;
import java.util.Collection;
import java.util.Map;
import java.util.Random;

@RequiredArgsConstructor
@Controller
public class PageController {

    private final Map<Long, Path> fileIndexMap;

    @GetMapping("/")
    public String index(Model model) {

        model.addAttribute("mediaList", new ReactiveDataDriverContextVariable(
                Flux.fromStream(fileIndexMap.entrySet().stream()
                        .map(m -> MediaSource.builder()
                                .id(m.getKey().toString())
                                .title(m.getValue().getFileName().toString())
                                .location(m.getValue().toString())
                                .build()))
        ));

        return "index";
    }

    @GetMapping("/resource/{mediaId}/next")
    public String playNextMedia(@PathVariable Long mediaId) {
        Collection<Long> longs = fileIndexMap.keySet();
        int i = new Random().nextInt(longs.size());
        Long nextId = longs.toArray(new Long[]{})[i];
        return String.format("redirect:/resource/%s", nextId);
    }

    @GetMapping("/resource/{mediaId}")
    public String playMedia(@PathVariable Long mediaId, Model model) {
        model.addAttribute("mediaId", mediaId);
        return "video";
    }

}
