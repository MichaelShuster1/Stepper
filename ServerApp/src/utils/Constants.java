package utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dto.StepExtensionDTO;

public class Constants {
    public static final String ADMIN_CONNECTED = "AdminConnected";

    public static final String FLOW_MANAGER ="FlowManager";

    public static final String FLOW_NAME = "flowName";

    public static final String JSON_FORMAT = "application/json";

    public final static Gson GSON_INSTANCE = new GsonBuilder()
            .registerTypeAdapter(StepExtensionDTO.class, new StepExtensionDTOSerializer())
            // Other Gson configuration
            .create();
}
