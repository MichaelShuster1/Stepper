package controllers.flowdefinition;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import dto.AvailableFlowDTO;
import javafx.beans.property.BooleanProperty;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;
import utils.Constants;
import utils.HttpClientUtil;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.TimerTask;
import java.util.function.Consumer;

public class flowDefinitionRefresher extends TimerTask {
    private final Consumer<List<AvailableFlowDTO>> flowsListConsumer;



    public flowDefinitionRefresher( Consumer<List<AvailableFlowDTO>> flowsListConsumer) {
        this.flowsListConsumer = flowsListConsumer;
    }

    @Override
    public void run() {


        HttpUrl.Builder urlBuilder = HttpUrl.parse(Constants.FULL_SERVER_PATH + "/get-flows").newBuilder();
        HttpClientUtil.runAsync(urlBuilder.build().toString(), new Callback() {

            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if(response.code() == 200) {
                    if(response.body() != null) {
                        String jsonArrayOfFlows = response.body().string();
                        Type listType = new TypeToken<List<AvailableFlowDTO>>() {}.getType();
                        List<AvailableFlowDTO> availableFlows = Constants.GSON_INSTANCE.fromJson(jsonArrayOfFlows, listType);
                        flowsListConsumer.accept(availableFlows);
                    }
                    else
                        flowsListConsumer.accept(new ArrayList<>());
                }


            }
        });
    }
}
