package controllers.flowdefinition;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import dto.AvailableFlowDTO;
import javafx.beans.property.BooleanProperty;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;
import java.util.TimerTask;
import java.util.function.Consumer;

public class flowDefinitionRefresher extends TimerTask {
    private final Consumer<List<AvailableFlowDTO>> flowsListConsumer;


    private OkHttpClient client;

    public flowDefinitionRefresher( Consumer<List<AvailableFlowDTO>> flowsListConsumer, OkHttpClient client) {
        this.flowsListConsumer = flowsListConsumer;
        this.client = client;
    }

    @Override
    public void run() {


        HttpUrl.Builder urlBuilder = HttpUrl.parse("http://localhost:8080/ServerApp/get-flows").newBuilder();
        Request request = new Request.Builder()
                .url(urlBuilder.build().toString())
                .build();
        client.newCall(request).enqueue(new Callback() {

            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                String jsonArrayOfFlows = response.body().string();
                Gson gson = new Gson();
                Type listType = new TypeToken<List<AvailableFlowDTO>>(){}.getType();
                List<AvailableFlowDTO> availableFlows = gson.fromJson(jsonArrayOfFlows, listType);
                flowsListConsumer.accept(availableFlows);
            }
        });
    }
}
