package utils;

import controllers.AppController;
import dto.ResultDTO;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.scene.control.Alert;
import okhttp3.*;

import java.io.IOException;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

public class HttpClientUtil {

    private final static CookieManager simpleCookieManager = new CookieManager();
    private final static OkHttpClient HTTP_CLIENT =
            new OkHttpClient.Builder()
                    .cookieJar(simpleCookieManager)
                    .followRedirects(false)
                    .build();


    public static void runAsync(String finalUrl, Callback callback) {
        Logger.getLogger(OkHttpClient.class.getName()).setLevel(Level.FINE);
        Request request = new Request.Builder()
                .url(finalUrl)
                .build();

        Call call = HttpClientUtil.HTTP_CLIENT.newCall(request);

        call.enqueue(callback);
    }

    public static void runAsyncDelete(String finalUrl, Callback callback) {
        Request request = new Request.Builder()
                .url(finalUrl)
                .delete()
                .build();

        Call call = HttpClientUtil.HTTP_CLIENT.newCall(request);

        call.enqueue(callback);
    }



    public static void runAsyncPost(String finalUrl, RequestBody requestBody,Callback callback) {
        Request request = new Request.Builder()
                .url(finalUrl)
                .post(requestBody)
                .build();

        Call call = HttpClientUtil.HTTP_CLIENT.newCall(request);

        call.enqueue(callback);
    }

    public static Response runSync(String finalUrl)
    {
        Response response=null;
        Request request = new Request.Builder()
                .url(finalUrl)
                .build();
        Call call = HttpClientUtil.HTTP_CLIENT.newCall(request);

        try {
            response=call.execute();
        } catch (IOException e) {

        }
        return response;
    }

    public static void errorMessage(ResponseBody responseBody, AppController appController) {
        try {
            ResultDTO resultDTO = Constants.GSON_INSTANCE.fromJson(responseBody.string(), ResultDTO.class);
            showErrorAlert(resultDTO.getMessage(), appController);
        }
        catch (Exception e) {
           showErrorAlert("Something went wrong...", appController);
        }
    }


    public static void shutdown() {
        System.out.println("Shutting down HTTP CLIENT");
        HTTP_CLIENT.dispatcher().executorService().shutdown();
        HTTP_CLIENT.connectionPool().evictAll();
    }


    public static void showErrorAlert(String message, AppController appController) {
        Platform.runLater(() -> {
        Alert alert =new Alert(Alert.AlertType.ERROR);

        ObservableList<String> stylesheets = appController.getPrimaryStage().getScene().getStylesheets();
        if(stylesheets.size()!=0)
            alert.getDialogPane().getStylesheets().add(stylesheets.get(0));

        alert.setTitle("Error");
        alert.setContentText(message);
        alert.showAndWait();
        });
    }

    public static void removeCookiesOf(String domain) {
        simpleCookieManager.removeCookiesOf(domain);
    }
}
