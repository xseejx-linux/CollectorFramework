package io.github.xseejx.colletctorframework.core.engine.stream;

import java.util.concurrent.TimeUnit;

public sealed interface StreamTrigger permits StreamTrigger.FixedRate,
                StreamTrigger.FixedDelay,
                StreamTrigger.Cron,
                StreamTrigger.Once {

    record FixedRate(long period, TimeUnit unit) implements StreamTrigger {}
    record FixedDelay(long delay, TimeUnit unit) implements StreamTrigger {}
    record Cron(String expression)               implements StreamTrigger {}
    record Once()                                implements StreamTrigger {}

    // ── Factories ─────────────────────────────────────────────────────────────

    static StreamTrigger everySeconds(long s)  { return new FixedRate(s, TimeUnit.SECONDS); }
    static StreamTrigger everyMillis(long ms)  { return new FixedRate(ms, TimeUnit.MILLISECONDS); }
    static StreamTrigger everyMinutes(long m)  { return new FixedRate(m, TimeUnit.MINUTES); }
    static StreamTrigger once()                { return new Once(); }
    static StreamTrigger cron(String expr)     { return new Cron(expr); }
    
}
