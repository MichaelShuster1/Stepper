package exception;

public class MandatoryInputException extends RuntimeException {
    public MandatoryInputException(String errorMessage) {
        super(errorMessage);
    }
}
