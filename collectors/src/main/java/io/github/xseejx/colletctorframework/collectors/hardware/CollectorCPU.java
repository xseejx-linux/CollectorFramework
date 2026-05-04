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
    description = "CPU usage, frequency, temperature, load average and core count",
    tags        = {"hardware", "realtime"}
)

public class CollectorCPU implements Collector {
    private static final Logger logger = LoggerFactory.getLogger(CollectorCPU.class);

    // With reflective modify those values on core
    private boolean includePerCore;
    private boolean includeTemperature;
    private boolean includeLoadAverage;     // AGGIUNTA 1: load average
    private boolean includeCoreInfo;        // AGGIUNTA 2: info core

    @Override
    public String getName() { return "hardware.cpu"; }

    @Override
    @SuppressWarnings("unchecked")
    public CollectorResult collect() { 
        try {
            SystemInfo si = new SystemInfo();
            var cpu = si.getHardware().getProcessor();

            JSONObject result = new JSONObject();
            
            // Parametro esistente
            if (includePerCore) {
                result.put("includePerCore", "ON");
            } else {
                result.put("includePerCore", "OFF");
            }
            
            // Parametro esistente
            if (includeTemperature) {
                result.put("includeTemperature", "ON");
            } else {
                result.put("includeTemperature", "OFF");
            }
            
            // AGGIUNTA 1: Load Average (carico medio 1, 5, 15 minuti)
            if (includeLoadAverage) {
                double[] loadAverage = cpu.getSystemLoadAverage(3);
                JSONObject load = new JSONObject();
                if (loadAverage.length == 3 && loadAverage[0] >= 0) {
                    load.put("1min", String.format("%.2f", loadAverage[0]));
                    load.put("5min", String.format("%.2f", loadAverage[1]));
                    load.put("15min", String.format("%.2f", loadAverage[2]));
                } else {
                    load.put("1min", "N/A");
                    load.put("5min", "N/A");
                    load.put("15min", "N/A");
                }
                result.put("loadAverage", load);
            } else {
                result.put("loadAverage", "OFF");
            }
            
            // AGGIUNTA 2: Informazioni core fisici e logici
            if (includeCoreInfo) {
                JSONObject coreInfo = new JSONObject();
                coreInfo.put("physicalCores", cpu.getPhysicalProcessorCount());
                coreInfo.put("logicalCores", cpu.getLogicalProcessorCount());
                coreInfo.put("physicalPackages", cpu.getPhysicalPackageCount());
                result.put("coreInfo", coreInfo);
            } else {
                result.put("coreInfo", "OFF");
            }

            return CollectorResult.ok(getName(), result);

        } catch (Exception e) {
            logger.error("Error occurred while collecting CPU information", e);
            JSONObject result = new JSONObject();
            result.put("error", e.getMessage());
            return CollectorResult.failure(getName(), result);
        }
    }

    @Override
    public Map<String, Class<?>> getAcceptedParameters() {
        return Map.of(
            "includePerCore",      Boolean.class,
            "includeTemperature",  Boolean.class,
            "includeLoadAverage",  Boolean.class,    // AGGIUNTA 1
            "includeCoreInfo",     Boolean.class     // AGGIUNTA 2
        );
    }

    public static void main( String[] args ) {   
        CollectorCPU collector = new CollectorCPU();
        
        // Abilita tutte le funzionalità per il test
        collector.includePerCore = true;
        collector.includeTemperature = true;
        collector.includeLoadAverage = true;
        collector.includeCoreInfo = true;
        
        CollectorResult result = collector.collect();
        
        System.out.println("=== CPU Collector Test ===");
        System.out.println(result.getResult().toJSONString());
    }
}