package progress;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import controllers.AppController;
import dto.AvailableFlowDTO;
import dto.DataDefintionDTO;
import dto.FlowExecutionDTO;
import dto.StepExtensionDTO;
import enginemanager.EngineApi;
import javafx.application.Platform;
import javafx.concurrent.Task;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;
import utils.*;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ProgressTracker extends Task<Boolean> {

    final List<String> flowsId;

    String currentFlowId;

    AppController appController;

    EngineApi engine;

    public ProgressTracker(AppController appController,EngineApi engine)
    {
        flowsId=new ArrayList<>();
        this.appController=appController;
        this.engine=engine;
    }

    public void addFlowId(String id)
    {
        synchronized (flowsId) {
            flowsId.add(id);
            currentFlowId = id;
        }
    }

    public void resetCurrentFlowId()
    {
        synchronized (flowsId) {
            currentFlowId = null;
        }
    }

    public boolean finishedFollowingLastActivatedFlow()
    {
        synchronized (flowsId){
            return (!flowsId.contains(currentFlowId) && currentFlowId!=null);
        }
    }

    public boolean areFlowsRunning()
    {
        synchronized (flowsId){
            return (flowsId.size()!=0);
        }
    }

    @Override
    protected Boolean call()  {
        while (appController!=null)
        {
            synchronized (flowsId) {
                for (int i = 0;i<flowsId.size();i++) {
                    String flowId = flowsId.get(i);
                    //FlowExecutionDTO flowExecutionDTO=engine.getHistoryDataOfFlow(flowId);

                    String finalUrl = HttpUrl
                            .parse(Constants.FULL_SERVER_PATH + "/get-history")
                            .newBuilder()
                            .addQueryParameter("flowId", flowId)
                            .build()
                            .toString();

                    int index = i;
                    HttpClientUtil.runAsync(finalUrl, new Callback() {
                        @Override
                        public void onFailure(@NotNull Call call, @NotNull IOException e) {
                            System.out.println("problem with fetching flow history");
                        }

                        @Override
                        public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                            if(response.code()==200&&response.body()!=null){

                                 Gson gson = new GsonBuilder()
                                         .registerTypeAdapter(DataDefintionDTO.class, new DataDefintionDTODeserializer())
                                         .registerTypeAdapter(StepExtensionDTO.class, new StepExtensionDTODeserializer())
                                        .registerTypeAdapter(FlowExecutionDTO.class, new FlowExecutionDTODeserializer())
                                        .serializeNulls()
                                         .setPrettyPrinting()
                                        .create();

                                System.out.println(response.body());
                                FlowExecutionDTO flowExecutionDTO =gson
                                        .fromJson(response.body().string(), FlowExecutionDTO.class);
                                System.out.println(flowExecutionDTO);
                                if(flowId.equals(currentFlowId)) {
                                    Platform.runLater(()->appController.updateProgressFlow(flowExecutionDTO));
                                }


                                if(flowExecutionDTO.getStateAfterRun() != null) {
                                    Platform.runLater(()->appController.addRowInHistoryTable(flowExecutionDTO));
                                    flowsId.remove(index);
                                }
                            }
                            else
                                System.out.println("problem with fetching flow history");
                        }
                    });

                    /*
                    if(flowId.equals(currentFlowId)) {
                        Platform.runLater(()->appController.updateProgressFlow(flowExecutionDTO));
                    }


                    if(flowExecutionDTO.getStateAfterRun() != null) {
                        Platform.runLater(()->appController.addRowInHistoryTable(flowExecutionDTO));
                        flowsId.remove(i);
                    }
                    */
                }
            }
            try {
                Thread.sleep(700);
            } catch (InterruptedException e) {
            }
        }
        return true;
    }
}
