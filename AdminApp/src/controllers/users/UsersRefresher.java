package controllers.users;

import com.google.gson.reflect.TypeToken;
import dto.AvailableFlowDTO;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;
import utils.Constants;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.TimerTask;
import java.util.function.Consumer;

public class UsersRefresher  extends TimerTask {
    private final Consumer<List<String>> usersListConsumer;

    public UsersRefresher(Consumer<List<String>> usersListConsumer) {
        this.usersListConsumer = usersListConsumer;
    }

    @Override
    public void run() {
        final String RESOURCE ="/get-users";
        Request request = new Request.Builder()
                .url(Constants.FULL_SERVER_PATH+RESOURCE)
                .build();

        Constants.HTTP_CLIENT.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if(response.code() == 200) {
                    if(response.body() != null) {
                        String jsonArrayOfUsers = response.body().string();
                        Type listType = new TypeToken<List<String>>() {}.getType();
                        List<String> usersList = Constants.GSON_INSTANCE.fromJson(jsonArrayOfUsers , listType);
                        usersListConsumer.accept(usersList);
                        response.body().close();
                    }
                    else
                        usersListConsumer.accept(new ArrayList<>());
                }
            }
        });

    }
}
