package dto;

public class ResultDTO {
    private final boolean status;
    private final boolean isFlowReady;
    private final String message;

    public ResultDTO(boolean status, String message) {
        this.status = status;
        this.message = message;
        this.isFlowReady=false;
    }

    public ResultDTO(boolean status, String message,boolean isFlowReady) {
        this.status = status;
        this.message = message;
        this.isFlowReady=isFlowReady;
    }

    public ResultDTO(String message){
        this.message=message;
        this.status=false;
        this.isFlowReady=false;
    }


    public boolean getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public boolean isFlowReady() {
        return isFlowReady;
    }
}
