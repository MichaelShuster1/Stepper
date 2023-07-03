package roles;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Role {
    private final String name;
    private final String description;
    private Set<String> flowsAssigned;

    public Role(String name, String description) {
        this.name = name;
        this.description = description;
        this.flowsAssigned=new HashSet<>();
    }

    public void addFlow(String flowName){
        flowsAssigned.add(flowName);
    }

    public void removeFlow(String flowName){
        flowsAssigned.remove(flowName);
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public void addFlows(Set<String> flows) {flowsAssigned.addAll(flows);}

    public Set<String> getFlowsAssigned() {
        return flowsAssigned;
    }
}
