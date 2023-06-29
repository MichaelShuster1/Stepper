package utils;

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

    public static void runAsyncPost(String finalUrl, Callback callback,RequestBody requestBody) {
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
            throw new RuntimeException(e);
        }
        return response;
    }


    public static void shutdown() {
        System.out.println("Shutting down HTTP CLIENT");
        HTTP_CLIENT.dispatcher().executorService().shutdown();
        HTTP_CLIENT.connectionPool().evictAll();
    }
}
