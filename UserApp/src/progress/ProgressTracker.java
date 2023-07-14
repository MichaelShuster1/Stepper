package progress;

import controllers.AppController;
import dto.*;
import enginemanager.EngineApi;
import javafx.application.Platform;
import javafx.concurrent.Task;
import okhttp3.HttpUrl;
import okhttp3.Response;
import utils.*;

public class ProgressTracker extends Task<Boolean> {

    String currentFlowId;

    AppController appController;

    EngineApi engine;

    public ProgressTracker(AppController appController,EngineApi engine)
    {
        this.appController=appController;
        this.engine=engine;
    }

    public void setFlowId(String id)
    {
        synchronized (currentFlowId) {
            currentFlowId = id;
        }
    }

    public void resetCurrentFlowId()
    {
        synchronized (currentFlowId) {
            currentFlowId = null;
        }
    }

    public boolean finishedFollowingLastActivatedFlow()
    {
        synchronized (currentFlowId){
            return (currentFlowId!=null);
        }
    }

    @Override
    protected Boolean call() {
        while (appController != null) {

            synchronized (currentFlowId) {

                String finalUrl = HttpUrl
                        .parse(Constants.FULL_SERVER_PATH + "/get-progress")
                        .newBuilder()
                        .addQueryParameter("flowId", currentFlowId)
                        .build()
                        .toString();


                Response response = HttpClientUtil.runSync(finalUrl);

                if (response == null) {
                    System.out.println("problem with fetching flow progress");
                } else {
                    if (response.code() == 200 && response.body() != null) {
                        try {
                            FlowExecutionDTO flowExecutionDTO = Constants.GSON_INSTANCE
                                    .fromJson(response.body().string(), FlowExecutionDTO.class);

                            Platform.runLater(() -> appController.updateProgressFlow(flowExecutionDTO));

                        } catch (Exception e) {
                            System.out.println(e.getMessage());
                        }
                    } else
                        System.out.println("problem with fetching flow progress");

                    if (response.body() != null)
                        response.body().close();
                }

            }

            try {
                Thread.sleep(700);
            }
            catch (InterruptedException e) {}

        }
        return true;
    }
}
