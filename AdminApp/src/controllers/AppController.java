package controllers;


import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import controllers.history.HistoryController;
import controllers.roles.RolesController;
import controllers.statistics.StatisticsController;
import controllers.users.UsersController;
import dto.FlowExecutionDTO;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import okhttp3.*;
import utils.Constants;
import utils.HttpClientUtil;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.*;
import java.util.Timer;

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

    private Stage primaryStage;

    private Timer timer;



    @FXML
    public void initialize() {
        initUsersController();
        initRolesController();
        historyComponentController.setAppController(this);
        statisticsComponentController.setAppController(this);
        setTab(3);
        StartUpdatesRefresher();


        tabPaneView.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Tab>() {
            @Override
            public void changed(ObservableValue<? extends Tab> observable, Tab oldTab, Tab newTab) {
                if(newTab!=null){
                    String tabTitle =newTab.getText();
                    if(tabTitle.equals("Roles Management") ){
                        rolesComponentController.rowSelect(true);
                    }
                }
            }
        });
    }

    private void initUsersController(){
        usersComponentController.setAppController(this);
        usersComponentController.StartUsersRefresher();
    }

    private void initRolesController(){
        rolesComponentController.setAppController(this);
        rolesComponentController.setRoleAdder(usersComponentController::addRole);
        rolesComponentController.setRoleDeleter(usersComponentController::removeRole);
    }


    public void StartUpdatesRefresher()
    {
        TimerTask updatesRefresher=new UpdatesRefresher(historyComponentController::updateHistoryTable,
                statisticsComponentController::fillTablesData, this,historyComponentController.getHistoryVersion());
        timer = new Timer();
        timer.schedule(updatesRefresher, 1000, 1000);
    }

    public void StopUpdatesRefresher()
    {
        timer.cancel();
    }


    public Integer CurrentTab(){
        if(!tabPaneView.getSelectionModel().isEmpty())
         return tabPaneView.getSelectionModel().getSelectedIndex();
        return null;
    }


    public void setPrimaryStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
        historyComponentController.setStage(primaryStage);
        usersComponentController.setStage(primaryStage);

        final String RESOURCE ="/admin";

        String finalUrl = HttpUrl
                .parse(Constants.FULL_SERVER_PATH + RESOURCE)
                .newBuilder()
                .build()
                .toString();

        HttpClientUtil.runAsync(finalUrl,new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

                HttpClientUtil.showErrorAlert(Constants.CONNECTION_ERROR,AppController.this);
                Platform.runLater(primaryStage::close);
                HttpClientUtil.shutdown();
                usersComponentController.StopUsersRefresher();
                timer.cancel();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if(response.code()==200){
                    JsonArray jsonArray = JsonParser.parseString(response.body().string()).getAsJsonArray();
                    Type setType = new TypeToken<Set<String>>() {}.getType();
                    Set<String> flowsNames,rolesNames;
                    if(!jsonArray.get(0).isJsonNull()) {
                       flowsNames = Constants.GSON_INSTANCE
                                .fromJson(jsonArray.get(0).getAsString(), setType);
                       rolesComponentController.updateFlows(flowsNames);
                    }
                    if(!jsonArray.get(1).isJsonNull()){
                        rolesNames=Constants.GSON_INSTANCE
                                .fromJson(jsonArray.get(1).getAsString(),setType);
                        usersComponentController.setRolesOptions(rolesNames);
                        rolesComponentController.setRolesListView(rolesNames);
                    }
                }
                else {
                        Platform.runLater(primaryStage::close);
                        HttpClientUtil.errorMessage(response.body(), AppController.this);
                        HttpClientUtil.shutdown();
                        usersComponentController.StopUsersRefresher();
                        timer.cancel();
                }
                if(response.body()!=null)
                    response.body().close();
            }
        });

        primaryStage.setOnCloseRequest(event -> {
            event.consume(); // Consume the event to prevent default close behavior


            // Show a confirmation dialog
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirmation");
            if(primaryStage.getScene().getStylesheets().size()!=0)
                alert.getDialogPane().getStylesheets().add(primaryStage.getScene().getStylesheets().get(0));


            alert.setHeaderText("Are you sure you want to exit?");
            alert.setContentText("Press OK to exit the application.\n");

            // Handle the user's choice
            alert.showAndWait().ifPresent(result -> {
                if (result == ButtonType.OK) {

                    usersComponentController.StopUsersRefresher();
                    timer.cancel();

                    String url = HttpUrl
                            .parse(Constants.FULL_SERVER_PATH + RESOURCE)
                            .newBuilder()
                            .build()
                            .toString();

                    HttpClientUtil.runAsyncDelete(url,new Callback() {
                        @Override
                        public void onFailure(Call call, IOException e) {
                            //HttpClientUtil.showErrorAlert(Constants.CONNECTION_ERROR,AppController.this);
                        }
                        @Override
                        public void onResponse(Call call, Response response) throws IOException {
                            if(response.body()!=null)
                                response.body().close();
                        }
                    });

                    HttpClientUtil.shutdown();
                    primaryStage.close();
                }
            });
        });
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

        String finalUrl = HttpUrl
                .parse(Constants.FULL_SERVER_PATH + RESOURCE)
                .newBuilder()
                .build()
                .toString();

        HttpClientUtil.runAsyncPost(finalUrl,body,new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                    HttpClientUtil.showErrorAlert(Constants.CONNECTION_ERROR,AppController.this);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if(response.code()==200) {
                    String json=response.body().string();
                    Platform.runLater(()->{
                        loadedXML.setText("Currently loaded file: " + selectedFile.getAbsolutePath());
                        showInfoAlert();
                        Type setType = new TypeToken<Set<String>>() {}.getType();
                        Set<String> newFlows=Constants.GSON_INSTANCE.fromJson(json,setType);
                        rolesComponentController.updateFlows(newFlows);
                    });
                }
                else
                    HttpClientUtil.errorMessage(response.body(),AppController.this);

                if(response.body()!=null)
                    response.body().close();
            }
        });
    }


    private File openFileChooserAndGetFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open File");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("XML Files", "*.xml"));
        File selectedFile = fileChooser.showOpenDialog(primaryStage);
        return  selectedFile;
    }

    private void showInfoAlert(){
        Alert alert =new Alert(Alert.AlertType.INFORMATION);
        ObservableList<String> stylesheets = primaryStage.getScene().getStylesheets();
        if(stylesheets.size()!=0)
            alert.getDialogPane().getStylesheets().add(stylesheets.get(0));
        alert.setTitle("Message");
        alert.setContentText("the xml file was loaded successfully");
        alert.showAndWait();
    }





    public void setTab(int index)
    {
        tabPaneView.getSelectionModel().select(index);
    }


    public void addRowInHistoryTable(FlowExecutionDTO flowExecutionDTO)
    {
        historyComponentController.addRow(flowExecutionDTO);
    }

    public Stage getPrimaryStage() {
        return primaryStage;
    }

    public Integer getHistoryVersion() {
        return historyComponentController.getHistoryVersion();
    }
}



