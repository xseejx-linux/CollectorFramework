package io.github.xseejx.colletctorframework.core.engine.stream;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

public class StreamSpec {
     private final String              collectorName;
    private final Map<String, Object> parameters;
    private final StreamTrigger       trigger;
    private final StopCondition       stopCondition;
    private final Consumer<String>    onEmit;
    private final Consumer<UUID>      onStop;       // called when stream ends
    private final int                 queueCapacity;


    private StreamSpec(Builder b) {
        this.collectorName = b.collectorName;
        this.parameters    = Collections.unmodifiableMap(b.parameters);
        this.trigger       = b.trigger;
        this.stopCondition = b.stopCondition;
        this.onEmit        = b.onEmit;
        this.onStop        = b.onStop;
        this.queueCapacity = b.queueCapacity;
    }
    public static Builder builder(String collectorName) {
        return new Builder(collectorName);
    }



    // Builder

    public static class Builder {
        private final String              collectorName;
        private Map<String, Object>       parameters    = new HashMap<>();
        private StreamTrigger             trigger       = StreamTrigger.everySeconds(1);
        private StopCondition             stopCondition = StopCondition.never();
        private Consumer<String>          onEmit        = result -> {};
        private Consumer<UUID>            onStop        = id -> {};
        private int                       queueCapacity = 128;

        private Builder(String collectorName) {
            this.collectorName = collectorName;
        }

        public Builder parameters(Map<String, Object> parameters) {
            this.parameters = parameters; return this;
        }
        public Builder trigger(StreamTrigger trigger) {
            this.trigger = trigger; return this;
        }
        public Builder stopCondition(StopCondition stopCondition) {
            this.stopCondition = stopCondition; return this;
        }
        public Builder onEmit(Consumer<String> onEmit) {
            this.onEmit = onEmit; return this;
        }
        public Builder onStop(Consumer<UUID> onStop) {
            this.onStop = onStop; return this;
        }
        public Builder queueCapacity(int capacity) {
            this.queueCapacity = capacity; return this;
        }

        public StreamSpec build() { return new StreamSpec(this); }
    }



    public Object getQueueCapacity() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getQueueCapacity'");
    }
}
