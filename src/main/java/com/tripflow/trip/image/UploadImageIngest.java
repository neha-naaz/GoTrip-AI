package com.tripflow.trip.image;

import com.tripflow.media.config.MediaProperties;
import com.tripflow.media.storage.MediaStorage;
import com.tripflow.trip.entity.TripImageSource;
import com.tripflow.trip.exception.TripRulesInvalidException;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
@RequiredArgsConstructor
public class UploadImageIngest implements TripImageIngest {

    private final MediaStorage mediaStorage;
    private final MediaProperties mediaProperties;

    @Override
    public TripImageSource source() {
        return TripImageSource.UPLOAD;
    }

    @Override
    public String ingest(Long tripId, TripImageIngestRequest request) {
        MultipartFile file = request.file();
        if (file == null || file.isEmpty()) {
            throw new TripRulesInvalidException("Image file is required");
        }
        if (file.getSize() > mediaProperties.getMaxBytes()) {
            throw new TripRulesInvalidException("Image must be 5MB or smaller");
        }

        try {
            String key = mediaStorage.store(
                    tripId,
                    file.getOriginalFilename(),
                    file.getContentType(),
                    file.getInputStream(),
                    file.getSize());
            return "/api/media/" + key;
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to read uploaded image", ex);
        }
    }
}
