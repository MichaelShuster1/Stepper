package utils;

import com.google.gson.Gson;

public class Constants {
    public static final String ADMIN_CONNECTED = "AdminConnected";

    public static final String FLOW_MANAGER ="FlowManager";
    public static final String FLOW_NAME = "flowName";

    public static final String JSON_FORMAT = "application/json";
    public static final String TEXT_FORMAT = "text/plain";
    public static final String INVALID_PARAMETER = "Invalid query parameters";
    public static final String UNAUTHORIZED_ACCESS = "Unauthorized access";
    public static final String FLOWS_LOCK="flowsLock";
    public static final String USERS_LOCK="usersLock";
    public static final String ROLE_UPDATE_LOCK="roleUpdateLock";
    public static final String USER_ROLES_LOCK="userRolesLock";

    public static final String CHAT_PARAMETER = "userstring";
    public static final String CHAT_VERSION_PARAMETER = "chatversion";

    public static final int INT_PARAMETER_ERROR = Integer.MIN_VALUE;

    public final static Gson GSON_INSTANCE = new Gson();
}
