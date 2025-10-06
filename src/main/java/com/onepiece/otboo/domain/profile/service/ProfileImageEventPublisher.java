package com.onepiece.otboo.domain.profile.service;

import com.onepiece.otboo.domain.profile.entity.Profile;
import com.onepiece.otboo.global.event.event.ProfileImageReplaceEvent;
import com.onepiece.otboo.global.storage.payload.UploadPayload;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ProfileImageEventPublisher {

    private final ApplicationEventPublisher publisher;

    public void publishReplace(Profile profile, String prefix, UploadPayload payload,
        String oldKey) {
        UUID userId = profile.getUser().getId();
        publisher.publishEvent(new ProfileImageReplaceEvent(userId, prefix, payload, oldKey));
    }
}