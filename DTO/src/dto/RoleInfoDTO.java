package dto;

import java.util.Set;

public class RoleInfoDTO {
    private final String name;
    private final String description;
    private final Set<String> flowsAssigned;
    private final Set<String> usersAssigned;

    public RoleInfoDTO(String name, String description, Set<String> flowsAssigned, Set<String> usersAssigned) {
        this.name = name;
        this.description = description;
        this.flowsAssigned = flowsAssigned;
        this.usersAssigned = usersAssigned;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Set<String> getFlowsAssigned() {
        return flowsAssigned;
    }

    public Set<String> getUsersAssigned() {
        return usersAssigned;
    }
}
