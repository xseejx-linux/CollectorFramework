package io.github.xseejx.colletctorframework.core.engine.stream;


@FunctionalInterface
public interface StopCondition {

    boolean shouldStop(StreamContext ctx);

    default StopCondition and(StopCondition other) {
        return ctx -> this.shouldStop(ctx) && other.shouldStop(ctx);
    }

    default StopCondition or(StopCondition other) {
        return ctx -> this.shouldStop(ctx) || other.shouldStop(ctx);
    }

    default StopCondition negate() {
        return ctx -> !this.shouldStop(ctx);
    }

    // ── Built-in factories ────────────────────────────────────────────────────

    static StopCondition never() {
        return ctx -> false;
    }

    static StopCondition afterCount(long n) {
        return ctx -> ctx.getEmissionCount() >= n;
    }

    static StopCondition afterDuration(Duration d) {
        return ctx -> ctx.elapsed().compareTo(d) >= 0;
    }

    static StopCondition whenResultContains(String substring) {
        return ctx -> ctx.getLastResult().contains(substring);
    }

    static StopCondition whenResultMatches(Predicate<String> predicate) {
        return ctx -> predicate.test(ctx.getLastResult());
    }

    static StopCondition whenEmissionCountIsMultipleOf(long n) {
        return ctx -> ctx.getEmissionCount() % n == 0;
    }
    
}
