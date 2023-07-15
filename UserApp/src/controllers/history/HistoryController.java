package controllers.history;

import controllers.AppController;
import dto.*;
import elementlogic.ElementLogic;
import enginemanager.EngineApi;
import javafx.application.Platform;
import javafx.beans.Observable;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;
import utils.Constants;
import utils.HttpClientUtil;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

public class HistoryController {
    @FXML
    private StackPane stackTableView;

    @FXML
    private ChoiceBox<String> stateFilterView;

    @FXML
    private VBox elementDetailsView;

    @FXML
    private VBox elementChoiceView;

    @FXML
    private Button reRunButton;

    @FXML
    private Button continuationButton;

    private AppController appController;

    private EngineApi engine;


    private TableView<FlowExecutionDTO> historyTableView;

    private ObservableList<FlowExecutionDTO> tableData;

    private TableColumn<FlowExecutionDTO,String> flowNameColumnView;

    private TableColumn<FlowExecutionDTO,String> activationTimeColumnView;

    private TableColumn<FlowExecutionDTO,String> flowStateColumnView;

    private ElementLogic elementLogic;


    @FXML
    public void initialize() {

        historyTableView=new TableView<>();
        historyTableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        flowNameColumnView=new TableColumn<>("flow name");
        activationTimeColumnView=new TableColumn<>("activation time");
        flowStateColumnView =new TableColumn<>("state after run");

        flowNameColumnView.setCellValueFactory(new PropertyValueFactory<>("name"));
        flowStateColumnView.setCellValueFactory(new PropertyValueFactory<>("stateAfterRun"));
        activationTimeColumnView.setCellValueFactory(new PropertyValueFactory<>("activationTime"));

        historyTableView.getColumns().addAll(flowNameColumnView,activationTimeColumnView, flowStateColumnView);
        historyTableView.getColumns().forEach(column -> column.setMinWidth(200));
        historyTableView.setOnMouseClicked(e-> HistoryTableRowClick(new ActionEvent()));
        historyTableView.setEditable(false);
        stackTableView.getChildren().add(historyTableView);

        stateFilterView.getItems().addAll("ALL", "SUCCESS", "WARNING", "FAILURE");
        stateFilterView.setValue("ALL");
        stateFilterView.setOnAction(event -> filterTable());

        tableData = FXCollections.observableArrayList();
        historyTableView.setItems(tableData);

        reRunButton.setDisable(true);
        continuationButton.setDisable(true);



        historyTableView.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                FlowExecutionDTO flowExecutionDTO=historyTableView.getSelectionModel().getSelectedItem();
                reRunButton.setDisable(!appController.canRunFlow(flowExecutionDTO.getName()));
                checkIfContinuationsAvailable(flowExecutionDTO);
                elementLogic.setElementDetailsView(flowExecutionDTO);
            } else {
                reRunButton.setDisable(true);
                continuationButton.setDisable(true);
            }
        });


    }


    private void showErrorAlert(String message) {
        Alert alert =new Alert(Alert.AlertType.ERROR);

        ObservableList<String> stylesheets = appController.getPrimaryStage().getScene().getStylesheets();
        if(stylesheets.size()!=0)
            alert.getDialogPane().getStylesheets().add(stylesheets.get(0));

        alert.setTitle("Error");
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void checkIfContinuationsAvailable(FlowExecutionDTO selectedItem) {
        String finalUrl = HttpUrl
                .parse(Constants.FULL_SERVER_PATH + "/continuation")
                .newBuilder()
                .addQueryParameter("getBy","name")
                .addQueryParameter("flowName", selectedItem.getName())
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
                        if (continutionMenuDTO != null)
                            continuationButton.setDisable(false);
                        else
                            continuationButton.setDisable(true);
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


    public void setAppController(AppController appController) {
        this.appController = appController;
    }

    public void setStage(Stage stage)
    {
        elementLogic=new ElementLogic(elementChoiceView,elementDetailsView,stage);
    }

    public void setEngine(EngineApi engine) {
        this.engine = engine;
    }

    @FXML
    void reRunFlow(ActionEvent event) {
        if(!historyTableView.getSelectionModel().isEmpty()) {
            FlowExecutionDTO flowExecutionDTO = historyTableView.getSelectionModel().getSelectedItem();
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
    }

    @FXML
    private void HistoryTableRowClick(ActionEvent event) {

        if(!historyTableView.getSelectionModel().isEmpty()) {
            FlowExecutionDTO flowExecutionDTO = historyTableView.getSelectionModel().getSelectedItem();
            elementLogic.setElementDetailsView(flowExecutionDTO);
        }
    }


    public void setHistoryTable(List<FlowExecutionDTO> flowExecutions){
        Platform.runLater(()->{
            tableData.clear();
            flowExecutions.forEach(this::addRow);
        });
    }

    public void addRows(List<FlowExecutionDTO> flowExecutions){
        Platform.runLater(()->{
            tableData.addAll(0,flowExecutions);
        });
    }

    public void addRow(FlowExecutionDTO flowExecutionDTO)
    {
        tableData.add(flowExecutionDTO);
    }

    public void filterTable()
    {
        String choice = stateFilterView.getValue();
        FilteredList<FlowExecutionDTO> filteredData = new FilteredList<>(tableData);
        switch(choice) {
            case "ALL":
                historyTableView.setItems(tableData);
                break;
            case "SUCCESS":
                filteredData.setPredicate(item -> {
                    return item.getStateAfterRun().equals("SUCCESS");
                });
                historyTableView.setItems(filteredData);
                break;
            case "FAILURE":
                filteredData.setPredicate(item -> {
                    return item.getStateAfterRun().equals("FAILURE");
                });
                historyTableView.setItems(filteredData);
                break;
            case "WARNING":
                filteredData.setPredicate(item -> {
                    return item.getStateAfterRun().equals("WARNING");
                });
                historyTableView.setItems(filteredData);
                break;

        }
        historyTableView.refresh();
    }

    public void clearTab()
    {
        elementLogic.clear();
        tableData.clear();
        historyTableView.getItems().clear();
        stateFilterView.setValue("ALL");
    }


    @FXML
    void showFlowInfo(MouseEvent event) {
        elementLogic.updateFlowInfoView();
    }

    @FXML
    void openContinuationPopUp(ActionEvent event) {
        FlowExecutionDTO flowExecutionDTO = historyTableView.getSelectionModel().getSelectedItem();
        TextInputDialog inputDialog =getNewTextInputDialog();
        String finalUrl = HttpUrl
                .parse(Constants.FULL_SERVER_PATH + "/continuation")
                .newBuilder()
                .addQueryParameter("getBy","name")
                .addQueryParameter("flowName", flowExecutionDTO.getName())
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
                        Optional<String> result = Optional.empty();
                        ChoiceBox<String> continuationChoice = new ChoiceBox<>();
                        continuationChoice.getItems().addAll(continutionMenuDTO.getTargetFlows());
                        continuationChoice.setStyle("-fx-pref-width: 200px;");

                        HBox hbox = new HBox(10, new Label("Available Continuations:"), continuationChoice);
                        hbox.setAlignment(Pos.CENTER);
                        inputDialog.getDialogPane().setContent(hbox);

                        inputDialog.setResultConverter(dialogButton -> {
                            if (dialogButton == ButtonType.OK) {
                                String selectedOption = continuationChoice.getValue();
                                return selectedOption;
                            }
                            return null;
                        });

                        Button submitButton=(Button) inputDialog.getDialogPane().lookupButton(ButtonType.OK);

                        continuationChoice.valueProperty().addListener((observable, oldValue, newValue) -> {
                            submitButton.setDisable(newValue == null);
                        });

                        result = inputDialog.showAndWait();
                        if(result.isPresent())
                        {
                            String targetName= result.get();
                            continueToFlow(targetName, flowExecutionDTO.getId());
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

    private TextInputDialog getNewTextInputDialog()
    {
        TextInputDialog inputDialog = new TextInputDialog();

        inputDialog.setTitle("Choose continuation");
        inputDialog.setHeaderText(null);
        inputDialog.setGraphic(null);
        inputDialog.getDialogPane().setPrefWidth(400);

        Button submitButton=(Button) inputDialog.getDialogPane().lookupButton(ButtonType.OK);
        submitButton.setText("Continue to flow");

        submitButton.setDisable(true);

        TextField textField = inputDialog.getEditor();

        textField.textProperty().addListener((observable, oldValue, newValue) -> {
            submitButton.setDisable(newValue.trim().isEmpty());
        });

        if(appController.getPrimaryStage().getScene().getStylesheets().size()!=0)
            inputDialog.getDialogPane().getStylesheets().add(appController.getPrimaryStage().getScene().getStylesheets().get(0));
        return  inputDialog;
    }

    void continueToFlow(String targetName, String id) {
        RequestBody requestBody =new FormBody.Builder()
                .add("Id",id)
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
