package utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dto.DataExecutionDTO;

public class Constants {
    public final static String BASE_DOMAIN = "localhost";
    private final static String BASE_URL = "http://" + BASE_DOMAIN + ":8080";
    private final static String CONTEXT_PATH = "/ServerApp";
    public final static String FULL_SERVER_PATH = BASE_URL + CONTEXT_PATH;

    public final static String LOGIN_PAGE = FULL_SERVER_PATH + "/login";

    public final static String SOMETHING_WRONG = "Something went wrong...";

    public final static String CONNECTION_ERROR = "There was a problem connecting to the server";

    public final static String LINE_SEPARATOR = System.getProperty("line.separator");
    public final static int REFRESH_RATE = 2000;
    public final static String CHAT_LINE_FORMATTING = "%tH:%tM:%tS | %.10s: %s%n";



    // GSON instance
    public final static Gson GSON_INSTANCE = new GsonBuilder()
            .registerTypeAdapter(DataExecutionDTO.class,new DataExecutionDTODeserializer())
            .serializeNulls()
            .create();
}
