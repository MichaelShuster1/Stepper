package progress;

import controllers.AppController;
import dto.*;
import javafx.application.Platform;
import javafx.concurrent.Task;
import okhttp3.HttpUrl;
import okhttp3.Response;
import utils.*;

public class ProgressTracker extends Task<Boolean> {

    String currentFlowId;

    AppController appController;


    private final Object progressTrackerLock = new Object();

    public ProgressTracker(AppController appController)
    {
        this.appController=appController;
    }

    public void setFlowId(String id)
    {
        synchronized (progressTrackerLock) {
            currentFlowId = id;
        }
    }

    public void resetCurrentFlowId()
    {
        synchronized (progressTrackerLock) {
            currentFlowId = null;
        }
    }

    public boolean finishedFollowingLastActivatedFlow()
    {
        synchronized (progressTrackerLock){
            return (currentFlowId==null);
        }
    }

    @Override
    protected Boolean call() {
        while (appController != null) {

            synchronized (progressTrackerLock) {

                if(currentFlowId!=null){
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

                                if(flowExecutionDTO.getStateAfterRun()!=null)
                                    currentFlowId=null;

                            } catch (Exception e) {
                                System.out.println(e.getMessage());
                            }
                        } else
                            System.out.println("problem with fetching flow progress");

                        if (response.body() != null)
                            response.body().close();
                    }
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
