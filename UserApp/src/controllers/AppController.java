package controllers;

import controllers.chat.ChatAreaController;
import controllers.flowdefinition.DefinitionController;
import controllers.history.HistoryController;
import controllers.login.LoginController;
import dto.*;
import elementlogic.ElementLogic;
import enginemanager.EngineApi;
import enginemanager.Manager;
import controllers.flowexecution.ExecutionController;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Screen;
import javafx.stage.Stage;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;
import progress.ProgressTracker;
import styles.Styles;
import utils.Constants;
import utils.HttpClientUtil;


import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.function.Predicate;

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

    @FXML
    private Hyperlink hyperlink;

    @FXML
    private Button logout;

    @FXML
    private Button chatButton;

    private ProgressTracker progressTracker;

    private Stage primaryStage;

    private Timer timer;



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

        logout.setOnMouseClicked(e -> logOutClick());
        chatButton.setOnMouseClicked(e->openChatRoom());

        tabPaneView.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Tab>() {
            @Override
            public void changed(ObservableValue<? extends Tab> observable, Tab oldTab, Tab newTab) {
                if(oldTab!=null){
                    String tabTitle =oldTab.getText();
                    if(tabTitle.equals("Flows Execution") && executionComponentController.isAfterRun()){
                        if(progressTracker.finishedFollowingLastActivatedFlow())
                            executionComponentController.clearTab();
                    }
                }
            }
        });
    }


    private void openChatRoom(){
        try {
            // Load the pop-up FXML file
            FXMLLoader popupLoader = new FXMLLoader(getClass().getResource("/resources/fxml/chat-area.fxml"));
            GridPane popupRoot = popupLoader.load();

            ChatAreaController chatAreaController=popupLoader.getController();

            // Create a new stage for the pop-up window
            Stage popupStage = new Stage();
            popupStage.setTitle("Chat Room");
            popupStage.initModality(Modality.APPLICATION_MODAL);
            popupStage.initOwner(primaryStage);

            // Set the pop-up content
            Scene popupScene = new Scene(popupRoot);
            popupStage.setScene(popupScene);

            // Show the pop-up window
            popupStage.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void logOutClick(){
        String finalUrl = HttpUrl
                .parse(Constants.FULL_SERVER_PATH + "/logout")
                .newBuilder()
                .build()
                .toString();

        HttpClientUtil.runAsync(finalUrl, new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                HttpClientUtil.showErrorAlert(Constants.CONNECTION_ERROR, AppController.this);
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if(response.code()==200){
                    Platform.runLater(() -> {
                        shutDownMainScreen(false);
                        HttpClientUtil.removeCookiesOf(Constants.BASE_DOMAIN);
                        try {switchToLogin();}
                        catch (Exception e) {}
                    });
                }
                else
                    HttpClientUtil.errorMessage(response.body(), AppController.this);

                if(response.body()!=null)
                    response.body().close();
            }
        });
    }


    public boolean canRunFlow(String flowName){
        return definitionComponentController.canRunFlow(flowName);
    }

    public void checkIfCurrentFlowValidInExecution(){
        String flowName=executionComponentController.getFlowName();
        if(flowName!=null &&!canRunFlow(flowName))
        {
            executionComponentController.clearTab();
            if(tabPaneView.getSelectionModel().getSelectedIndex()==1) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Message");
                if(primaryStage.getScene().getStylesheets().size()!=0)
                    alert.getDialogPane().getStylesheets().add(primaryStage.getScene().getStylesheets().get(0));
                alert.setContentText("The permission to run the flow: "+flowName+"\n Has been taken away from you by the admin");
                alert.showAndWait();
            }
        }
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


    public void setModel() {
        progressTracker=new ProgressTracker(this);
        Thread thread=new Thread(progressTracker);
        thread.setDaemon(true);
        thread.start();
    }

    public void setFlowId(String id)
    {
        progressTracker.setFlowId(id);
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



            alert.setHeaderText("Are you sure you want to exit?");
            alert.setContentText("Press OK to exit the application.\n");

            // Handle the user's choice
            alert.showAndWait().ifPresent(result -> {
                if (result == ButtonType.OK) {
                    //definitionComponentController.StopFlowRefresher();
                    shutDownMainScreen(true);
                }
            });
        });
    }

    private void shutDownMainScreen(boolean shutDownClient) {
        stopUpdatesRefresher();
        primaryStage.close();
        if(shutDownClient)
            HttpClientUtil.shutdown();
    }



    public void streamFlow(String flowName) {
        progressTracker.resetCurrentFlowId();
        String finalUrl = HttpUrl
                .parse(Constants.FULL_SERVER_PATH + "/get-inputs")
                .newBuilder()
                .addQueryParameter("flowName", flowName)
                .build()
                .toString();

        HttpClientUtil.runAsync(finalUrl, new Callback() {

            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                HttpClientUtil.showErrorAlert(Constants.CONNECTION_ERROR, AppController.this);
            }
            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if (response.code() != 200) {
                    HttpClientUtil.errorMessage(response.body(), AppController.this);
                } else {
                    if (response.body() != null) {
                        String jsonInputs = response.body().string();
                        InputsDTO inputsDTO = Constants.GSON_INSTANCE.fromJson(jsonInputs, InputsDTO.class);
                        Platform.runLater(() -> {
                            executionComponentController.setTabView(inputsDTO,flowName);
                            setTab(1);
                        });
                    }
                }
                if(response.body() != null)
                    response.body().close();
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

    public void updateUserInfo(UserInfoDTO userInfoDTO){
        UserDetailsDTO userDetails=userInfoDTO.getUserDetailsDTO();
        Platform.runLater(()->{

            if(userDetails.getManager()!=null){

                if (userDetails.getManager()) {
                    isManager.setText("Is Manager: Yes");
                }
                else {
                    isManager.setText("Is Manager: No");
                }
            }

            if(userInfoDTO.getRoles()!=null){
                Set<String> roles=userInfoDTO.getRoles();
                if(roles.size()!=0)
                {
                    hyperlink.setDisable(false);
                    hyperlink.setVisible(true);
                    ListView<String> listView = ElementLogic.createListView(Arrays.asList(roles.toArray()));
                    hyperlink.setOnMouseClicked(e -> ElementLogic.showNewPopUp(listView, primaryStage));
                    HBox.setMargin(hyperlink, new Insets(0, 0, 0, -10));
                }
                else {
                    hyperlink.setDisable(true);
                    hyperlink.setVisible(false);
                }


                /*
                userDetailsView.getChildren().remove(hyperlink);
                String text = roles.toString()
                        .replace("[", " ")
                        .replace("]", " ");
                userRoles.setText("Assigned Roles: " + text);
                */
            }
        });
    }


    public void startUpdatesRefresher(){
        UpdatesRefresher updatesRefresher = new UpdatesRefresher(definitionComponentController::fillTableData,
                this::updateUserInfo, historyComponentController::setHistoryTable, historyComponentController::addRows);
        timer = new Timer();
        timer.schedule(updatesRefresher, 500, 2000);
    }

    public void stopUpdatesRefresher(){
        timer.cancel();
    }


    public void switchToLogin() throws IOException {

        URL resource =getClass().getResource("/resources/fxml/Login.fxml");
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(resource);

        Parent root = loader.load(resource.openStream());
        LoginController controller = loader.getController();
        controller.setPrimaryStage(primaryStage);

        Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
        double widthFraction = 0.3;
        double heightFraction = 0.4;
        double desiredWidth = screenBounds.getWidth() * widthFraction;
        double desiredHeight = screenBounds.getHeight() * heightFraction;

        Scene scene = new Scene(root,desiredWidth,desiredHeight);
        Image icon = new Image(getClass().getResource("/resources/pictures/Icon.png").toExternalForm());
        scene.getStylesheets().add(getClass().getResource("/resources/css/Default.css").toExternalForm());
        primaryStage.getIcons().add(icon);
        primaryStage.setTitle("Stepper");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

}



