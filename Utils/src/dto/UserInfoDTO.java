package dto;

import java.util.Set;

public class UserInfoDTO {

    private final  UserDetailsDTO userDetailsDTO;
    private final Integer numOfDefinedFlows;
    private final Integer numOfFlowsPerformed;
    private final Set<String> roles;
    private final int historyVersion;

    public UserInfoDTO(UserDetailsDTO userDetailsDTO, Integer numOfDefinedFlows, Integer numOfFlowsPerformed,
                       Set<String> roles,int historyVersion) {
        this.numOfDefinedFlows = numOfDefinedFlows;
        this.numOfFlowsPerformed = numOfFlowsPerformed;
        this.roles = roles;
        this.userDetailsDTO=userDetailsDTO;
        this.historyVersion=historyVersion;
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

    public UserDetailsDTO getUserDetailsDTO() {
        return userDetailsDTO;
    }

    public int getHistoryVersion() {
        return historyVersion;
    }
}
