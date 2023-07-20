package controllers.history;

import controllers.AppController;
import dto.FlowExecutionDTO;
import elementlogic.ElementLogic;
import enginemanager.EngineApi;
import javafx.application.Platform;
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
import javafx.stage.Stage;

import java.util.List;
import java.util.Optional;

public class HistoryController {
    @FXML
    private StackPane stackTableView;

    @FXML
    private ChoiceBox<String> stateFilterView;

    @FXML
    private VBox elementDetailsView;

    @FXML
    private VBox elementChoiceView;

    private AppController appController;

    private EngineApi engine;


    private TableView<FlowExecutionDTO> historyTableView;

    private ObservableList<FlowExecutionDTO> tableData;

    private TableColumn<FlowExecutionDTO,String> flowNameColumnView;

    private TableColumn<FlowExecutionDTO,String> activationTimeColumnView;

    private TableColumn<FlowExecutionDTO,String> flowStateColumnView;

    private ElementLogic elementLogic;

    private Integer historyVersion;




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
        //historyTableView.setOnMouseClicked(e-> HistoryTableRowClick(new ActionEvent()));
        historyTableView.setEditable(false);
        stackTableView.getChildren().add(historyTableView);

        stateFilterView.getItems().addAll("ALL", "SUCCESS", "WARNING", "FAILURE");
        stateFilterView.setValue("ALL");
        stateFilterView.setOnAction(event -> filterTable());

        tableData = FXCollections.observableArrayList();
        historyTableView.setItems(tableData);
        historyVersion = 0;

        historyTableView.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null)
                HistoryTableRowClick();
            else
                elementLogic.clear();
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
    private void HistoryTableRowClick() {

        if(!historyTableView.getSelectionModel().isEmpty()) {
            FlowExecutionDTO flowExecutionDTO = historyTableView.getSelectionModel().getSelectedItem();
            elementLogic.setElementDetailsView(flowExecutionDTO);
        }
    }

    public void updateHistoryTable(List<FlowExecutionDTO> flowExecutionDTOList)
    {
        Platform.runLater(() -> {
            historyVersion = historyVersion + flowExecutionDTOList.size();
            tableData.addAll(0,flowExecutionDTOList);

            /*
            if(tableData.size()==0){
                tableData.addAll(0,flowExecutionDTOList);
            }
            else{
                flowExecutionDTOList.forEach(this::addRow);
            }
            */

        });
    }


    public void addRow(FlowExecutionDTO flowExecutionDTO)
    {
        tableData.add(0,flowExecutionDTO);
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

    public Integer getHistoryVersion() {
        return historyVersion;
    }

    public void setHistoryVersion(int historyVersion) {
        this.historyVersion = historyVersion;
    }
}
