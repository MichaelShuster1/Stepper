package enginemanager;

import dto.*;
import flow.FlowExecution;
import users.User;

import javax.xml.bind.JAXBException;
import java.io.InputStream;
import java.util.List;

public interface EngineApi {
    void loadXmlFile(InputStream inputStream) throws Exception;

    void loadXmlFile(String path) throws JAXBException;

    List<String> getFlowsNames();

    List<AvailableFlowDTO> getAvailableFlows();

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

    ResultDTO saveDataOfSystemToFile(String path);

    ResultDTO loadDataOfSystemFromFile(String path);

    int getCurrInitializedFlowsCount();

    ContinutionMenuDTO getContinutionMenuDTO();

    ContinutionMenuDTO getContinuationMenuDTOByName(String flowName);

    void reUseInputsData(FlowExecutionDTO flowExecutionDTO);

    void doContinuation(FlowExecution flowExecution, String targetName);

    FlowExecution getFlowExecution(String ID);

    List<String> getEnumerationAllowedValues(User user, String inputName);

    String getInputDefaultName(User user , String inputName);

    void endProcess();


}
