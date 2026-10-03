package com.tripflow.media.controller;

import com.tripflow.media.storage.MediaStorage;
import com.tripflow.trip.exception.TripNotFoundException;
import java.nio.file.Files;
import java.nio.file.Path;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/media")
@RequiredArgsConstructor
public class MediaController {

    private final MediaStorage mediaStorage;

    @GetMapping("/{tripId}/{filename}")
    public ResponseEntity<Resource> get(@PathVariable Long tripId, @PathVariable String filename) {
        String key = tripId + "/" + filename;
        Path path = mediaStorage.resolve(key);
        if (!Files.isRegularFile(path)) {
            throw new TripNotFoundException(tripId);
        }

        String contentType;
        try {
            contentType = Files.probeContentType(path);
        } catch (Exception ex) {
            contentType = null;
        }
        MediaType mediaType = contentType != null
                ? MediaType.parseMediaType(contentType)
                : MediaType.APPLICATION_OCTET_STREAM;

        return ResponseEntity.ok()
                .contentType(mediaType)
                .body(new FileSystemResource(path));
    }
}
