package io.github.xseejx.colletctorframework.core.engine.stream;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import com.cronutils.model.CronType;
import com.cronutils.model.definition.CronDefinitionBuilder;
import com.cronutils.model.time.ExecutionTime;
import com.cronutils.parser.CronParser;

import io.github.xseejx.colletctorframework.core.request.CollectorRequestActivator;

public class StreamerEngine {
        private final CollectorRequestActivator      activator;
    private final ScheduledExecutorService       scheduler;
    private final ConcurrentHashMap<UUID, StreamHandle> streams = new ConcurrentHashMap<>();

    public StreamerEngine(CollectorRequestActivator activator, int schedulerThreads) {
        this.activator = activator;
        this.scheduler = Executors.newScheduledThreadPool(schedulerThreads);
    }

    // ── Start ─────────────────────────────────────────────────────────────────

    public UUID start(StreamSpec spec) {
        UUID         id     = UUID.randomUUID();
        StreamHandle handle = new StreamHandle(id, spec);
        streams.put(id, handle);

        scheduleHandle(handle);
        return id;
    }

    private void scheduleHandle(StreamHandle handle) {
        StreamTrigger trigger = handle.getSpec().getTrigger();
        Runnable      task    = buildTask(handle);

        ScheduledFuture<?> future = switch (trigger) {
            case StreamTrigger.FixedRate  t -> scheduler.scheduleAtFixedRate(
                    task, 0, t.period(), t.unit());

            case StreamTrigger.FixedDelay t -> scheduler.scheduleWithFixedDelay(
                    task, 0, t.delay(), t.unit());

            case StreamTrigger.Once       t -> scheduler.schedule(
                    task, 0, TimeUnit.MILLISECONDS);

            case StreamTrigger.Cron       t -> scheduleCron(handle, task, t.expression());
        };

        handle.setScheduledFuture(future);
    }

    private ScheduledFuture<?> scheduleCron(StreamHandle handle, Runnable task, String expression) {
        // cron-utils: compute ms until next execution
        CronParser    parser   = new CronParser(CronDefinitionBuilder.instanceDefinitionFor(CronType.UNIX));
        Cron          cron     = parser.parse(expression);
        ExecutionTime execTime = ExecutionTime.forCron(cron);

        long delayMs = execTime
            .timeToNextExecution(ZonedDateTime.now())
            .map(Duration::toMillis)
            .orElse(60_000L);

        // schedule once; task will re-schedule itself at the end
        return scheduler.schedule(() -> {
            task.run();
            if (handle.isRunning()) scheduleCron(handle, task, expression);
        }, delayMs, TimeUnit.MILLISECONDS);
    }

    // ── Task ──────────────────────────────────────────────────────────────────

    private Runnable buildTask(StreamHandle handle) {
        return () -> {
            if (!handle.isRunning()) return;

            StreamSpec spec = handle.getSpec();

            try {
                String result = activator.activateRequest(
                    spec.getCollectorName(),
                    spec.getParameters()
                );

                long count = handle.incrementAndGetCount();

                // put on queue (drops if full — never blocks the scheduler thread)
                handle.getQueue().offer(result);

                // fire callback
                spec.getOnEmit().accept(result);

                // evaluate stop condition
                StreamContext ctx = new StreamContext(
                    handle.getId(), result, count,
                    handle.getStartedAt(), Instant.now()
                );

                if (spec.getStopCondition().shouldStop(ctx)) {
                    stop(handle.getId());
                }

            } catch (Exception e) {
                // never let a collector crash the scheduler thread
                String errorJson = "{\"error\": \"" + e.getMessage() + "\"}";
                handle.getQueue().offer(errorJson);
                spec.getOnEmit().accept(errorJson);
            }
        };
    }

    // ── Lifecycle ─────────────────────────────────────────────────────────────

    public void stop(UUID id) {
        StreamHandle handle = streams.get(id);
        if (handle == null) return;

        handle.markStopped();
        handle.cancelFuture();
        handle.getSpec().getOnStop().accept(id);
        streams.remove(id);
    }

    public void stopAll() {
        new HashSet<>(streams.keySet()).forEach(this::stop);
    }

    public Optional<StreamHandle> get(UUID id) {
        return Optional.ofNullable(streams.get(id));
    }

    public Set<UUID> listActive() {
        return Collections.unmodifiableSet(streams.keySet());
    }

    public void shutdown() {
        stopAll();
        scheduler.shutdown();
    }
}
