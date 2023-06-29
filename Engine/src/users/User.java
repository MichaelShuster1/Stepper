package users;

import flow.Flow;

import java.util.HashMap;
import java.util.Map;

public class User {
    private String name;
    private Map<String, Flow> flows;
    private Flow currentFlow;
    //private List<Role> roles


    public User(String name) {
        this.name = name;
        this.flows = new HashMap<>();
        currentFlow =null;
    }

    public void addFlow(Flow flow)
    {
        flows.put(flow.getName(),flow);
    }

    public void removeFlow(String name) {
        flows.remove(name);
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setFlows(Map<String, Flow> flows) {
        this.flows = flows;
    }

    public void setCurrentFlow(String flowName) {
        currentFlow=flows.get(flowName);
    }

    public String getName() {
        return name;
    }

    public Map<String, Flow> getFlows() {
        return flows;
    }

    public Flow getCurrentFlow() {
        return currentFlow;
    }
}