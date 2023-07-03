package dto;

import java.util.Set;

public class UserInfoDTO {
    private final String name;
    private final Integer numOfDefinedFlows;
    private final Integer numOfFlowsPerformed;

    private final Set<String> roles;

    public UserInfoDTO(String name, Integer numOfDefinedFlows, Integer numOfFlowsPerformed, Set<String> roles) {
        this.name = name;
        this.numOfDefinedFlows = numOfDefinedFlows;
        this.numOfFlowsPerformed = numOfFlowsPerformed;
        this.roles = roles;
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
}
