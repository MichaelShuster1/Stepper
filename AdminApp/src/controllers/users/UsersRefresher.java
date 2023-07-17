package controllers.users;

import com.google.gson.reflect.TypeToken;
import controllers.AppController;
import dto.AvailableFlowDTO;
import dto.UserInfoDTO;
import okhttp3.*;
import utils.Constants;
import utils.HttpClientUtil;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.TimerTask;
import java.util.function.Consumer;

public class UsersRefresher  extends TimerTask {
    private final Consumer<List<UserInfoDTO>> usersListConsumer;

    private final AppController appController;


    public UsersRefresher(Consumer<List<UserInfoDTO>> usersListConsumer,AppController appController) {
        this.appController=appController;
        this.usersListConsumer = usersListConsumer;
    }

    @Override
    public void run() {
        final String RESOURCE ="/get-users";

        String finalUrl = HttpUrl
                .parse(Constants.FULL_SERVER_PATH + RESOURCE)
                .newBuilder()
                .build()
                .toString();

        HttpClientUtil.runAsync(finalUrl, new Callback() {

            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if(response.code() == 200) {
                    if(response.body() != null) {
                        String jsonArrayOfUsers = response.body().string();
                        Type listType = new TypeToken<List<UserInfoDTO>>() {}.getType();
                        List<UserInfoDTO> usersList = Constants.GSON_INSTANCE.fromJson(jsonArrayOfUsers , listType);
                        if(usersList!=null)
                            usersListConsumer.accept(usersList);
                        else
                            usersListConsumer.accept(new ArrayList<>());
                        response.body().close();
                    }
                    else
                        usersListConsumer.accept(new ArrayList<>());
                }
                if(response.body()!=null)
                    response.body().close();
            }
        });

    }
}
