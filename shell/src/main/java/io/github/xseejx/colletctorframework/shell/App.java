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

        System.out.println("Waiting for collector result...");

        String s = activator.activateRequest("generic.test", Map.of(
            "value1", false
        ));

        System.out.println("Collector execution completed.");

        System.out.println("Collector result: " + s);
    }
}
