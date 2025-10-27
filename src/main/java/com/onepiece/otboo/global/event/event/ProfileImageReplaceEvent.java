package com.onepiece.otboo.global.event.event;

import com.onepiece.otboo.global.storage.payload.UploadPayload;
import java.util.UUID;

public record ProfileImageReplaceEvent(
    UUID userId,
    String prefix,
    UploadPayload payload,
    String oldKey
) {

}
