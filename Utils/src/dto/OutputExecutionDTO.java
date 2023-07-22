package dto;

public class OutputExecutionDTO extends DataDefintionDTO {
    private final Object data;

    public OutputExecutionDTO(DataDefintionDTO other, Object data) {
        super(other);
        this.data = data;
    }

    public Object getData() {
        return data;
    }
}
