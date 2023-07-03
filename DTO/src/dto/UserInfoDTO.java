package dto;

import java.util.Set;

public class UserInfoDTO {
    private final String name;
    private final Integer numOfDefinedFlows;
    private final Integer numOfFlowsPerformed;
    private final Set<String> roles;
    private final Boolean isManager;

    public UserInfoDTO(String name, Integer numOfDefinedFlows, Integer numOfFlowsPerformed,
                       Set<String> roles,Boolean isManager) {
        this.name = name;
        this.numOfDefinedFlows = numOfDefinedFlows;
        this.numOfFlowsPerformed = numOfFlowsPerformed;
        this.roles = roles;
        this.isManager=isManager;
    }

    public String getName() {
        return name;
    }

    public Integer getNumOfDefinedFlows() {
        return numOfDefinedFlows;
    }

    public Integer getNumOfFlowsPerformed() {
        return numOfFlowsPerformed;
    }

    public Set<String> getRoles() {
        return roles;
    }

    public Boolean getManager() {
        return isManager;
    }
}
