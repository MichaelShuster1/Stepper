package dto;

import java.util.List;
import java.util.Map;

public class StepExtensionDTO {
    private List<String> logs;

    private List<DataExecutionDTO> inputs;

    private List<DataExecutionDTO> outputs;

    public StepExtensionDTO(List<String> logs, List<DataExecutionDTO> inputs, List<DataExecutionDTO> outputs) {
        this.logs = logs;
        this.inputs = inputs;
        this.outputs = outputs;
    }

    public List<DataExecutionDTO> getInputs() {
        return inputs;
    }

    public List<DataExecutionDTO> getOutputs() {
        return outputs;
    }

    public List<String> getLogs() {
        return logs;
    }
}
