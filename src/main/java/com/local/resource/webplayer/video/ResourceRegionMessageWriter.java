package com.local.resource.webplayer.video;

import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import org.springframework.core.ResolvableType;
import org.springframework.core.codec.ResourceRegionEncoder;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourceRegion;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.MediaTypeFactory;
import org.springframework.http.ReactiveHttpOutputMessage;
import org.springframework.http.ZeroCopyHttpOutputMessage;
import org.springframework.http.codec.HttpMessageWriter;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

@Slf4j
@Component
public class ResourceRegionMessageWriter implements HttpMessageWriter<ResourceRegion> {

    private final ResolvableType REGION_TYPE = ResolvableType.forClass(ResourceRegion.class);
    private ResourceRegionEncoder regionEncoder = new ResourceRegionEncoder();

    private List<MediaType> mediaTypes = MediaType.asMediaTypes(regionEncoder.getEncodableMimeTypes());

    @Override
    public List<MediaType> getWritableMediaTypes() {
        return mediaTypes;
    }

    @Override
    public boolean canWrite(ResolvableType elementType, MediaType mediaType) {
        return regionEncoder.canEncode(elementType, mediaType);
    }

    @Override
    public Mono<Void> write(Publisher<? extends ResourceRegion> inputStream,
                            ResolvableType elementType,
                            MediaType mediaType,
                            ReactiveHttpOutputMessage message,
                            Map<String, Object> hints) {
        return Mono.from(inputStream).flatMap((Function<ResourceRegion, Mono<? extends Void>>) resourceRegion -> {
            log.info("Write resource: {}", resourceRegion.getPosition());
            HttpHeaders headers = message.getHeaders();
            //Set a status code
//            response.setStatusCode(HttpStatus.PARTIAL_CONTENT);
            var resourceMediaType = getResourceMediaType(mediaType, resourceRegion.getResource());
            headers.setContentType(resourceMediaType);
            long contentLength = 0;

            try {
                contentLength = resourceRegion.getResource().contentLength();
            } catch (IOException e) {
                log.warn("Couldn't get content length");
                e.printStackTrace();
            }
            var start = resourceRegion.getPosition();
            var end = Math.min(start + resourceRegion.getCount() - 1, contentLength - 1);
            headers.add("Content-Range", "bytes " + start + '-' + end + '/' + contentLength);
            headers.setContentLength(end - start + 1);

            return zeroCopy(resourceRegion.getResource(), resourceRegion, message)
                    .orElseGet(() -> {
                        var input = Mono.just(resourceRegion);
                        var body = regionEncoder.encode(input, message.bufferFactory(), REGION_TYPE, resourceMediaType, Collections.emptyMap());
                        return message.writeWith(body);
                    });

        });
    }


    private MediaType getResourceMediaType(MediaType mediaType, Resource resource) {
        if (Objects.nonNull(mediaType) && mediaType.isConcrete() && !MediaType.APPLICATION_OCTET_STREAM.equals(mediaType)) {
            return mediaType;
        } else {
            return MediaTypeFactory.getMediaType(resource).orElse(MediaType.APPLICATION_OCTET_STREAM);
        }
    }

    private Optional<Mono<Void>> zeroCopy(Resource resource, ResourceRegion region,
                                          ReactiveHttpOutputMessage message) {
        if (message instanceof ZeroCopyHttpOutputMessage && resource.isFile()) {
            try {
                var file = resource.getFile();
                var pos = region.getPosition();
                var count = region.getCount();
                return Optional.of(((ZeroCopyHttpOutputMessage) message).writeWith(file, pos, count));
            } catch (IOException e) {
                log.warn("Couldn't get a file");
                return Optional.empty();
            }

        }
        return Optional.empty();
    }

}
