package controllers;

import controllers.chat.ChatAreaController;
import controllers.flowdefinition.DefinitionController;
import controllers.history.HistoryController;
import controllers.login.LoginController;
import dto.*;
import elementlogic.ElementLogic;
import controllers.flowexecution.ExecutionController;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
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
    private StackPane chatComponent;

    @FXML
    private ChatAreaController chatComponentController;

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

    private boolean isChatOpen;

    ListView<String> rolesListView;



    @FXML
    public void initialize() {
        initControllers();
        initStyleChoiceView();
        setTab(2);

        initRolesListView();
        initHyperLink();
        logout.setOnMouseClicked(e -> logOutClick());
        chatButton.setOnMouseClicked(e->displayChat());
        isChatOpen=false;

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
                if (newTab!=null){
                    String tabTitle =newTab.getText();
                    if(tabTitle.equals("Flows Definition")){
                        if(isChatOpen) {
                            chatComponentController.close();
                            closeChat();
                            isChatOpen=true;
                        }
                        chatButton.setVisible(false);
                        chatButton.setDisable(true);
                    }
                    else {
                        if (isChatOpen) {
                            chatComponentController.startListRefresher();
                            displayChat();
                        }
                        else
                        {
                            chatButton.setVisible(true);
                            chatButton.setDisable(false);
                        }
                    }

                }
            }
        });
    }

    private void initRolesListView() {
        rolesListView=new ListView<>();
        rolesListView.setOrientation(Orientation.VERTICAL);
    }

    private void initStyleChoiceView() {
        styleChoiceView.getItems().addAll(Styles.getStyles());
        styleChoiceView.setValue(Styles.DEFAULT.toString());
        styleChoiceView.setOnAction(e->setStyle());
    }

    private void initControllers() {
        executionComponentController.setAppController(this);
        executionComponentController.bindAnimationBooleanProperty(animationsRadioButtonVIew.selectedProperty());
        definitionComponentController.setAppController(this);
        historyComponentController.setAppController(this);
        chatComponentController.setAppController(this);
    }

    private void initHyperLink() {
        hyperlink.setOnMouseClicked(e -> {
            Platform.runLater(()->ElementLogic.showNewPopUp(rolesListView, primaryStage));
        });

        HBox.setMargin(hyperlink, new Insets(0, 0, 0, -10));
    }


    private void displayChat(){
        chatComponent.maxHeightProperty().bind(Bindings.multiply(primaryStage.heightProperty(), 0.35));
        chatComponent.maxWidthProperty().bind(Bindings.multiply(primaryStage.widthProperty(), 0.25));

        chatComponent.setVisible(true);
        chatComponent.setDisable(false);

        chatButton.setVisible(false);
        chatButton.setDisable(true);

        chatComponentController.startListRefresher();
        isChatOpen=true;
    }

    public void closeChat(){
        chatComponent.maxHeightProperty().unbind();
        chatComponent.maxWidthProperty().unbind();

        chatComponent.setMaxHeight(0);
        chatComponent.setMaxWidth(0);

        chatComponent.setVisible(false);
        chatComponent.setDisable(true);

        chatButton.setVisible(true);
        chatButton.setDisable(false);

        isChatOpen=false;
    }

    
    private void setRolesListView(Set<String> roles){
        int counter=1;
        String selected = null;
        if(rolesListView.getSelectionModel().getSelectedItem() != null) {
            selected = rolesListView.getSelectionModel().getSelectedItem();
            int dotIndex = selected.indexOf(".");
            if (dotIndex != -1)
              selected = selected.substring(dotIndex + 1);
        }
        rolesListView.getItems().clear();
        for(String role:roles)
        {
            rolesListView.getItems().add(counter+"."+role);
            if(selected != null) {
                if (selected.equals(role))
                    rolesListView.getSelectionModel().select(counter - 1);
            }
            counter++;
        }
    }
    

    private void sendLogoutRequest(Boolean switchToLogin, boolean shutDownHttp) {
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
                        shutDownMainScreen(shutDownHttp);
                        HttpClientUtil.removeCookiesOf(Constants.BASE_DOMAIN);
                        if(switchToLogin) {
                            try {switchToLogin();}
                            catch (Exception e) {}
                        }
                    });
                }
                else
                    HttpClientUtil.errorMessage(response.body(), AppController.this);

                if(response.body()!=null)
                    response.body().close();
            }
        });

    }

    private void logOutClick(){
       sendLogoutRequest(true,false);
       progressTracker.resetCurrentFlowId();
       progressTracker=null;
    }


    public boolean canRunFlow(String flowName){
        return definitionComponentController.canRunFlow(flowName);
    }

    public void checkIfCurrentFlowValidInExecution(){
        String flowName=executionComponentController.getFlowName();
        if(flowName!=null &&!canRunFlow(flowName))
        {
            executionComponentController.clearTab();
            progressTracker.resetCurrentFlowId();
            if(tabPaneView.getSelectionModel().getSelectedIndex()==1) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Message");
                if(primaryStage.getScene().getStylesheets().size()!=0)
                    alert.getDialogPane().getStylesheets().add(primaryStage.getScene().getStylesheets().get(0));
                alert.setContentText("The permission to run the flow: "+flowName+"\n Has been taken away from you by the admin. \n If the flow was in progress," +
                        " you will be able to see its execution details in the history tab once the flow is finished.");
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
                    sendLogoutRequest(false,true);
                    progressTracker.resetCurrentFlowId();
                    progressTracker=null;
                }
            });
        });
    }

    private void shutDownMainScreen(boolean shutDownClient) {
        stopUpdatesRefresher();
        primaryStage.close();
        if(shutDownClient)
            HttpClientUtil.shutdown();
        chatComponentController.close();
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
                    setRolesListView(roles);
                }
                else {
                    resetRolesListView();
                    hyperlink.setDisable(true);
                    hyperlink.setVisible(false);
                }
            }
        });
    }

    private void resetRolesListView() {
        rolesListView.getItems().clear();
        rolesListView.setPlaceholder(new Label("No roles assigned"));
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

    public void validateRerunButton() {
        String flowName = historyComponentController.getSelectedFlowName();
        if(flowName!=null &&!canRunFlow(flowName))
            historyComponentController.setRerunButtonDisable(true);
        else if(flowName != null && canRunFlow(flowName))
            historyComponentController.setRerunButtonDisable(false);
    }
}



