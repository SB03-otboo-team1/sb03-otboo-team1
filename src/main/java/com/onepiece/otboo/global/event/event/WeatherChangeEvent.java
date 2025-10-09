package com.onepiece.otboo.global.event.event;

import java.util.UUID;

public record WeatherChangeEvent(
    UUID outboxId,
    UUID userId,
    String title,
    String message
) {

}
