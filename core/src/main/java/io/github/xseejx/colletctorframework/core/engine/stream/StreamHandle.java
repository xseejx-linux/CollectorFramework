package io.github.xseejx.colletctorframework.core.engine.stream;

import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.atomic.AtomicLong;

public class StreamHandle {
    private final UUID                    id;
    private final StreamSpec              spec;
    private final BlockingQueue<String>   queue;
    private final AtomicLong              emissionCount = new AtomicLong(0);
    private final Instant                 startedAt     = Instant.now();
    private volatile ScheduledFuture<?>   scheduledFuture;
    private volatile boolean              running       = true;



     public StreamHandle(UUID id, StreamSpec spec) {
        this.id    = id;
        this.spec  = spec;
        this.queue = new LinkedBlockingQueue<>(spec.getQueueCapacity());
    }

    public long incrementAndGetCount()  { return emissionCount.incrementAndGet(); }
    public boolean isRunning()          { return running; }
    public void markStopped()           { running = false; }

    public void cancelFuture() {
        if (scheduledFuture != null) scheduledFuture.cancel(false);
    }

}
