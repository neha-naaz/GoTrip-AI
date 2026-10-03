package com.tripflow.media.storage;

import com.tripflow.media.config.MediaProperties;
import com.tripflow.trip.exception.TripRulesInvalidException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class LocalFileMediaStorage implements MediaStorage {

    private static final Set<String> ALLOWED_TYPES = Set.of("image/jpeg", "image/png", "image/webp");

    private final Path root;

    public LocalFileMediaStorage(MediaProperties mediaProperties) {
        this.root = Path.of(mediaProperties.getRootDir()).toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.root);
        } catch (IOException ex) {
            throw new IllegalStateException("Cannot create media root: " + this.root, ex);
        }
    }

    @Override
    public String store(Long tripId, String originalFilename, String contentType, InputStream content, long size) {
        if (contentType == null || !ALLOWED_TYPES.contains(contentType.toLowerCase(Locale.ROOT))) {
            throw new TripRulesInvalidException("Only JPEG, PNG, and WebP images are allowed");
        }

        String extension = extensionFor(contentType, originalFilename);
        String filename = UUID.randomUUID() + extension;
        String key = tripId + "/" + filename;
        Path target = resolve(key);

        try {
            Files.createDirectories(target.getParent());
            Files.copy(content, target, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to store media file", ex);
        }
        return key;
    }

    @Override
    public Path resolve(String storageKey) {
        Path resolved = root.resolve(storageKey).normalize();
        if (!resolved.startsWith(root)) {
            throw new TripRulesInvalidException("Invalid media path");
        }
        return resolved;
    }

    @Override
    public void delete(String storageKey) {
        try {
            Files.deleteIfExists(resolve(storageKey));
        } catch (IOException ex) {
            // Best-effort cleanup; DB row is source of truth for the gallery.
        }
    }

    private static String extensionFor(String contentType, String originalFilename) {
        return switch (contentType.toLowerCase(Locale.ROOT)) {
            case "image/png" -> ".png";
            case "image/webp" -> ".webp";
            case "image/jpeg" -> ".jpg";
            default -> {
                String ext = StringUtils.getFilenameExtension(originalFilename);
                yield StringUtils.hasText(ext) ? "." + ext.toLowerCase(Locale.ROOT) : ".bin";
            }
        };
    }
}
