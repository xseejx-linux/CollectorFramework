package io.github.xseejx.colletctorframework.core.engine.stream;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

public class StreamContext {
    private final UUID     streamId;
    private final String   lastResult;
    private final long     emissionCount;
    private final Instant  startedAt;
    private final Instant  lastEmittedAt;

    public StreamContext(UUID streamId, String lastResult,
        long emissionCount, Instant startedAt, Instant lastEmittedAt) {


        this.streamId      = streamId;
        this.lastResult    = lastResult;
        this.emissionCount = emissionCount;
        this.startedAt     = startedAt;
        this.lastEmittedAt = lastEmittedAt;
    }


    public Duration elapsed() {
        return Duration.between(startedAt, Instant.now());
    }

}
