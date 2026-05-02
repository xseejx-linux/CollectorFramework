package io.github.xseejx.colletctorframework.collectors.generic;

import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.xseejx.colletctorframework.collectors.hardware.CollectorCPU;
import io.github.xseejx.colletctorframework.core.api.Collector;
import io.github.xseejx.colletctorframework.core.api.CollectorMetadata;
import io.github.xseejx.colletctorframework.core.api.CollectorResult;
import oshi.SystemInfo;

import org.json.simple.JSONObject;


@CollectorMetadata(
    name        = "generic.test",
    description = "A simple test collector",
    tags        = {"generic", "test"}
)

public class CollectorTest implements Collector{
    private static final Logger logger = LoggerFactory.getLogger(CollectorCPU.class);


    // With reflective modify those values on core
    private boolean value1;
    private String value2;

    @Override
    public String getName() { return "generic.test"; }

    @Override
    public CollectorResult collect() { 
        try {
            SystemInfo si = new SystemInfo();
            var cpu = si.getHardware().getProcessor();


            JSONObject result = new JSONObject();
            result.put("model", "Test");
            //System.out.println(result.toJSONString());
           

            return CollectorResult.ok(getName(), result);


        } catch (Exception e) {
            logger.error("Error occurred while collecting CPU information", e);
            JSONObject result = new JSONObject();
            return CollectorResult.failure(getName(), result);
        }
    }

    @Override
    public Map<String, Class<?>> getAcceptedParameters() {
        return Map.of(
            "includePerCore",      Boolean.class,
            "includeTemperature",  Boolean.class
        );
    }

    // all classes must be protected



    public static void main( String[] args )
    {   
        SystemInfo si = new SystemInfo();
        JSONObject result = new JSONObject();
        logger.info("CPU: " + si.getHardware().getProcessor().toString());
    }
}
