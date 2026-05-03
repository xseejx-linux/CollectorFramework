package io.github.xseejx.colletctorframework.shell;

import java.util.List;
import java.util.Map;

import io.github.xseejx.colletctorframework.core.request.CollectorRequestActivator;

/**
 * Test Application for the Collector Framework. 
 * This class demonstrates how to use the CollectorRequestActivator to execute a collector and print the result. 
 * In a real application, you would likely have a more
 *
 */
public class App 
{
    public static void main( String[] args )
    {
        //CollectorRequestActivator activator = new CollectorRequestActivator();

        //System.out.println("Waiting for collector result...");

        /*String s = activator.activateRequest("generic.test", Map.of(
            "value1", false
        ));*/
        /*List<String> available = activator.listAvailable();
        System.out.println("Available collectors: " + available);*/

        // pass a List<Map<String, Map<String, Object>>> to activate multiple collectors at once
        /*List<Map<String, Map<String, Object>>> requests = List.of(
           Map.of("generic.test", Map.of("value1", true, "value2", "Test1")),
           Map.of("hardware.cpu", Map.of("includePerCore", true))
        );*/

        //List<String> results = activator.activateRequestsV2(requests);
        
        // If Collector Name is wrong then nothing happens


        //System.out.println("Results: " + results);
        //System.out.println("Collector execution completed.");

        //System.out.println("Collector result: " + s);
    }
}
