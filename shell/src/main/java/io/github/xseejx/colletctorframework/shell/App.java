package io.github.xseejx.colletctorframework.shell;

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
        CollectorRequestActivator activator = new CollectorRequestActivator();
        // print result of collector execution
        // wait for the result to be available and then print it
        System.out.println("Waiting for collector result...");
        // Simulate waiting (in a real scenario, you might use a callback or future)
        String s = activator.activateRequest("hardware.cpu", Map.of(
            "includePerCore", true,
            "includeTemperature", true
        ));
        System.out.println("Collector execution completed.");

        System.out.println("Collector result: " + s);
    }
}
