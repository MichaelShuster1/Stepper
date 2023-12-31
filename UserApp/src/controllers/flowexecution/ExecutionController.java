package controllers.flowexecution;


import controllers.AppController;
import dto.*;
import elementlogic.ElementLogic;
import enums.DataType;
import javafx.animation.FadeTransition;
import javafx.animation.RotateTransition;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.transform.Rotate;
import javafx.stage.*;
import javafx.util.Duration;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;
import utils.Constants;
import utils.HttpClientUtil;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


public class ExecutionController {

    @FXML
    private FlowPane mandatoryInputsView;

    @FXML
    private FlowPane optionalInputsView;

    @FXML
    private Button executeButton;

    @FXML
    private Button flowInfoView;

    @FXML
    private ChoiceBox<String> choiceBoxView;

    @FXML
    private VBox elementDetailsView;

    @FXML
    private VBox elementChoiceView;

    @FXML
    private ProgressBar progressBarView;

    @FXML
    private Button continuationButton;
    @FXML
    private HBox hBoxView;

    private AppController appController;


    private List<Button> mandatoryInputButtons;

    private List<Button> optionalInputButtons;

    private Button rerunButton;

    private ElementLogic elementLogic;

    private BooleanProperty isAnimationsOn;

    private boolean isClicked;

    private boolean afterRun;

    private String flowName;



    @FXML
    public void initialize() {
        continuationButton.setDisable(true);
        continuationButton.disableProperty().bind(choiceBoxView.valueProperty().isNull());
        choiceBoxView.setDisable(true);
        isClicked = false;
        afterRun=false;
        rerunButton=new Button("Rerun flow");
        flowName=null;
        HBox.setMargin(rerunButton,new Insets(0,10,0,0));
    }

    public boolean isAfterRun() {
        return afterRun;
    }

    public String getFlowName() {
        return flowName;
    }

    public void setAppController(AppController appController)
    {
        this.appController=appController;
    }

    public void setStage(Stage stage)
    {
        elementLogic=new ElementLogic(elementChoiceView,elementDetailsView,stage);
        elementLogic.setTableOpacity(0.0);
        choiceBoxView.setOpacity(0.0);
    }

    public void bindAnimationBooleanProperty(BooleanProperty booleanProperty)
    {
        isAnimationsOn=new SimpleBooleanProperty();
        isAnimationsOn.bind(booleanProperty);
    }


    public void setTabView(InputsDTO inputsDTO,String flowName)
    {
        clearTab();
        int numberOfMandatoryInputs=0;
        int numberOfInsertedMandatoryInputs=0;

        mandatoryInputButtons=new ArrayList<>();
        optionalInputButtons=new ArrayList<>();

        for(int i=0;i<inputsDTO.getNumberOfInputs();i++)
        {
            InputData inputData=inputsDTO.getFreeInput(i);
            Button button=new Button();
            button.setId(inputData.getSystemName());


            button.setOnMouseClicked(event -> {
                if(event.getButton()==MouseButton.PRIMARY)
                    inputClick(button,new ActionEvent());
                else if (event.getButton()==MouseButton.SECONDARY) {
                    rightInputClick(button);
                }
            });

            String inputPresentation =inputData.getSystemName().replace("_"," ").toLowerCase();
            inputPresentation+="\nDescription: "+inputData.getUserString();
            button.setText(inputPresentation);
            button.setDisable(false);
            FlowPane.setMargin(button,new Insets(0,10,10,0));


            if(inputData.getNecessity())
            {
                button.setStyle("-fx-background-color: #ff0000; ");
                mandatoryInputsView.getChildren().add(button);
                mandatoryInputButtons.add(button);
                numberOfMandatoryInputs++;
            }
            else {
                optionalInputsView.getChildren().add(button);
                optionalInputButtons.add(button);
            }

            if (inputData.IsInserted()) {
                button.setStyle("-fx-background-color: #40ff00; ");
                if(inputData.getNecessity())
                    numberOfInsertedMandatoryInputs++;
            }
        }
        executeButton.setDisable(numberOfInsertedMandatoryInputs != numberOfMandatoryInputs);
        flowInfoView.setText(flowName+" Flow info");
        this.flowName=flowName;
    }

    public void clearTab() {
        clearInputButtons();
        clearExecutionUpdate();
        choiceBoxView.setOpacity(0.0);
        elementLogic.setTableOpacity(0.0);
        isClicked = false;
        afterRun=false;
        flowInfoView.setText("Flow info");
        if(hBoxView.getChildren().contains(rerunButton)){
            hBoxView.getChildren().remove(rerunButton);
            hBoxView.getChildren().add(executeButton);
        }
        flowName=null;
        executeButton.setDisable(true);
    }

    public void clearInputButtons(){
        mandatoryInputsView.getChildren().clear();
        optionalInputsView.getChildren().clear();
    }

    public void clearExecutionUpdate()
    {
        choiceBoxView.getItems().clear();
        elementLogic.clear();
        progressBarView.setProgress(0);
        choiceBoxView.setDisable(true);
    }



    @FXML
    public void inputClick(Button button,ActionEvent event)
    {
        String finalUrl = HttpUrl
                .parse(Constants.FULL_SERVER_PATH + "/input-parameters")
                .newBuilder()
                .addQueryParameter("inputName", button.getId())
                .build()
                .toString();

        HttpClientUtil.runAsync(finalUrl, new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                HttpClientUtil.showErrorAlert(Constants.CONNECTION_ERROR, appController);
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if(response.code()==200&&response.body()!=null){
                    FreeInputExecutionDTO freeInputExecutionDTO =Constants
                            .GSON_INSTANCE
                            .fromJson(response.body().string(),FreeInputExecutionDTO.class);

                    Platform.runLater(()->{
                        Optional<String> result =getInputFromUser(freeInputExecutionDTO);
                        if(result.isPresent())
                        {
                            String data= result.get();
                            processInput(button, data);
                        }
                    });

                }
                else
                    HttpClientUtil.errorMessage(response.body(), appController);

                if(response.body()!=null)
                    response.body().close();
            }
        });
    }

    private Optional<String> getInputFromUser(FreeInputExecutionDTO freeInputExecutionDTO){

        Optional<String> result=Optional.empty();
        String inputType=freeInputExecutionDTO.getType();
        TextInputDialog inputDialog =getNewTextInputDialog();

        switch (DataType.valueOf(inputType.toUpperCase()))
        {
            case ENUMERATOR:
                ChoiceBox<String> enumerationSetChoice = new ChoiceBox<>();
                enumerationSetChoice.getItems().addAll(freeInputExecutionDTO.getAllowedValues());
                enumerationSetChoice.setStyle("-fx-pref-width: 200px;");

                HBox hbox = new HBox(10, new Label("Please select an option:"),  enumerationSetChoice);
                hbox.setAlignment(Pos.CENTER);
                inputDialog.getDialogPane().setContent(hbox);

                inputDialog.setResultConverter(dialogButton -> {
                    if (dialogButton == ButtonType.OK) {
                        String selectedOption = enumerationSetChoice.getValue();
                        return selectedOption;
                    }
                    return null;
                });

                Button submitButton=(Button) inputDialog.getDialogPane().lookupButton(ButtonType.OK);

                enumerationSetChoice.valueProperty().addListener((observable, oldValue, newValue) -> {
                    submitButton.setDisable(newValue == null);
                });


                result = inputDialog.showAndWait();
                break;
            case NUMBER:
                TextField textField =inputDialog.getEditor();
                inputDialog.setContentText("Please enter the number here:");
                textField.addEventFilter(KeyEvent.KEY_TYPED, e -> {
                    String input = e.getCharacter();
                    if (!input.matches("[0-9]")) {
                        e.consume();
                    }
                });
                result =inputDialog.showAndWait();
                break;
            case STRING:
                String inputDefaultName = freeInputExecutionDTO.getDefaultName();
                result = getStringInputFromUser(inputDialog, inputDefaultName);
                break;
            case JSON:
                result=getJsonInputFromUser();
                break;
        }

        return result;
    }

    private Optional<String> getJsonInputFromUser(){
        Optional<String> result = Optional.empty();
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Json Input Dialog");

        // Create a TextArea
        TextArea textArea = new TextArea();
        textArea.setPrefRowCount(10); // Set the preferred number of rows
        textArea.setPrefColumnCount(50); // Set the preferred number of columns

        // Add the TextArea to the dialog content
        VBox vbox = new VBox(new Label("Please enter your json body below:"), textArea);
        dialog.getDialogPane().setContent(vbox);

        // Add buttons to the dialog (OK and Cancel)
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        Button submitButton=(Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
        submitButton.setText("Submit");

        submitButton.setDisable(true);


        textArea.textProperty().addListener((observable, oldValue, newValue) -> {
            submitButton.setDisable(newValue.trim().isEmpty());
        });


        if(appController.getPrimaryStage().getScene().getStylesheets().size()!=0)
            dialog.getDialogPane().getStylesheets().add(appController.getPrimaryStage().getScene().getStylesheets().get(0));

        // Set the result converter to handle the user input
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == ButtonType.OK) {
                return textArea.getText(); // Return the content of the TextArea on OK button press
            }
            return null;
        });

        // Show the dialog and wait for user input
        result=dialog.showAndWait();
        return result;
    }

    private Optional<String> getStringInputFromUser(TextInputDialog inputDialog, String inputDefaultName) {
        Optional<String> result = Optional.empty();
        switch (inputDefaultName) {
            case "FOLDER_NAME":
                result = openFolderChooser();
                break;
            case "FILE_NAME":
                result = openNotExistFileChooser();
                break;
            case "SOURCE":
                Dialog<ButtonType> dialog = new Dialog<>();
                ToggleGroup toggleGroup = createZipChooserDialogAndGetToggle(dialog);
                Optional<ButtonType> zipResult = dialog.showAndWait();
                result = processZipResult(result, toggleGroup, zipResult);
                break;
            default:
                inputDialog.setContentText("Please enter the input here:");
                result = inputDialog.showAndWait();
                break;
        }
        return result;
    }

    private Optional<String> processZipResult(Optional<String> result, ToggleGroup toggleGroup, Optional<ButtonType> zipResult) {
        if(zipResult.isPresent()) {
            ButtonType selectedButton = zipResult.get();
            if (selectedButton == ButtonType.OK) {
                RadioButton selectedRadioButton = (RadioButton) toggleGroup.getSelectedToggle();
                if (selectedRadioButton != null) {
                    String selectedOption = selectedRadioButton.getText();
                    if (selectedOption.equals("Zip folder"))
                        result = openFolderChooser();
                    else
                        result = openExistFileChooser(null);
                }
            }
        }
        return result;
    }

    public ToggleGroup createZipChooserDialogAndGetToggle(Dialog<ButtonType> dialog) {
        dialog.setTitle("Choose zipping source");
        dialog.setHeaderText("Please select an option:");
        if(appController.getPrimaryStage().getScene().getStylesheets().size()!=0)
            dialog.getDialogPane().getStylesheets().add(appController.getPrimaryStage().getScene().getStylesheets().get(0));

        RadioButton option1 = new RadioButton("Zip folder");
        RadioButton option2 = new RadioButton("Unzip/Zip file");

        ToggleGroup toggleGroup = new ToggleGroup();
        option1.setToggleGroup(toggleGroup);
        option2.setToggleGroup(toggleGroup);

        HBox hbox = new HBox(10, option1, option2);
        hbox.setPadding(new Insets(10));

        dialog.getDialogPane().setContent(hbox);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        return toggleGroup;
    }
    private void processInput(Button button, String data) {

        RequestBody requestBody =new FormBody.Builder()
                .add("inputName",button.getId())
                .add("data",data)
                .build();

        String finalUrl = HttpUrl
                .parse(Constants.FULL_SERVER_PATH + "/process-input")
                .newBuilder()
                .build()
                .toString();

        HttpClientUtil.runAsyncPost(finalUrl, requestBody, new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                HttpClientUtil.showErrorAlert(Constants.CONNECTION_ERROR,appController);
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if(response.code()==200&& response.body()!=null) {

                    ResultDTO resultDTO =Constants.GSON_INSTANCE
                            .fromJson(response.body().string(),ResultDTO.class);

                    Platform.runLater(()->{
                        if(resultDTO.getStatus())
                        {
                            button.setStyle("-fx-background-color: #40ff00; ");
                            if(resultDTO.isFlowReady())
                                executeButton.setDisable(false);
                        }
                        else
                        {
                            Alert alert =new Alert(Alert.AlertType.ERROR);

                            ObservableList<String> stylesheets = appController.getPrimaryStage().getScene().getStylesheets();
                            if(stylesheets.size()!=0)
                                alert.getDialogPane().getStylesheets().add(stylesheets.get(0));

                            alert.setTitle("Error");
                            alert.setContentText(resultDTO.getMessage());
                            alert.showAndWait();
                        }

                    });

                }
                else
                    HttpClientUtil.showErrorAlert(Constants.CONNECTION_ERROR,appController);

                if(response.body()!=null)
                    response.body().close();


            }
        });

    }



    private TextInputDialog getNewTextInputDialog()
    {
        TextInputDialog inputDialog = new TextInputDialog();

        inputDialog.setTitle("submit input");
        inputDialog.setHeaderText(null);
        inputDialog.setGraphic(null);
        inputDialog.getDialogPane().setPrefWidth(400);

        Button submitButton=(Button) inputDialog.getDialogPane().lookupButton(ButtonType.OK);
        submitButton.setText("Submit");

        submitButton.setDisable(true);

        TextField textField = inputDialog.getEditor();

        textField.textProperty().addListener((observable, oldValue, newValue) -> {
            submitButton.setDisable(newValue.trim().isEmpty());
        });

        if(appController.getPrimaryStage().getScene().getStylesheets().size()!=0)
            inputDialog.getDialogPane().getStylesheets().add(appController.getPrimaryStage().getScene().getStylesheets().get(0));
        return  inputDialog;
    }

    public Optional<String> openFolderChooser() {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Select Folder");
        File selectedFolder = directoryChooser.showDialog(appController.getPrimaryStage());
        if (selectedFolder != null)
            return Optional.of(selectedFolder.getAbsolutePath());
        else
            return Optional.empty();
    }

    public Optional<String> openNotExistFileChooser() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("First navigate to where you want to save the file and then write his name");
        File selectedFolder = fileChooser.showSaveDialog(appController.getPrimaryStage());
        if (selectedFolder != null)
            return Optional.of(selectedFolder.getAbsolutePath());
        else
            return Optional.empty();
    }

    public Optional<String> openExistFileChooser(String filter) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select File");

        if(filter!=null)
            fileChooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter(filter.toUpperCase()+" Files", "*."+filter));


        File selectedFolder = fileChooser.showOpenDialog(appController.getPrimaryStage());
        if (selectedFolder != null)
            return Optional.of(selectedFolder.getAbsolutePath());
        else
            return Optional.empty();
    }




    public void rightInputClick(Button button)
    {
        ContextMenu contextMenu = new ContextMenu();

        MenuItem item1 = new MenuItem("Clear input's data");
        MenuItem item2 = new MenuItem("Show input's data");

        contextMenu.getItems().addAll(item1,item2);

        item1.setOnAction(event -> {
            //boolean necessity =engine.clearInputData(button.getId()).getNecessity();
            String finalUrl = HttpUrl
                    .parse(Constants.FULL_SERVER_PATH + "/input-options")
                    .newBuilder()
                    .addQueryParameter("Id", button.getId())
                    .build()
                    .toString();

            HttpClientUtil.runAsyncDelete(finalUrl, new Callback() {
                @Override
                public void onFailure(@NotNull Call call, @NotNull IOException e) {
                    HttpClientUtil.showErrorAlert(Constants.CONNECTION_ERROR, appController);
                }

                @Override
                public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                    if (response.code() == 200) {
                        if (response.body() != null) {
                            Boolean necessity = Constants.GSON_INSTANCE.fromJson(response.body().string(), Boolean.class);
                            Platform.runLater(() -> {
                            if (necessity) {
                                executeButton.setDisable(true);
                                button.setStyle("-fx-background-color: #ff0000; ");
                            } else
                                button.setStyle("");
                            });
                        }
                    }
                    else
                        HttpClientUtil.errorMessage(response.body(), appController);
                    if(response.body() != null)
                         response.body().close();
                }
            });
        });

        item2.setOnAction(event -> {
            //String data =engine.getInputData(button.getId()).getData();
            String finalUrl = HttpUrl
                    .parse(Constants.FULL_SERVER_PATH + "/input-options")
                    .newBuilder()
                    .addQueryParameter("Id", button.getId())
                    .build()
                    .toString();

            HttpClientUtil.runAsync(finalUrl, new Callback() {
                @Override
                public void onFailure(@NotNull Call call, @NotNull IOException e) {
                    HttpClientUtil.showErrorAlert(Constants.CONNECTION_ERROR, appController);
                }

                @Override
                public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                    if(response.code() == 200 && response.body() != null) {
                        String data = Constants.GSON_INSTANCE.fromJson(response.body().string(), String.class);
                        Platform.runLater(() -> {
                        Alert alert = new Alert(Alert.AlertType.INFORMATION);
                            ObservableList<String> stylesheets = appController.getPrimaryStage().getScene().getStylesheets();
                            if(stylesheets.size()!=0)
                                alert.getDialogPane().getStylesheets().add(stylesheets.get(0));
                            alert.setGraphic(null);
                            alert.setTitle("input's data");
                            if (data != null)
                                alert.setHeaderText(data);
                           else
                               alert.setHeaderText("no data");
                        alert.showAndWait();
                        });
                    }
                    else {
                        HttpClientUtil.errorMessage(response.body(), appController);
                    }
                    if(response.body() != null)
                        response.body().close();
                }
            });

        });

        button.setContextMenu(contextMenu);
    }




    @FXML
    private void executeFlow(ActionEvent event)
    {
        elementDetailsView.getChildren().clear();

        String finalUrl = HttpUrl
                .parse(Constants.FULL_SERVER_PATH + "/run-flow")
                .newBuilder()
                .build()
                .toString();

        HttpClientUtil.runAsync(finalUrl, new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                HttpClientUtil.showErrorAlert(Constants.CONNECTION_ERROR,appController);
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if(response.code()==200&&response.body()!=null){
                    String flowId=response.body().string();
                    appController.setFlowId(flowId);
                }
                else
                    HttpClientUtil.errorMessage(response.body(), appController);

                if(response.body()!=null)
                    response.body().close();
            }
        });


        //String flowId=engine.runFlow();
        //appController.addFlowId(flowId);
        executeButton.setDisable(true);

        for(Button button:mandatoryInputButtons) {
            button.setDisable(true);
            if(isAnimationsOn.get()) {
                createButtonFlipAnimation(button,-360);
            }
        }
        for (Button button:optionalInputButtons) {
            button.setDisable(true);
            if(isAnimationsOn.get()) {
                createButtonFlipAnimation(button,360);
            }
        }


        if(isAnimationsOn.get())
            elementLogic.animateTable();
        else
            elementLogic.setTableOpacity(1.0);
    }

    private void createButtonFlipAnimation(Button button,int direction)
    {
        RotateTransition rotationY = new RotateTransition();
        rotationY.setAxis(Rotate.Z_AXIS);
        rotationY.setDuration(Duration.seconds(1));
        rotationY.setByAngle(-360);
        rotationY.setNode(button);
        rotationY.setCycleCount(1);
        rotationY.play();
    }


    public void updateProgressFlow(FlowExecutionDTO flowExecutionDTO)
    {
        elementLogic.setElementDetailsView(flowExecutionDTO);
        progressBarView.setProgress(flowExecutionDTO.getProgress());
        if(flowExecutionDTO.getStateAfterRun()!=null)
        {
            afterRun=true;

            if(isAnimationsOn.get() && !elementLogic.isTableClicked() && !isClicked) {
                createFadeAnimation(elementDetailsView);
            }
            progressBarView.setProgress(1);
            hBoxView.getChildren().remove(executeButton);
            rerunButton.setOnAction(e->reRunFlow(flowExecutionDTO));
            hBoxView.getChildren().add(rerunButton);

            String finalUrl = HttpUrl
                    .parse(Constants.FULL_SERVER_PATH + "/continuation")
                    .newBuilder()
                    .addQueryParameter("getBy","current")
                    .build()
                    .toString();

            HttpClientUtil.runAsync(finalUrl, new Callback() {
                @Override
                public void onFailure(@NotNull Call call, @NotNull IOException e) {
                    HttpClientUtil.showErrorAlert(Constants.CONNECTION_ERROR, appController);
                }

                @Override
                public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                    if(response.code()==200 && response.body()!=null){
                        ContinutionMenuDTO continutionMenuDTO = Constants.GSON_INSTANCE.fromJson(response.body().string(), ContinutionMenuDTO.class);
                        Platform.runLater(() -> {
                            if(continutionMenuDTO!=null)
                            {
                                List<String> targetFlows = continutionMenuDTO.getTargetFlows();
                                choiceBoxView.setItems(FXCollections.observableArrayList(targetFlows));
                                if(isAnimationsOn.get()) {
                                    createFadeAnimation(choiceBoxView);
                                }
                                else
                                    choiceBoxView.setOpacity(1.0);
                                choiceBoxView.setDisable(false);
                            }
                        });
                    }
                    else {
                        HttpClientUtil.errorMessage(response.body(), appController);
                    }
                    if(response.body() != null)
                        response.body().close();

                }
            });
        }
    }


    private void reRunFlow(FlowExecutionDTO flowExecutionDTO)
    {
        RequestBody requestBody =new FormBody.Builder()
                .add("freeInputs",Constants.GSON_INSTANCE.toJson(flowExecutionDTO.getFreeInputs()))
                .add("flowName", flowExecutionDTO.getName())
                .build();

        String finalUrl = HttpUrl
                .parse(Constants.FULL_SERVER_PATH + "/rerun")
                .newBuilder()
                .build()
                .toString();

        HttpClientUtil.runAsyncPost(finalUrl, requestBody, new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                HttpClientUtil.showErrorAlert(Constants.CONNECTION_ERROR, appController);
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if(response.code()==200 && response.body()!=null){
                    Platform.runLater(() -> {
                        appController.streamFlow(flowExecutionDTO.getName());
                    });
                }
                else {
                    HttpClientUtil.errorMessage(response.body(), appController);
                }
                if(response.body() != null)
                    response.body().close();
            }
        });
    }
    private void createFadeAnimation(Node node)
    {
        FadeTransition fadeTransition = new FadeTransition(Duration.seconds(2), node);
        fadeTransition.setFromValue(0.0);
        fadeTransition.setToValue(1.0);
        fadeTransition.play();
    }



    @FXML
    void showFlowInfo(MouseEvent event) {
        isClicked = true;
        elementLogic.updateFlowInfoView();
    }

    @FXML
    void continueToFlow(ActionEvent event) {
        String targetName = choiceBoxView.getValue();
        RequestBody requestBody =new FormBody.Builder()
                .add("Id",elementLogic.getID())
                .add("flowName", targetName)
                .build();

        String finalUrl = HttpUrl
                .parse(Constants.FULL_SERVER_PATH + "/continuation")
                .newBuilder()
                .build()
                .toString();

        HttpClientUtil.runAsyncPost(finalUrl, requestBody, new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                HttpClientUtil.showErrorAlert(Constants.CONNECTION_ERROR, appController);
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if(response.code()==200 && response.body()!=null){
                    Platform.runLater(() -> {
                        appController.streamFlow(targetName);
                    });
                }
                else {
                    HttpClientUtil.errorMessage(response.body(), appController);
                }
                if(response.body() != null)
                    response.body().close();
            }
        });
    }

}
