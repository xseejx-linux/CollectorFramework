package io.github.xseejx.colletctorframework.shell;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;


import io.github.xseejx.colletctorframework.core.service.ServiceManager;
import io.github.xseejx.colletctorframework.core.service.ServiceModel;

/**
 * Test Application for the Collector Framework. 
 * This class demonstrates how to use the CollectorRequestActivator to execute a collector and print the result. 
 * In a real application, you would likely have a more
 *
 */
@SuppressWarnings("unused")
public class App 
{
    public static void main( String[] args )
    {
        ServiceManager activator = new ServiceManager();
        //CollectorRequestActivator activator = new CollectorRequestActivator();

        //System.out.println("Waiting for collector result...");

        /*String s = activator.activateRequest("generic.test", Map.of(
            "value1", false
        ));*/
        /*List<String> available = activator.listAvailable();
        System.out.println("Available collectors: " + available);*/
/* 
        // pass a List<Map<String, Map<String, Object>>> to activate multiple collectors at once
        List<Map<String, Map<String, Object>>> requests = List.of(
           Map.of("generic.test", Map.of("value321", true, "value2", "Test1")),
           Map.of("hardware.cpu", Map.of("includePerCore", true))
        );

        List<String> results = activator.activateRequestsSync(requests);
        
        // If Collector Name is wrong then nothing happens


        System.out.println("Results: " + results);
        */
        //System.out.println("Collector execution completed.");

        //System.out.println("Collector result: " + s);



        // LIST OUT ALL METADA INFOS ABOUT ALL COLLECTORS
        /*CollectorRequestActivator activator = new CollectorRequestActivator();
        List<String> metadataConnector = activator.getMetada("hardware.cpu");
        System.out.println("Metadata for hardware.cpu:");
        metadataConnector.forEach(System.out::println);
        */


        //Type of streaming request:

        //activator.activateStreamingRequest("stream.test2", Map.of());

        /*
        System.out.println("Testing sync request...");
        System.out.println("Testing async request...");

        CompletableFuture<String> future =
            activator.activateRequestAsync("generic.test", Map.of());

        

        future.thenAccept(res -> {
            System.out.println("Async Result: " + res);
        });
        
  
        String result = activator.activateRequestSync("hardware.cpu", Map.of());
        System.out.println("Sync Result: " + result);
        String result2 = activator.activateRequestSync("hardware.cpu", Map.of());
        System.out.println("Sync Result: " + result2);

        // Keep the main thread alive to see async result
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();    
        }

        */

        // Try multiple async requests
        List<Map<String, Map<String, Object>>> requests = List.of(
            Map.of("generic.test", Map.of("value1", true, "valaue2", "Test1")),
            Map.of("hardware.cpu", Map.of("includePerCore", true, "includeTemeperature", false))
        );
/*
       activator.activateRequestsAsync(requests).thenAccept(res2 -> {

            res2.forEach(System.out::println);
        });*/
        
        
        /*List<CompletableFuture<String>> futures = activator.activateRequestsAsync(requests);

        futures.forEach(f ->
            f.thenAccept(res -> {
                System.out.println("Async Result: " + res);
            })
        );*/
        

        //String result = activator.activateRequestSync("hardware.cpu", Map.of("includeTemperature", true, "coreInfo", true ));
        /*String s = activator.activateRequestSync("generic.test", Map.of());
        System.out.println(s);*/
        /*try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();    
        }*/








        //TODO: Fix simple heartbeat, run infinite a function

        //TODO: implement streaming execution for single and multiple collectors

        //TODO:add cron jobs

        

        //───────────────────────────────────────────────────────────────────────────────
        // ── Testing Streming ──────────────────────────────────────────────────────────

  /* 
        CollectorRequestActivator activator = new CollectorRequestActivator();
        StreamerRequestActivator streamer = new StreamerRequestActivator(activator);

        // ── Simple heartbeat ──────────────────────────────────────────────────────────
        UUID id = streamer.heartbeat(
            "generic.test",
            Map.of("value1", false),
            result -> System.out.println("Got: " + result),
            StopCondition.whenResultContains("STOP")
                .or(StopCondition.afterCount(100))
        );

*/
        //───────────────────────────────────────────────────────────────────────────────

        // // ── Cron-scheduled task ───────────────────────────────────────────────────────
        // UUID cronId = streamer.stream(StreamSpec.builder("hardware.cpu")
        //     .trigger(StreamTrigger.cron("*/30 * * * *"))   // every 30 minutes
        //     .stopCondition(StopCondition.afterDuration(Duration.ofHours(6)))
        //     .onEmit(result -> log.info("CPU snapshot: {}", result))
        //     .onStop(stoppedId -> log.info("CPU stream {} ended", stoppedId))
        //     .build());

        // // ── Composed stop condition ───────────────────────────────────────────────────
        // StopCondition condition = StopCondition.afterCount(50)
        //     .or(StopCondition.afterDuration(Duration.ofMinutes(10)))
        //     .or(StopCondition.whenResultMatches(r -> r.contains("\"success\":false")));

        // // ── Management ────────────────────────────────────────────────────────────────
        // streamer.stop(id);
        // Set<UUID> active = streamer.listActive();
        // List<String> queued = streamer.drain(cronId);

        
    }
}
