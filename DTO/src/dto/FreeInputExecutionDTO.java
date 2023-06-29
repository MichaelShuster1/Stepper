package dto;

import java.util.List;

public class FreeInputExecutionDTO extends DataDefintionDTO  {
    private final String data;
    private final List<String> allowedValues;
    private final boolean necessity;


    public FreeInputExecutionDTO(DataDefintionDTO other, String data, boolean necessity) {
        super(other);
        this.data = data;
        this.necessity = necessity;
        this.allowedValues=null;
    }

    public FreeInputExecutionDTO(DataDefintionDTO other, String data, boolean necessity,List<String> allowedValues) {
        super(other);
        this.data = data;
        this.necessity = necessity;
        this.allowedValues=allowedValues;
    }

    public String getData() {
        return data;
    }

    public boolean isMandatory() {
        return necessity;
    }

    public List<String> getAllowedValues() {
        return allowedValues;
    }
}
