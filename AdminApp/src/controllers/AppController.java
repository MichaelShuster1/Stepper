package controllers;


import controllers.history.HistoryController;
import controllers.roles.RolesController;
import controllers.statistics.StatisticsController;
import controllers.users.UsersController;
import dto.FlowExecutionDTO;
import dto.InputsDTO;
import enginemanager.EngineApi;
import enginemanager.Manager;
import javafx.application.Platform;
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
import utils.Constants;

import java.io.File;
import java.io.IOException;

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

    private ProgressTracker progressTracker;

    private Stage primaryStage;






    @FXML
    public void initialize() {
        historyComponentController.setAppController(this);
        statisticsComponentController.setAppController(this);
        usersComponentController.setAppController(this);
        rolesComponentController.setAppController(this);
        setTab(3);

        usersComponentController.StartUsersRefresher();
    }


    public void setPrimaryStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
        historyComponentController.setStage(primaryStage);

        final String RESOURCE ="/admin";
        Request request = new Request.Builder()
                .url(Constants.FULL_SERVER_PATH+RESOURCE)
                .build();

        Constants.HTTP_CLIENT.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

                Platform.runLater(()->{
                    Alert alert =new Alert(Alert.AlertType.INFORMATION);
                    ObservableList<String> stylesheets = primaryStage.getScene().getStylesheets();
                    if(stylesheets.size()!=0)
                        alert.getDialogPane().getStylesheets().add(stylesheets.get(0));

                    alert.setTitle("Message");
                    alert.setContentText("The server is closed");
                    alert.showAndWait();
                    primaryStage.close();
                });
                Constants.HTTP_CLIENT.dispatcher().executorService().shutdown();
                Constants.HTTP_CLIENT.connectionPool().evictAll();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if(response.code()==401)
                {
                    Platform.runLater(primaryStage::close);
                    Constants.HTTP_CLIENT.dispatcher().executorService().shutdown();
                    Constants.HTTP_CLIENT.connectionPool().evictAll();
                }
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

                    Request closeRequest = new Request.Builder()
                            .url(Constants.FULL_SERVER_PATH+RESOURCE)
                            .delete()
                            .build();

                    Constants.HTTP_CLIENT.newCall(closeRequest).enqueue(new Callback() {
                        @Override
                        public void onFailure(Call call, IOException e) {}
                        @Override
                        public void onResponse(Call call, Response response) throws IOException {}
                    });

                    Constants.HTTP_CLIENT.dispatcher().executorService().shutdown();
                    Constants.HTTP_CLIENT.connectionPool().evictAll();
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

        Request request = new Request.Builder()
                .url(Constants.FULL_SERVER_PATH + RESOURCE)
                .post(body)
                .build();

        Constants.HTTP_CLIENT.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if(response.code()==200) {

                    Platform.runLater(() ->
                            loadedXML.setText("Currently loaded file: " + selectedFile.getAbsolutePath()));

                    Platform.runLater(()->{
                        Alert alert =new Alert(Alert.AlertType.INFORMATION);
                        ObservableList<String> stylesheets = primaryStage.getScene().getStylesheets();
                        if(stylesheets.size()!=0)
                            alert.getDialogPane().getStylesheets().add(stylesheets.get(0));

                        alert.setTitle("Message");
                        alert.setContentText("the xml file was loaded successfully");
                        alert.showAndWait();

                    });
                }

                if(response.code()==400){
                    Platform.runLater(()->{
                        Alert alert =new Alert(Alert.AlertType.ERROR);

                        ObservableList<String> stylesheets = primaryStage.getScene().getStylesheets();
                        if(stylesheets.size()!=0)
                            alert.getDialogPane().getStylesheets().add(stylesheets.get(0));

                        alert.setTitle("Error");
                        try {
                            alert.setContentText(response.body().string());
                        } catch (IOException e) {
                            alert.setContentText("the xml file was not loaded successfully!");
                        }
                        alert.showAndWait();
                    });
                }


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



