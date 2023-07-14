package controllers;

import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import dto.AvailableFlowDTO;
import dto.UserInfoDTO;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;
import utils.Constants;
import utils.HttpClientUtil;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.TimerTask;
import java.util.function.Consumer;

public class UpdatesRefresher extends TimerTask {
    private final Consumer<List<AvailableFlowDTO>> flowsListConsumer;

    private final Consumer<UserInfoDTO> userInfoConsumer;

    private int  historyVersion;

    private boolean isManager;


    public UpdatesRefresher(Consumer<List<AvailableFlowDTO>> flowsListConsumer,Consumer<UserInfoDTO> userInfoConsumer) {
        this.flowsListConsumer = flowsListConsumer;
        this.userInfoConsumer=userInfoConsumer;
        historyVersion=0;
        isManager=false;
    }

    @Override
    public void run() {


        HttpUrl.Builder urlBuilder = HttpUrl
                .parse(Constants.FULL_SERVER_PATH + "/user-updates")
                .newBuilder();
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
                            if(!jsonArray.get(0).isJsonNull()) {
                                Type listType = new TypeToken<List<AvailableFlowDTO>>() {
                                }.getType();
                                List<AvailableFlowDTO> availableFlows = Constants.GSON_INSTANCE
                                        .fromJson(jsonArray.get(0).getAsString(), listType);
                                flowsListConsumer.accept(availableFlows);
                            }
                            else
                                flowsListConsumer.accept(new ArrayList<>());
                        }
                        catch (Exception e){
                            System.out.println("Error:" + e.getMessage() );
                        }

                        try {
                            if(!jsonArray.get(1).isJsonNull()){
                               UserInfoDTO userInfo =Constants.GSON_INSTANCE
                                       .fromJson(jsonArray.get(1).getAsString(),UserInfoDTO.class);
                               userInfoConsumer.accept(userInfo);
                               updateHistory(userInfo);
                            }
                        }
                        catch (Exception e){
                            System.out.println("Error:" + e.getMessage() );
                        }
                    }
                }
                if(response.body() != null)
                    response.body().close();
            }
        });
    }



    private void updateHistory(UserInfoDTO userInfo){

        boolean change=false;
        String RESOURCE ="/get-history";


        if(userInfo.getManager()!=isManager){
            historyVersion=0;
            isManager= userInfo.getManager();
            change=true;
        }


        if(userInfo.getVersion()<historyVersion||change){
            HttpUrl.Builder urlBuilder = HttpUrl.parse(Constants.FULL_SERVER_PATH + RESOURCE)
                    .newBuilder()
                    .addQueryParameter("historyVersion", Integer.toString(historyVersion));
            //send async request and code for response (lidros if change =true)
        }

    }



}

