package io.github.xseejx.colletctorframework.core.api;


/**
 * Called inside executeInternal(), it will forward data through Queue/Broker
 */


public interface ResultDispatcher {

    /**
     * Dispatches the result of a collector execution.
     * This method is responsible for forwarding the result data, 
     * which may include success status, error messages, timestamps, and any collected data, 
     * to the appropriate destination such as a message queue or broker.
     * The implementation of this method can vary based on the specific requirements of the application and the chosen communication mechanism.
     * @param taskId
     * @param groupName
     * @param result
     */
    void dispatch(String taskId, String groupName, CollectorResult result);
}
