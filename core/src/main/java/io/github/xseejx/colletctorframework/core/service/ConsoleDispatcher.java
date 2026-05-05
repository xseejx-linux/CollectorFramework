package io.github.xseejx.colletctorframework.core.service;

import io.github.xseejx.colletctorframework.core.api.CollectorResult;
import io.github.xseejx.colletctorframework.core.api.ResultDispatcher;
import org.json.simple.JSONObject;

public class ConsoleDispatcher implements ResultDispatcher {

    @Override
    public void dispatch(String taskId, String groupName, CollectorResult result) {
        JSONObject payload = new JSONObject();
        payload.put("taskId", taskId);
        payload.put("group", groupName);
        payload.put("collectorName", result.getResult().get("collectorName"));
        payload.put("success", result.getResult().get("success"));
        payload.put("errorMessage", result.getResult().get("errorMessage"));
        payload.put("timestamp", result.getResult().get("timestamp"));
        payload.put("data", result.getResult().get("data"));

        System.out.println("[Dispatcher] Task result: " + payload.toJSONString());
    }
}
