package io.github.xseejx.collectorframework;

// IMPORTS
import java.util.Map;
//

/**
 * Class: TaskModel
 * Defines how a Task is composed, structure of task
 */
public class TaskModel {
    private final String collectorName;
    private final Map<String, Object> parameters;
    private final String cronExpression;
    private final String groupName;
    private String desc = "Description";

    /**
     * Constructor for creating an immediate task without a cron expression, thus it will be executed as soon as it's created
     * And it will run as a heartbeat task (if the collector supports it)
     * @param collectorName
     * @param parameters
     */
    public TaskModel(String collectorName, Map<String, Object> parameters, String groupName) {
        this.collectorName = collectorName;
        this.parameters = parameters;
        this.cronExpression = null;
        this.groupName = groupName;
    }
    /**
     * Constructor for creating a scheduled task with a cron expression
     * @param collectorName
     * @param parameters
     * @param cronExpression
     */
    public TaskModel(String collectorName, Map<String, Object> parameters, String cronExpression, String groupName) {
        this.collectorName = collectorName;
        this.parameters = parameters;
        this.cronExpression = cronExpression;
        this.groupName = groupName;
    }

    public Map<String, Object> getParameters() {
        return parameters;
    }

    public String getCollectorName() {
        return collectorName;
    }

    public String getCronExpression(){
        return cronExpression;
    }
    public String getGroups() {
        return groupName;
    }
    public String getDesc() {
        return desc;
    }
    public void setDesc(String desc){
        this.desc = desc;
    }

}
