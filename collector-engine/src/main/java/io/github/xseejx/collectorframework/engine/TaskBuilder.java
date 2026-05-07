package io.github.xseejx.collectorframework.engine;


// IMPORTS
import java.util.UUID;
// IMPORTS QUARTZ (For building Job)
import org.quartz.JobBuilder;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
// IMPORTS QUARTZ (For building trigger)
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
// IMPORTS INTERNAL 
import io.github.xseejx.collectorframework.engine.internal.CollectorJob;

/**
 * Class: TaskBuilder
 * Build Task aswell as their trigger
 * 
 * TODO: Possible substitute for Service unless Services finds other purposes
 */
class TaskBuilder {

    /**
     * Builds a JobDetail object based on the provided TaskModel.
     * It extracts the collector name and parameters from the TaskModel, 
     * creates a JobDataMap to store this information,
     * and constructs a JobDetail object with a unique identity, description, amd group name.
     * The JobDetail is configured to be stored durably, 
     * meaning it will persist even if there are no triggers associated with it.
     * @param model
     * @return
     */
    public JobDetail buildTaskDetail(TaskModel model){
        
        JobDataMap jobDataMap = new JobDataMap();

        jobDataMap.put("CollectorName", model.getCollectorName());
        jobDataMap.put("Parameters", model.getParameters());
        jobDataMap.put("DispatcherName", model.getDispatcherName());

        return JobBuilder.newJob(CollectorJob.class)
                .withIdentity(UUID.randomUUID().toString(), model.getGroups())
                .withDescription(model.getDesc())
                .usingJobData(jobDataMap)
                .storeDurably()
                .build();
    }

    /**
     * Builds a Trigger object based on the provided JobDetail and TaskModel.
     * If the TaskModel contains a cron expression, it creates a cron trigger 
     * that schedules the job according to the specified cron schedule. 
     * If no cron expression is provided, it creates a simple trigger that starts the job immediately.
     * @param jobDetail
     * @param model
     * @return
     */
    public Trigger buildTrigger(JobDetail jobDetail, TaskModel model){
            if (model.getCronExpression() != null) {
            return TriggerBuilder.newTrigger()
                    .forJob(jobDetail)
                    .withSchedule(
                        org.quartz.CronScheduleBuilder.cronSchedule(model.getCronExpression())
                    )
                    .build();
        }
        // immediate execution
        return TriggerBuilder.newTrigger()
                .forJob(jobDetail)
                .startNow()
                .build();
    
    }
    

}
