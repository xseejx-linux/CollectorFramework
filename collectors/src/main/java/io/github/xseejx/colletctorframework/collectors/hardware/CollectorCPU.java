package io.github.xseejx.colletctorframework.collectors.hardware;


import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.auto.service.AutoService;
import io.github.xseejx.colletctorframework.core.api.Collector;
import io.github.xseejx.colletctorframework.core.api.CollectorMetadata;
import io.github.xseejx.colletctorframework.core.api.CollectorResult;
import org.json.simple.JSONObject;

import oshi.SystemInfo;

@AutoService(Collector.class)
@CollectorMetadata(
    name        = "hardware.cpu",
    description = "CPU usage, frequency, temperature",
    tags        = {"hardware", "realtime"}
)


/**
 * A collector for gathering CPU hardware information.
 */
public class CollectorCPU implements Collector
{
    private static final Logger logger = LoggerFactory.getLogger(CollectorCPU.class);


    // With reflective modify those values on core
    private boolean includePerCore;
    private boolean includeTemperature;

    @Override
    public String getName() { return "hardware.cpu"; }

    @Override
    public CollectorResult collect() { 
        try {
            SystemInfo si = new SystemInfo();
            var cpu = si.getHardware().getProcessor();


            JSONObject result = new JSONObject();
            if (includePerCore) {
                result.put("includePerCore", "ON");
            }else {
                result.put("includePerCore", "OFF");
            }
            if (includeTemperature) {
                result.put("includeTemperature", "ON");
            }else {
                result.put("includeTemperature", "OFF");
            }

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





    public static void main( String[] args )
    {   
        SystemInfo si = new SystemInfo();
        JSONObject result = new JSONObject();
        logger.info("CPU: " + si.getHardware().getProcessor().toString());
    }
}
