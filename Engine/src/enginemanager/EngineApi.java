package enginemanager;

import dto.*;
import flow.FlowExecution;
import flow.FlowHistory;
import users.User;

import javax.xml.bind.JAXBException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface EngineApi {

    Set<String> getRolesNames();

    Set<String> loadXmlFile(InputStream inputStream, Map<String, User> users) throws Exception;

    Set<String> getFlowsNames();

    List<AvailableFlowDTO> getAvailableFlows(User user);

    void updateUserRoles(User user, Set<String> roleNames);

    int getFlowIndexByName(String name);

    FlowDefinitionDTO getFlowDefinition(int flowIndex);

    FlowDefinitionDTO getFlowDefinition(String flowName);

    InputsDTO getFlowInputs(User user, String flowName);

    ResultDTO processInput(User user,String inputName, String data);

    InputData clearInputData(User user, String inputName);

    FreeInputExecutionDTO getInputData(User user, String inputName);

    boolean isFlowReady(User user);

    String runFlow(User user);

    List<String> getInitialHistoryList();

    FlowExecutionDTO getFullHistoryData(int flowIndex);

    FlowExecutionDTO getHistoryDataOfFlow(String id);

    StatisticsDTO getStatistics();

    int getCurrInitializedFlowsCount();

    ContinutionMenuDTO getContinutionMenuDTO(User user);

    ContinutionMenuDTO getContinuationMenuDTOByName(User user, String flowName);

    void reUseInputsData(User user, List<FreeInputExecutionDTO> inputs, String flowName);

    void doContinuation(User user, FlowExecution flowExecution, String targetName);

    FlowExecution getFlowExecution(String ID);

    void endProcess();

    List<FlowExecutionDTO> getFlowsHistoryDelta(int historyVersion);

    boolean addRole(RoleInfoDTO roleInfoDTO);

    void updateRole(String roleName, Set<String> flowNames, Map<String, User> users);

    RoleInfoDTO getRoleInfo(String roleName);

    List<FlowExecutionDTO> getFlowsHistoryDeltaFromUser(int historyVersion, User user);

    int getHistoryVersion();

    boolean checkParameterValidity(String parameter, String type, User user);
}
