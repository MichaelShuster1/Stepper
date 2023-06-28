package controllers;


import controllers.history.HistoryController;
import controllers.roles.RolesController;
import controllers.statistics.StatisticsController;
import controllers.users.UsersController;
import dto.FlowExecutionDTO;
import dto.InputsDTO;
import enginemanager.EngineApi;
import enginemanager.Manager;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import okhttp3.*;
import progress.ProgressTracker;

import java.io.File;

public class AppController {

    @FXML
    private StackPane historyComponent;

    @FXML
    private HistoryController historyComponentController;

    @FXML
    private StackPane statisticsComponent;

    @FXML
    private StatisticsController statisticsComponentController;

    @FXML
    private StackPane usersComponent;

    @FXML
    private UsersController usersComponentController;

    @FXML
    private StackPane rolesComponent;

    @FXML
    private RolesController rolesComponentController;

    @FXML
    private Button loadXML;

    @FXML
    private Label loadedXML;

    @FXML
    private TabPane tabPaneView;

    private EngineApi engine;

    private ProgressTracker progressTracker;

    private Stage primaryStage;

    private OkHttpClient okHttpClient;





    @FXML
    public void initialize() {
        historyComponentController.setAppController(this);
        statisticsComponentController.setAppController(this);
        usersComponentController.setAppController(this);
        rolesComponentController.setAppController(this);
        setTab(3);
        okHttpClient=new OkHttpClient();
    }


    public void setModel(Manager engine) {
        this.engine = engine;
        statisticsComponentController.setEngine(engine);
        historyComponentController.setEngine(engine);
        progressTracker=new ProgressTracker(this,engine);
        Thread thread=new Thread(progressTracker);
        thread.setDaemon(true);
        thread.start();
    }

    public void addFlowId(String id)
    {
        progressTracker.addFlowId(id);
    }

    public void setPrimaryStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
        historyComponentController.setStage(primaryStage);

        primaryStage.setOnCloseRequest(event -> {
            event.consume(); // Consume the event to prevent default close behavior

            // Show a confirmation dialog
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirmation");
            if(primaryStage.getScene().getStylesheets().size()!=0)
                alert.getDialogPane().getStylesheets().add(primaryStage.getScene().getStylesheets().get(0));

            if(progressTracker.areFlowsRunning())
                alert.setHeaderText("The are still flows running in the background!\nAre you sure you want to exit?");
            else
                alert.setHeaderText("Are you sure you want to exit?");
            alert.setContentText("Press OK to exit the application.\n");

            // Handle the user's choice
            alert.showAndWait().ifPresent(result -> {
                if (result == ButtonType.OK) {
                    primaryStage.close();
                    engine.endProcess();
                }
            });
        });
    }


    public InputsDTO getFlowInputs(int index)
    {
        return engine.getFlowInputs(index);
    }


    @FXML
    private void loadXMLFile(ActionEvent event) {
       File selectedFile = openFileChooserAndGetFile();
       if(selectedFile == null)
           return;

        String RESOURCE = "/upload-file";
        RequestBody body =
                new MultipartBody.Builder()
                        .addFormDataPart("xmlFile", selectedFile.getName(),
                                RequestBody.create(selectedFile, MediaType.parse("text/plain")))
                        .build();

        Request request = new Request.Builder()
                .url("http://localhost:8080/ServerApp" + RESOURCE)
                .post(body)
                .build();

        Call call = okHttpClient.newCall(request);



        try {
            Response response = call.execute();
            loadedXML.setText("Currently loaded file: " + selectedFile.getAbsolutePath());
            statisticsComponentController.createStatisticsTables();
        }
        catch (Exception ex)
        {
            Alert alert =new Alert(Alert.AlertType.ERROR);

            ObservableList<String> stylesheets = primaryStage.getScene().getStylesheets();
            if(stylesheets.size()!=0)
                alert.getDialogPane().getStylesheets().add(stylesheets.get(0));

            alert.setTitle("Error");
            alert.setContentText(ex.getMessage());
            alert.showAndWait();
        }
    }


    private File openFileChooserAndGetFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open File");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("XML Files", "*.xml"));
        File selectedFile = fileChooser.showOpenDialog(primaryStage);
        return  selectedFile;
    }


    public void streamFlow(String flowName) {
        progressTracker.resetCurrentFlowId();
        int index =engine.getFlowIndexByName(flowName);
        setTab(2);
    }


    /*
    public void updateProgressFlow(FlowExecutionDTO flowExecutionDTO)
    {
        executionComponentController.updateProgressFlow(flowExecutionDTO);
    }
    */



    public void setTab(int index)
    {
        tabPaneView.getSelectionModel().select(index);
    }


    public void updateStatistics() {
        statisticsComponentController.fillTablesData();
    }


    public void addRowInHistoryTable(FlowExecutionDTO flowExecutionDTO)
    {
        historyComponentController.addRow(flowExecutionDTO);
    }

    public Stage getPrimaryStage() {
        return primaryStage;
    }

    public void clearTabs() {
        historyComponentController.clearTab();
        statisticsComponentController.clearTab();
    }

}



