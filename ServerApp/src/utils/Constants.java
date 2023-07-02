package utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class Constants {
    public static final String ADMIN_CONNECTED = "AdminConnected";

    public static final String FLOW_MANAGER ="FlowManager";

    public static final String FLOW_NAME = "flowName";

    public static final String JSON_FORMAT = "application/json";

    public static final String TEXT_FORMAT = "text/plain";

    public static final String INVALID_PARAMETER = "Invalid query parameters";

    public static final String UNAUTHORIZED_ACCESS = "Unauthorized access";

    public final static Gson GSON_INSTANCE = new Gson();
}
