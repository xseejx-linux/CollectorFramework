package io.github.xseejx.colletctorframework.shell;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import org.quartz.Scheduler;

import io.github.xseejx.colletctorframework.core.engine.SchedulerProvider;
import io.github.xseejx.colletctorframework.core.registry.CollectorRegistry;
import io.github.xseejx.colletctorframework.core.service.ServiceManager;
import io.github.xseejx.colletctorframework.core.service.ServiceModel;
import io.github.xseejx.colletctorframework.core.service.TaskManager;
import io.github.xseejx.colletctorframework.core.service.TaskModel;

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


        //ServiceManager activator = new ServiceManager();
        //ServiceManager activator = ServiceManager.begin();
        //ServiceManager service = 
        //CollectorRequestActivator activator = new CollectorRequestActivator();
        //System.out.println("Waiting for collector result...");

        /*String s = activator.activateServiceSync("generic.test", Map.of(
            "value1", false
        ));
        List<String> available = activator.listAvailable();
        System.out.println("Available collectors: " + available);
 
        // pass a List<Map<String, Map<String, Object>>> to activate multiple collectors at once
        List<Map<String, Map<String, Object>>> requests = List.of(
           Map.of("generic.test", Map.of("value321", true, "value2", "Test1")),
           Map.of("hardware.cpu", Map.of("includePerCore", true))
        );

        List<String> results = activator.activateServicesSync(requests);
        
        // If Collector Name is wrong then nothing happens


        System.out.println("Results: " + results);
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
       /* List<Map<String, Map<String, Object>>> requests1 = List.of(
            Map.of("generic.test", Map.of("value1", true, "valaue2", "Test1")),
            Map.of("hardware.cpu", Map.of("includePerCore", true, "includeTemeperature", false))
        );*/
/*
       activator.activateRequestsAsync(requests).thenAccept(res2 -> {

            res2.forEach(System.out::println);
        });*/
        
        
        //List<CompletableFuture<String>> futures = activator.activateServicesAsync(requests1);

       /* futures.forEach(f ->
            f.thenAccept(res -> {
                System.out.println("Async Result: " + res);
            })
        );*/


        //activator.end();
        

        //String result = activator.activateRequestSync("hardware.cpu", Map.of("includeTemperature", true, "coreInfo", true ));
        /*String s = activator.activateRequestSync("generic.test", Map.of());
        System.out.println(s);*/
        /*try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();    
        }*/








        

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



        

       
        

        TaskManager manager = new TaskManager();

        System.out.println("Creating two tasks in ConsoleDispatcher...");
        String task2 = manager.createTask(new TaskModel(
            "generic.test",
            Map.of("value1", true, "value2", "Hello"),
            "* * * * * ?",
            "system"
        ));

        String task1 = manager.createTask(new TaskModel(
            "hardware.cpu",
            Map.of("includePerCore", true, "includeTemperature", false),
            "system"
        ));


        System.out.println("Created tasks: " + task1 + ", " + task2);
        System.out.println("Waiting for scheduled executions...");

        try {
            Thread.sleep(12000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        System.out.println("Deleting task: " + task2);
        //boolean deleted = manager.deleteTask(task2, "system");
        //System.out.println("Delete result: " + deleted);

        try {
            Thread.sleep(8000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        manager.shutdown();
        System.out.println("Collector scheduling demo complete.");
        /*

package io.github.xseejx.colletctorframework.shell;

import java.lang.reflect.Field;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import io.github.xseejx.colletctorframework.core.api.Collector;
import io.github.xseejx.colletctorframework.core.service.ServiceManager;

public class App {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        ServiceManager activator = new ServiceManager();

        try {

            List<String> available = activator.listAvailable();
            if (available == null || available.isEmpty()) {
                System.out.println("No collectors available.");
                return;
            }

            System.out.println("Available collectors:");
            for (int i = 0; i < available.size(); i++) {
                System.out.println("[" + i + "] " + available.get(i));
            }

            System.out.print("Select collector index: ");
            int selectedIndex = Integer.parseInt(scanner.nextLine().trim());

            if (selectedIndex < 0 || selectedIndex >= available.size()) {
                System.out.println("Invalid index.");
                return;
            }

            String collectorName = available.get(selectedIndex);
            Collector collector = activator.getCollector(collectorName);

            if (collector == null) {
                System.out.println("Collector not found: " + collectorName);
                return;
            }

            Map<String, Class<?>> accepted = collector.getAcceptedParameters();
            Map<String, Object> params = new LinkedHashMap<>();

            System.out.println("\nEditing parameters for: " + collectorName);

            for (Map.Entry<String, Class<?>> entry : accepted.entrySet()) {
                String key = entry.getKey();
                Class<?> type = entry.getValue();

                Field field = findField(collector.getClass(), key);
                if (field == null) {
                    System.out.println("Skipping missing field: " + key);
                    continue;
                }

                field.setAccessible(true);
                Object currentValue = field.get(collector);

                System.out.println();
                System.out.println("Parameter: " + key);
                System.out.println("Type: " + type.getSimpleName());
                System.out.println("Current value: " + currentValue);
                System.out.print("New value (ENTER to keep): ");

                String input = scanner.nextLine();

                Object finalValue = input.isBlank()
                        ? currentValue
                        : convert(input, type);

                params.put(key, finalValue);
            }

            injectValues(collector, params);

            System.out.println("\nFinal parameters:");
            for (Map.Entry<String, Object> entry : params.entrySet()) {
                System.out.println(entry.getKey() + " -> " + entry.getValue());
            }

            System.out.println("\nCollector output:");
            String result = activator.activateServiceSync(collectorName, params);
            //System.out.println(collector.collect().getResult().toJSONString());
            System.out.println(result);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            scanner.close();
            activator.end();
        }
    }

    private static Field findField(Class<?> clazz, String fieldName) {
        Class<?> current = clazz;

        while (current != null) {
            try {
                return current.getDeclaredField(fieldName);
            } catch (NoSuchFieldException ignored) {
                current = current.getSuperclass();
            }
        }

        return null;
    }

    private static void injectValues(Collector collector, Map<String, Object> params) throws Exception {
        Class<?> clazz = collector.getClass();

        for (Map.Entry<String, Object> entry : params.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();

            Field field = findField(clazz, key);
            if (field == null) {
                continue;
            }

            field.setAccessible(true);
            Object converted = adaptToFieldType(field.getType(), value);
            field.set(collector, converted);
        }
    }

    private static Object adaptToFieldType(Class<?> fieldType, Object value) {
        if (value == null) {
            return null;
        }

        if (fieldType.isAssignableFrom(value.getClass())) {
            return value;
        }

        if (fieldType == boolean.class || fieldType == Boolean.class) {
            return Boolean.parseBoolean(value.toString());
        }

        if (fieldType == int.class || fieldType == Integer.class) {
            return Integer.parseInt(value.toString());
        }

        if (fieldType == long.class || fieldType == Long.class) {
            return Long.parseLong(value.toString());
        }

        if (fieldType == float.class || fieldType == Float.class) {
            return Float.parseFloat(value.toString());
        }

        if (fieldType == double.class || fieldType == Double.class) {
            return Double.parseDouble(value.toString());
        }

        if (fieldType == String.class) {
            return value.toString();
        }

        if (fieldType == Path.class) {
            return Path.of(value.toString());
        }

        if (fieldType.isEnum()) {
            @SuppressWarnings({ "rawtypes", "unchecked" })
            Enum<?> enumValue = Enum.valueOf((Class<? extends Enum>) fieldType, value.toString());
            return enumValue;
        }

        return value;
    }

    private static Object convert(String input, Class<?> type) {
        if (type == String.class) {
            return input;
        }

        if (type == boolean.class || type == Boolean.class) {
            return Boolean.parseBoolean(input);
        }

        if (type == int.class || type == Integer.class) {
            return Integer.parseInt(input);
        }

        if (type == long.class || type == Long.class) {
            return Long.parseLong(input);
        }

        if (type == float.class || type == Float.class) {
            return Float.parseFloat(input);
        }

        if (type == double.class || type == Double.class) {
            return Double.parseDouble(input);
        }

        if (type == Path.class) {
            return Path.of(input);
        }

        if (type.isEnum()) {
            @SuppressWarnings({ "rawtypes", "unchecked" })
            Enum<?> enumValue = Enum.valueOf((Class<? extends Enum>) type, input);
            return enumValue;
        }

        return input;
    }
}
        
        */
    }
}
