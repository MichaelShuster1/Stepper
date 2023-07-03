package dto;

public class UserInfoDTO {
    private final String name;
    private final Integer numOfDefinedFlows;
    private final Integer numOfFlowsPerformed;

    public UserInfoDTO(String name, Integer numOfDefinedFlows, Integer numOfFlowsPerformed) {
        this.name = name;
        this.numOfDefinedFlows = numOfDefinedFlows;
        this.numOfFlowsPerformed = numOfFlowsPerformed;
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
