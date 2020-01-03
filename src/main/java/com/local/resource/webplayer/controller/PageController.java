package com.local.resource.webplayer.controller;

import com.local.resource.webplayer.dto.MediaSourceGroup;
import com.local.resource.webplayer.service.FileIndexService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Random;

@RequiredArgsConstructor
@Controller
public class PageController {

    private final FileIndexService fileIndexService;

    @GetMapping("/")
    public String index(Model model) {

        MediaSourceGroup mediaSourceGroup = fileIndexService.mediaSourceGroup();

        model.addAttribute("mediaGroup", mediaSourceGroup);

        return "index";
    }

    @GetMapping("/resource/{mediaId}/next")
    public String playNextMedia(@PathVariable Long mediaId) {
//        Collection<Long> longs = fileIndexMap.keySet();
        int i = new Random().nextInt(10);
//        Long nextId = longs.toArray(new Long[]{})[i];
        return String.format("redirect:/resource/%s", i);
    }

    @GetMapping("/resource/{mediaId}")
    public String playMedia(@PathVariable Long mediaId, Model model) {
        model.addAttribute("mediaId", mediaId);
        return "video";
    }

}
