package controllers;

import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import com.sun.istack.internal.NotNull;
import dto.AvailableFlowDTO;
import dto.FlowExecutionDTO;
import dto.StatisticsDTO;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.Response;
import utils.Constants;
import utils.HttpClientUtil;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.TimerTask;
import java.util.function.Consumer;

public class UpdatesRefresher extends TimerTask {
    private final Consumer<List<FlowExecutionDTO>> flowsListConsumer;
    private final Consumer<StatisticsDTO> statisticsConsumer;

    private final Integer historyVersionConsumer;
    AppController appController;



    public UpdatesRefresher(Consumer<List<FlowExecutionDTO>> flowsListConsumer, Consumer<StatisticsDTO> statistics,
                            AppController appController, Integer historyVersionConsumer) {
        this.flowsListConsumer = flowsListConsumer;
        this.statisticsConsumer = statistics;
        this.appController = appController;
        this.historyVersionConsumer = historyVersionConsumer;
    }

    @Override
    public void run() {


        HttpUrl.Builder urlBuilder = HttpUrl.parse(Constants.FULL_SERVER_PATH + "/get-updates")
                .newBuilder()
                .addQueryParameter("historyVersion", appController.getHistoryVersion().toString());
        HttpClientUtil.runAsync(urlBuilder.build().toString(), new Callback() {

            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if(response.code() == 200) {
                    if(response.body() != null) {
                        JsonArray jsonArray = JsonParser.parseString(response.body().string()).getAsJsonArray();
                        try {
                            Type listType = new TypeToken<List<FlowExecutionDTO>>() {}.getType();
                            if(!jsonArray.get(0).isJsonNull()) {
                                List<FlowExecutionDTO> historyFlows = Constants.GSON_INSTANCE.fromJson(jsonArray.get(0).getAsString(), listType);
                                flowsListConsumer.accept(historyFlows);
                            }
                        }
                        catch (Exception e) {
                            System.out.println("Error:" + e.getMessage() );
                        }
                        try {
                            if(!jsonArray.get(0).isJsonNull()) {
                                StatisticsDTO statisticsDTO = Constants.GSON_INSTANCE.fromJson(jsonArray.get(1).getAsString(), StatisticsDTO.class);
                                statisticsConsumer.accept(statisticsDTO);
                            }
                        }
                        catch (Exception e) {
                            System.out.println("Error:" + e.getMessage());
                        }

                    }
                }
                if(response.body() != null)
                    response.body().close();
            }
        });
    }
}
