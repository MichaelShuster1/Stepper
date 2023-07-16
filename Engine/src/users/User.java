package users;

import dto.AvailableFlowDTO;
import dto.FlowExecutionDTO;
import dto.UserDetailsDTO;
import dto.UserInfoDTO;
import flow.Flow;
import flow.FlowExecution;
import flow.FlowHistory;
import roles.Role;

import java.util.*;

public class User {
    private String name;
    boolean isManager;
    private int numOfFlowsPerformed;
    private Map<String, Flow> flows;
    private Flow currentFlow;
    private Map<String, Role> roles;
    private Map<String, Integer> flowsAppearance;
    private boolean isAllFlows;
    private final List<FlowHistory> flowsHistory;




    public User(String name) {
        this.name = name;
        this.flows = new HashMap<>();
        currentFlow = null;
        isManager = false;
        numOfFlowsPerformed = 0;
        flowsAppearance = new HashMap<>();
        roles = new HashMap<>();
        isAllFlows = false;
        flowsHistory = new ArrayList<>();
    }

    public void addFlow(Flow flow)
    {
        if(!flows.containsKey(flow.getName()))
            flows.put(flow.getName(), new Flow(flow));
    }

    public void removeFlow(String flowName) {
        if(flows.containsKey(flowName)) {
            flows.remove(flowName);
            if(currentFlow != null && currentFlow.getName().equals(flowName))
                currentFlow = null;
        }
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setFlows(Map<String, Flow> flows) {
        this.flows = flows;
    }

    public void setCurrentFlow(String flowName) {
        if(flows.containsKey(flowName))
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

    public boolean isManager() {
        return isManager;
    }

    public void setManager(boolean manager) {
        isManager = manager;
    }

    public int getNumOfFlowsPerformed() {
        return numOfFlowsPerformed;
    }

    public void setNumOfFlowsPerformed(int numOfFlowsPerformed) {
        this.numOfFlowsPerformed = numOfFlowsPerformed;
    }

    public synchronized void incrementNumOfFlowsPerformed() {
        numOfFlowsPerformed++;
    }

    public UserInfoDTO getUserInformation()
    {
        Set<String> rolesSet = new HashSet<>();
        rolesSet.addAll(roles.keySet());
        if(isAllFlows)
            rolesSet.add("All Flows");
        else
            rolesSet.remove("All Flows");
        UserDetailsDTO userDetailsDTO=new UserDetailsDTO(name,isManager);
        return new UserInfoDTO(userDetailsDTO,flows.keySet().size(),numOfFlowsPerformed,
                rolesSet,flowsHistory.size());
    }

    public UserInfoDTO getUserInformation(int totalHistory)
    {
        Set<String> rolesSet = new HashSet<>();
        rolesSet.addAll(roles.keySet());
        int currentVersion;
        if (isManager)
            currentVersion = totalHistory;
        else
            currentVersion = flowsHistory.size();
        if(isAllFlows)
            rolesSet.add("All Flows");
        else
            rolesSet.remove("All Flows");
        UserDetailsDTO userDetailsDTO=new UserDetailsDTO(name,isManager);
        return new UserInfoDTO(userDetailsDTO,flows.keySet().size(),numOfFlowsPerformed,
                rolesSet,currentVersion);
    }

    public void addFlowAppearance (String flowName) {
        flowsAppearance.compute(flowName, (k, v) -> v == null ? 1 : v + 1);
    }

    public void addRole(Role role) {
        if(!roles.containsKey(role.getName())) {
            Set<String> flows = role.getFlowsAssigned();
            for (String flowName : flows)
                addFlowAppearance(flowName);
            roles.put(role.getName(), role);
            role.addUser(name);
        }
    }

    public void removeRole(Role role) {
        Set<String> flows = role.getFlowsAssigned();
        for (String flowName : flows) {
            flowsAppearance.compute(flowName, (k, v) -> v == null ? 0 : v - 1);
            if (flowsAppearance.get(flowName) == 0)
                flowsAppearance.remove(flowName);
        }
        roles.remove(role.getName());
        role.removeUser(name);
    }


    public Map<String, Role> getRoles() {
        return roles;
    }

    public Map<String, Integer> getFlowsAppearance() {
        return flowsAppearance;
    }

    public boolean isAllFlows() {
        return isAllFlows;
    }

    public void setAllFlows(boolean allFlows) {
        isAllFlows = allFlows;
    }

    public synchronized void updateUserFlow(Flow flow, String roleName) {
        if(roles.containsKey(roleName)) {
            addFlow(flow);
            addFlowAppearance(flow.getName());
        }
    }


    public boolean haveRole(String roleName) {
        if(roleName.equals("All Flows"))
            return isAllFlows;
        return roles.containsKey(roleName);
    }

    public void removeFlowAppearance(String flowName) {
        flowsAppearance.compute(flowName, (k, v) -> v == null ? 0 : v - 1);
        if(flowsAppearance.get(flowName) == 0)
            removeFlow(flowName);
    }

    public List<FlowExecutionDTO> getFlowsHistoryDelta(int historyVersion)
    {
        List<FlowExecutionDTO> flowsList = new ArrayList<>();
        synchronized (flowsHistory) {
            int delta = flowsHistory.size() - historyVersion;
            for (int i = 0; i < delta; i++) {
                flowsList.add(flowsHistory.get(i).getFullData());
            }
        }
        return flowsList;
    }

    public void addFlowHistory(FlowHistory flowHistory) {
        synchronized (flowsHistory) {
            flowsHistory.add(0, flowHistory);
        }
    }

    public synchronized boolean havePermissionForFlow(String flowName) {
        return flows.containsKey(flowName);
    }

}
