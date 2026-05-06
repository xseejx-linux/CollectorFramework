package io.github.xseejx.colletctorframework.core.engine;

import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;
import org.quartz.Trigger;
import org.quartz.impl.StdSchedulerFactory;

import io.github.xseejx.colletctorframework.core.registry.CollectorRegistry;

public class SchedulerProvider {
    private Scheduler scheduler;

    public SchedulerProvider(){
        try {
            createScheduler();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * it creates the scheduler
     * @throws SchedulerException
     */
    private void createScheduler() throws SchedulerException {
        if (scheduler == null) {
            SchedulerFactory factory = new StdSchedulerFactory();
            scheduler = factory.getScheduler();
            scheduler.start();
        }
    }

    /**
     * Schedules a job with the given details.
     * @param job
     * @param trigger
     * @param registry
     */
    public void scheduleJob(JobDetail job, Trigger trigger, CollectorRegistry registry) {
        try {
            scheduler.scheduleJob(job, trigger);
        } catch (SchedulerException e) {
            throw new RuntimeException("Failed to schedule job: " + job.getKey(), e);
        }
    }

    /**
     * Deletes a scheduled job by its name and group name.
     * @param jobName
     * @param groupName
     * @return
     */
    public boolean deleteJob(String jobName, String groupName) {
        try {
            return scheduler.deleteJob(new JobKey(jobName, groupName));
        } catch (SchedulerException e) {
            throw new RuntimeException("Failed to delete job: " + groupName + "/" + jobName, e);
        }
    }

    /**
     * Shuts down the scheduler, stopping all scheduled tasks.
     * @throws SchedulerException
     */
    public void shutdown() {
        try {
            if (scheduler != null && !scheduler.isShutdown()) {
                scheduler.shutdown(true);
            }
        } catch (SchedulerException e) {
            throw new RuntimeException("Failed to shutdown scheduler", e);
        }
    }
}
