package controllers;

import controllers.flowdefinition.DefinitionController;
import controllers.history.HistoryController;
import dto.FlowDefinitionDTO;
import dto.FlowExecutionDTO;
import dto.InputsDTO;
import enginemanager.EngineApi;
import enginemanager.Manager;
import controllers.flowexecution.ExecutionController;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;
import progress.ProgressTracker;
import styles.Styles;
import utils.Constants;
import utils.HttpClientUtil;

import java.io.File;
import java.io.IOException;

public class AppController {

    @FXML
    private StackPane definitionComponent;

    @FXML
    private DefinitionController definitionComponentController;

    @FXML
    private StackPane executionComponent;

    @FXML
    private ExecutionController executionComponentController;

    @FXML
    private StackPane historyComponent;

    @FXML
    private HistoryController historyComponentController;



    @FXML
    private TabPane tabPaneView;

    @FXML
    private Tab executionTabView;
    @FXML
    private ChoiceBox<String> styleChoiceView;

    @FXML
    private ImageView stepperLogo;

    @FXML
    private RadioButton animationsRadioButtonVIew;

    @FXML
    private Label userName;

    @FXML
    private Label isManager;

    @FXML
    private Label userRoles;

    private EngineApi engine;

    private ProgressTracker progressTracker;

    private Stage primaryStage;

    private boolean tabClicked;





    @FXML
    public void initialize() {
        executionComponentController.setAppController(this);
        executionComponentController.bindAnimationBooleanProperty(animationsRadioButtonVIew.selectedProperty());
        definitionComponentController.setAppController(this);
        historyComponentController.setAppController(this);
        styleChoiceView.getItems().addAll(Styles.getStyles());
        styleChoiceView.setValue(Styles.DEFAULT.toString());
        styleChoiceView.setOnAction(e->setStyle());
        setTab(2);
        tabClicked=true;


        tabPaneView.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Tab>() {
            @Override
            public void changed(ObservableValue<? extends Tab> observable, Tab oldTab, Tab newTab) {
                if (newTab != null) {
                    String tabTitle = newTab.getText();
                    if(tabTitle.equals("Flows Execution")&&tabClicked) {
                        if(progressTracker.finishedFollowingLastActivatedFlow())
                            executionComponentController.clearTab();
                    }
                }
                tabClicked =true;
            }
        });
    }


    private void setStyle() {
        String choice= styleChoiceView.getValue();
        primaryStage.getScene().getStylesheets().clear();
        switch (Styles.valueOf(choice))
        {
            case DARK:
                primaryStage.getScene().getStylesheets().add(
                        getClass().getResource("/resources/css/Dark.css").toExternalForm());
                stepperLogo.setImage(new Image(getClass().getResource("/resources/pictures/blackLogo.png").toExternalForm()));
                break;
            case MIDNIGHT:
                primaryStage.getScene().getStylesheets().add(
                        getClass().getResource("/resources/css/Midnight.css").toExternalForm());
                stepperLogo.setImage(new Image(getClass().getResource("/resources/pictures/purpleLogo.png").toExternalForm()));
                break;
            case DEFAULT:
                primaryStage.getScene().getStylesheets().add(
                        getClass().getResource("/resources/css/Default.css").toExternalForm());
                stepperLogo.setImage(new Image(getClass().getResource("/resources/pictures/blackLogo.png").toExternalForm()));
                break;
        }
    }


    public void setModel(Manager engine) {
        this.engine = engine;
        executionComponentController.setEngine(engine);
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
        executionComponentController.setStage(primaryStage);
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
                    definitionComponentController.StopFlowRefresher();
                    primaryStage.close();
                    HttpClientUtil.shutdown();
                    engine.endProcess();
                }
            });
        });
    }



    public void streamFlow(String flowName) {
        progressTracker.resetCurrentFlowId();
        //int index =engine.getFlowIndexByName(flowName);
        String finalUrl = HttpUrl
                .parse(Constants.FULL_SERVER_PATH + "/get-inputs")
                .newBuilder()
                .addQueryParameter("flowName", flowName)
                .build()
                .toString();

        HttpClientUtil.runAsync(finalUrl, new Callback() {

            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
            }
            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if (response.code() != 200) {
                } else {
                    if (response.body() != null) {
                        String jsonInputs = response.body().string();
                        InputsDTO inputsDTO = Constants.GSON_INSTANCE.fromJson(jsonInputs, InputsDTO.class);
                        Platform.runLater(() -> {
                            executionComponentController.setTabView(inputsDTO,flowName);

                        });
                        tabClicked=false;
                        setTab(1);
                    }
                }

            }
        });
    }



    public void updateProgressFlow(FlowExecutionDTO flowExecutionDTO)
    {
        executionComponentController.updateProgressFlow(flowExecutionDTO);
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

    public void clearTabs() {
        definitionComponentController.clearTab();
        executionComponentController.clearTab();
        historyComponentController.clearTab();
    }

    public void setUserName(String name) {
        userName.setText(userName.getText() + " " + name);
    }



    public void setFlowRefreshActive() {
        definitionComponentController.startFlowRefresher();
    }

}



