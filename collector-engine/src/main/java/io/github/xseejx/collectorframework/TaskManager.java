package io.github.xseejx.collectorframework;

// IMPORTS QUARTZ (Job detail)
import org.quartz.JobDetail;
import org.quartz.Trigger;
// IMPORTS INTERNAL 
import io.github.xseejx.collectorframework.internal.SchedulerProvider;
import io.github.xseejx.collectorframework.internal.registry.CollectorRegistry;

/**
 * Class: TaskManager
 * Manages Creation of tasks and their execution and termination.
 * It is used by application layer, SchedulerProvider and TaskBuilder are used to create and manage tasks.
 */
public class TaskManager {

    private final CollectorRegistry registry = new CollectorRegistry();
    private final TaskBuilder taskBuilder = new TaskBuilder();
    private final SchedulerProvider schedulerProvider = new SchedulerProvider();


    /**
     * Creates a new task based on the provided TaskModel.
     * It validates the collector, builds the job and trigger, and schedules it.
     * @param model
     */
    public String createTask(TaskModel model){
        listAvailable();
        registry.get(model.getCollectorName())
            .orElseThrow(() -> new IllegalArgumentException("Collector not found"));

        JobDetail job = taskBuilder.buildTaskDetail(model);
        Trigger trigger = taskBuilder.buildTrigger(job, model);

        schedulerProvider.scheduleJob(job, trigger, registry);
        return job.getKey().getName();
    }

    /**
     * Deletes a scheduled task by its ID and group name. 
     * It returns true if the task was successfully deleted
     * @param taskId
     * @param groupName
     * @return
     */
    public boolean deleteTask(String taskId, String groupName) {
        return schedulerProvider.deleteJob(taskId, groupName);
    }

    /**
     * Shuts down the scheduler, stopping all scheduled tasks.
     * Caution: This will stop all tasks and should be used when the application is shutting down.
     */
    public void shutdown() {
        schedulerProvider.shutdown();
    }

    public void listAvailable() {
        registry.discoverAll();
    }
    
}
