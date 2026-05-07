package io.github.xseejx.collectorframework.api;


/**
 * Interface: ResultDispatcher
 * All classes that implements this interface,
 * will be called inside executeInternal(), and the classes must have the functionality
 * to forward data through a Queue/Broker or any other type of Buffer
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
