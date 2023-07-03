package roles;

import java.util.HashSet;
import java.util.Set;

public class Role {
    private final String name;
    private final String description;
    private Set<String> flowsAssigned;
    private Set<String> usersAssigned;

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

    public void addUser(String userName){
        usersAssigned.add(userName);
    }

    public void removeUser(String userName){
        usersAssigned.add(userName);
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }
}
