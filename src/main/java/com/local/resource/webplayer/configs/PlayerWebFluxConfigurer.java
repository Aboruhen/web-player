package com.local.resource.webplayer.configs;

import com.local.resource.webplayer.video.ResourceRegionMessageWriter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.web.reactive.config.WebFluxConfigurer;

@RequiredArgsConstructor
@Configuration
public class PlayerWebFluxConfigurer implements WebFluxConfigurer {

    private final ResourceRegionMessageWriter resourceRegionMessageWriter;

    @Override
    public void configureHttpMessageCodecs(ServerCodecConfigurer configurer) {
        configurer.customCodecs().writer(resourceRegionMessageWriter);
    }

}
