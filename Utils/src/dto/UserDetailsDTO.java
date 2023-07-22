package dto;

public class UserDetailsDTO {
    private final String userName;
    private final Boolean isManager;

    public UserDetailsDTO(String userName, Boolean isManager) {
        this.userName = userName;
        this.isManager = isManager;
    }

    public String getUserName() {
        return userName;
    }

    public Boolean getManager() {
        return isManager;
    }
}
