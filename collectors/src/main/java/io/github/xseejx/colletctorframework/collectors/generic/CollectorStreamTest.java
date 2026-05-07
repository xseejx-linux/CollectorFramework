package io.github.xseejx.colletctorframework.collectors.generic;

import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.auto.service.AutoService;


import io.github.xseejx.colletctorframework.core.api.Collector;
import io.github.xseejx.colletctorframework.core.api.CollectorMetadata;
import io.github.xseejx.colletctorframework.core.api.CollectorResult;


import org.json.simple.JSONObject;

@AutoService(Collector.class)
@CollectorMetadata(
    name        = "stream.test",
    description = "A simple test collector",
    tags        = {"generic", "test"}
)

// Just a stupid collector for testing, like the others nothing special here :)
public class CollectorStreamTest implements Collector{
    private static final Logger logger = LoggerFactory.getLogger(CollectorStreamTest.class);


    // With reflective modify those values on core
    private boolean value1 = false;
    private int value2 = 0;

    
    @Override
    public String getName() { return "stream.test"; }

    @SuppressWarnings("unchecked")
    @Override
    public CollectorResult collect() { 
        try {

            value1 = !value1;
            value2++;

            JSONObject result = new JSONObject();
            result.put("Value1", value1);
            result.put("Value2", value2);

        return CollectorResult.ok("RandomCollector", result);

        } catch (Exception e) {
            logger.error("Error occurred while collecting Test information", e);
            JSONObject result = new JSONObject();
            return CollectorResult.failure(getName(), result);
        }
    }

    @Override
    public Map<String, Class<?>> getAcceptedParameters() {
        return Map.of(
            "value1",  Boolean.class,
            "value2",  int.class
        );
    }





    @SuppressWarnings("unused")
    public static void main( String[] args )
    {   
        
        JSONObject result = new JSONObject();
        logger.info("Test Hello");
    }
}
