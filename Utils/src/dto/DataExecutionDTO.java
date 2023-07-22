package dto;

public class DataExecutionDTO extends DataDefintionDTO{

    private final Object data;

    public DataExecutionDTO(String name, String type, Object data) {
        super(name, type);
        this.data = data;
    }
    public DataExecutionDTO(DataDefintionDTO other, Object data) {
        super(other);
        this.data = data;
    }

    public Object getData() {
        return data;
    }
}
