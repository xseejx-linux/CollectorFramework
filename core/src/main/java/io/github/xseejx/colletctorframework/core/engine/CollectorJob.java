package io.github.xseejx.colletctorframework.core.engine;

import java.util.Map;

import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.quartz.QuartzJobBean;

import io.github.xseejx.colletctorframework.core.api.CollectorResult;
import io.github.xseejx.colletctorframework.core.api.ResultDispatcher;
import io.github.xseejx.colletctorframework.core.registry.CollectorRegistry;
import io.github.xseejx.colletctorframework.core.service.ConsoleDispatcher;
import io.github.xseejx.colletctorframework.core.service.ServiceModel;

public class CollectorJob extends QuartzJobBean {
    private static final Logger logger = LoggerFactory.getLogger(CollectorEngine.class);

    private final CollectorRegistry registry = new CollectorRegistry();
    private final CollectorEngine engine = new CollectorEngine(registry);
        
    /**
     * This method is called when the scheduled job is triggered.
     * It retrieves the collector name and parameters from the job data map,
     * discovers available collectors, executes the specified collector synchronously, 
     * and dispatches the result using a ResultDispatcher.
     * If any exceptions occur during execution, it logs an error message. Finally, it ensures that the CollectorEngine is properly shut down after execution.
     */
    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        JobDataMap dataMap = context.getMergedJobDataMap();
        String collectorName = dataMap.getString("CollectorName");

        @SuppressWarnings("unchecked")
        Map<String, Object> parameters = (Map<String, Object>) dataMap.get("Parameters");

        registry.discoverAll();        

        try {
            CollectorResult result = engine.executeSync(new ServiceModel(collectorName, parameters)).get();
            //TODO: Dispacther make it dynamic
            ResultDispatcher dispatcher = new ConsoleDispatcher();
            
            String taskId = context.getJobDetail().getKey().getName();
            String groupName = context.getJobDetail().getKey().getGroup();
            
            dispatcher.dispatch(taskId, groupName, result);
        } catch (Exception e) {
            String taskId = context.getJobDetail().getKey().getName();
            String groupName = context.getJobDetail().getKey().getGroup();
            
            logger.error("[CollectorJob] Failed to execute task [{}/{}]: {}", groupName, taskId, e.getMessage());
        } finally {
            engine.shutdown();
        }
    }
}
