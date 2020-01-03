package com.local.resource.webplayer.controller;

import com.local.resource.webplayer.dto.MediaSourceGroup;
import com.local.resource.webplayer.repository.LastSessionStore;
import com.local.resource.webplayer.service.FileIndexService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseCookie;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.JdkIdGenerator;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Objects;
import java.util.Random;

@RequiredArgsConstructor
@Controller
public class PageController {

    private final LastSessionStore lastSessionStore;
    private final FileIndexService fileIndexService;

    @GetMapping("/")
    public String index(@RequestParam(value = "group", required = false) String group,
                        @CookieValue(name = "JSESSIONID", required = false) String sessionId,
                        ServerHttpResponse response, Model model) {
        if (Objects.isNull(sessionId)) {
            JdkIdGenerator jdkIdGenerator = new JdkIdGenerator();
            response.addCookie(ResponseCookie.from("JSESSIONID", jdkIdGenerator.generateId().toString()).maxAge(-1).httpOnly(true).build());
        }

        MediaSourceGroup mediaSourceGroup = fileIndexService.mediaSourceGroup(group);

        model.addAttribute("mediaGroup", mediaSourceGroup);

        return "index";
    }

    @GetMapping("/lastView")
    public String lastViewing(@CookieValue(name = "JSESSIONID", required = false) String sessionId) {
        Long mediaId = lastSessionStore.findLastView(sessionId);

        if (Objects.isNull(mediaId)) {
            return "redirect:/";
        }
        return String.format("redirect:/resource/%s", mediaId);
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
