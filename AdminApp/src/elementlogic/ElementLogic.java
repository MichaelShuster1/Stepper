package elementlogic;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import data.Relation;
import dto.*;
import enums.DataType;
import javafx.animation.FadeTransition;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.MapValueFactory;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.List;
import java.util.Map;

public class ElementLogic {
    private VBox elementChoiceView;
    private VBox elementDetailsView;
    private Stage primaryStage;
    private FlowExecutionDTO flowExecutionDTO;

    private TableView<StepExecutionDTO> stepsTableView;

    private TableColumn<StepExecutionDTO,String> stepColumnView;

    private TableColumn<StepExecutionDTO,String> stateColumnView;
    boolean tableClicked;


    public ElementLogic(VBox elementChoiceView, VBox elementDetailsView, Stage primaryStage) {
        this.elementChoiceView = elementChoiceView;
        this.elementDetailsView = elementDetailsView;
        this.primaryStage=primaryStage;
        this.tableClicked = false;

        stepsTableView=new TableView<>();
        stepColumnView=new TableColumn<>("step");
        stateColumnView=new TableColumn<>("state");

        stepColumnView.setCellValueFactory(new PropertyValueFactory<>("name"));
        stateColumnView.setCellValueFactory(new PropertyValueFactory<>("stateAfterRun"));

        stepsTableView.getColumns().addAll(stepColumnView,stateColumnView);

        stepsTableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        elementChoiceView.getChildren().add(stepsTableView);
        VBox.setVgrow(stepsTableView, Priority.ALWAYS);


        stepsTableView.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                rowSelect();
            }
        });


        stepsTableView.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                rowSelect();
            }
        });
    }

    public void setTableOpacity(Double opacity) {
        stepsTableView.setOpacity(opacity);
    }

    public void animateTable() {
            FadeTransition fadeTransition = new FadeTransition(Duration.seconds(2), stepsTableView);
            fadeTransition.setFromValue(0.0);
            fadeTransition.setToValue(1.0);
            fadeTransition.play();
    }




    public void setElementDetailsView(FlowExecutionDTO flowExecutionDTO)
    {
        this.flowExecutionDTO=flowExecutionDTO;
        ObservableList<StepExecutionDTO> items= FXCollections.observableArrayList();
        items.addAll(flowExecutionDTO.getSteps());
        stepsTableView.setItems(items);

        if(flowExecutionDTO.getStateAfterRun()!=null)
            updateFlowInfoView();

    }



    public void clear()
    {
        elementDetailsView.getChildren().clear();
        stepsTableView.getItems().clear();
        flowExecutionDTO=null;
        tableClicked = false;
    }

    public boolean isTableClicked() {
        return tableClicked;
    }

    @FXML
    private void rowSelect()
    {
        if(!stepsTableView.getSelectionModel().isEmpty()) {

            elementDetailsView.getChildren().clear();
            StepExecutionDTO stepExecutionDTO=stepsTableView.getSelectionModel().getSelectedItem();
            StepExtensionDTO stepExtensionDTO =stepExecutionDTO.getStepExtensionDTO();
            tableClicked = true;

            addTitleLine("STEP'S DETAILS: ");
            addKeyValueLine("Name: ",stepExecutionDTO.getName());
            addKeyValueLine("Run Time: ",stepExecutionDTO.getRunTime()+ "ms");
            addKeyValueLine("Finish state: ",stepExecutionDTO.getStateAfterRun());
            addTitleLine("\nSTEP'S INPUT DATA:");
            addStepInputsOrOutputsData(stepExtensionDTO.getInputs());
            addTitleLine("\nSTEP'S OUTPUTS DATA:");
            addStepInputsOrOutputsData(stepExtensionDTO.getOutputs());
            addTitleLine("\nSTEP LOGS: ");
            addStepLogs(stepExtensionDTO.getLogs());
        }
    }

    private HBox getNewHbox()
    {
        HBox hBox =new HBox();
        hBox.setAlignment(Pos.BASELINE_LEFT);
        hBox.setPrefHeight(Region.USE_COMPUTED_SIZE);
        hBox.setPrefWidth(Region.USE_COMPUTED_SIZE);
        return  hBox;
    }

    private void addKeyValueLine(String name, String value)
    {
        HBox hBox = getNewHbox();

        Label key =new Label(name);
        key.setFont(Font.font("System", FontWeight.BOLD,12));

        Label data =new Label(value);
        data.setAlignment(Pos.TOP_LEFT);

        hBox.getChildren().add(key);
        hBox.getChildren().add(data);

        elementDetailsView.getChildren().add(hBox);
    }


    private void addKeyHyperLinkValueLine(String name, String value, Object data)
    {
        HBox hBox = getNewHbox();

        Label label =new Label(name+": ");
        label.setFont(Font.font("System",FontWeight.BOLD,12));

        Hyperlink hyperlink=new Hyperlink(value);

        switch (DataType.valueOf(value.toUpperCase()))
        {
            case RELATION:
                hyperlink.setOnMouseClicked(e->relationPopUp((Relation) data));
                break;
            case LIST:
                hyperlink.setOnMouseClicked(e->listPopUp((List<Object>) data));
                break;
            case JSON:
                Gson gson=new GsonBuilder().setPrettyPrinting().create();
                JsonElement jsonElement = JsonParser.parseString(data.toString());
                hyperlink.setOnMouseClicked(e->textAreaPopUp(gson.toJson(jsonElement)));
                break;
        }



        hBox.getChildren().add(label);
        hBox.getChildren().add(hyperlink);

        elementDetailsView.getChildren().add(hBox);
    }

    private void addKeyProgressIndicator(String name)
    {
        HBox hBox = getNewHbox();

        Label key =new Label(name);
        key.setFont(Font.font("System", FontWeight.BOLD,12));

        hBox.setMaxHeight(14);

        ProgressIndicator progressIndicator =new ProgressIndicator();
        progressIndicator.setProgress(ProgressIndicator.INDETERMINATE_PROGRESS);

        hBox.getChildren().add(key);
        hBox.getChildren().add(progressIndicator);

        elementDetailsView.getChildren().add(hBox);
    }



    private void addTitleLine(String title)
    {
        HBox hBox= getNewHbox();
        Label label = new Label(title);
        label.setFont(Font.font("System",FontWeight.BOLD,14));
        hBox.getChildren().add(label);
        elementDetailsView.getChildren().add(hBox);
    }


    private void addStepInputsOrOutputsData(List<DataExecutionDTO> io)
    {
        for(DataExecutionDTO dataExecutionDTO:io)
        {
            String name=dataExecutionDTO.getName();
            String type=dataExecutionDTO.getType();
            Object data=dataExecutionDTO.getData();

            if(data!=null) {
                if( type.equals(DataType.RELATION.toString()) || type.equals(DataType.LIST.toString())
                        || type.equals(DataType.JSON.toString()) )
                    addKeyHyperLinkValueLine(name,type,data);
                else
                    addKeyValueLine(name+": ",data.toString());
            }
            else
                addKeyValueLine(name+": ","No Data Received");
        }
        if(io.size()==0)
            addKeyValueLine("No outputs exist","");
    }


    private void showNewPopUp(Parent root)
    {
        final Stage stage = new Stage();
        stage.initOwner(primaryStage);
        stage.initModality(Modality.APPLICATION_MODAL);
        Scene scene = new Scene(root, 400, 300);
        if(primaryStage.getScene().getStylesheets().size()!=0)
            scene.getStylesheets().add(primaryStage.getScene().getStylesheets().get(0));
        stage.setScene(scene);
        stage.show();
    }


    private TableView<Map<String, String>> createTableView(Relation data)
    {
        TableView<Map<String, String>> tableView=new TableView<>();
        List<String> columnNames=data.getColumnNames();

        for(String columnName :columnNames)
        {
            TableColumn<Map<String, String>,String> columnView=new TableColumn<>(columnName);
            columnView.setCellValueFactory(new MapValueFactory(columnName));
            tableView.getColumns().add(columnView);
        }

        ObservableList<Map<String,String>> items= FXCollections.observableArrayList();
        items.addAll(data.getRows());
        tableView.setItems(items);
        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tableView.setEditable(false);
        return  tableView;
    }

    private void relationPopUp(Relation data) {

        showNewPopUp(createTableView(data));
    }

    private void listPopUp(List<Object> data)
    {
        if(data.size()==0) {
            showEmptyPopUp("empty list");
        }
        else
            showNewPopUp(createListView(data));
    }


    private ListView<String> createListView(List<Object> list)
    {
        ListView<String> listView=new ListView<>();
        listView.setOrientation(Orientation.VERTICAL);
        int counter=1;
        for(Object object:list)
        {
            listView.getItems().add(counter+"."+object.toString());
            counter++;
        }
        return  listView;
    }

    private void textAreaPopUp(String json) {
        if(json==null||json.isEmpty()){
            showEmptyPopUp("empty json");
        }
        else {
            showNewPopUp(createTextAreaPopUp(json));
        }

    }

    private StackPane createTextAreaPopUp(String json) {
        TextArea textArea = new TextArea(json);
        textArea.setPrefRowCount(10); // Set the preferred number of rows
        textArea.setPrefColumnCount(50); // Set the preferred number of columns
        textArea.setEditable(false);

        // Create a layout pane to hold the TextArea
        StackPane root = new StackPane();
        root.getChildren().add(textArea);
        return  root;
    }

    private void showEmptyPopUp(String text) {
        Label label =new Label(text);
        label.setAlignment(Pos.CENTER);
        label.setFont(Font.font("System", FontWeight.BOLD,15));
        BorderPane borderPane =new BorderPane();
        borderPane.setCenter(label);
        showNewPopUp(borderPane);
    }



    private void addStepLogs(List<String> logs) {
        if (logs.size() == 0)
            addKeyValueLine( "","The step had no logs\n\n");
        else {
            for (String currLog : logs) {
                addKeyValueLine( "",currLog+"\n\n");
            }
        }
    }


    public void updateFlowInfoView()
    {
        elementDetailsView.getChildren().clear();
        if(flowExecutionDTO==null)
            return;


        updateFlowNameIDAndState();
        if(flowExecutionDTO.getStateAfterRun()!=null)
            addKeyValueLine("Flow total run time: " , flowExecutionDTO.getRunTime() + " ms");
        else
            addKeyProgressIndicator("Flow total run time: ");
            //addKeyValueLine("Flow total run time: " ,  "flow is still running");


        addTitleLine("\n\nFREE INPUTS THAT RECEIVED DATA:\n");
        if(flowExecutionDTO.getFreeInputs().size()!=0) {
            updateFlowFreeInputs(flowExecutionDTO.getFreeInputs(), true);
            updateFlowFreeInputs(flowExecutionDTO.getFreeInputs(), false);
        }
        else
            addKeyValueLine("","NO FREE INPUTS HAVE RECEIVED DATA");
        addTitleLine("\nDATA PRODUCED (OUTPUTS):\n");
        if(flowExecutionDTO.getOutputs().size()!=0)
            updateOutputsHistoryData(flowExecutionDTO.getOutputs());
        else
            addKeyValueLine("","NO DATA WAS PRODUCED");
        addTitleLine("\nFLOW STEPS DATA:\n");
        updateStepsHistoryData(flowExecutionDTO.getSteps());

    }

    private void updateFlowNameIDAndState() {
        addTitleLine("FLOW EXECUTION DATA:\n");

        UserDetailsDTO userDetails=flowExecutionDTO.getUserDetails();
        String isManager=userDetails.getManager()? "yes":"no";
        addKeyValueLine("Executed by: ", userDetails.getUserName());
        addKeyValueLine("Is Manager: ",isManager);

        addKeyValueLine("Flow unique ID: ",flowExecutionDTO.getId());
        addKeyValueLine("Flow name: ",flowExecutionDTO.getName());
        if(flowExecutionDTO.getStateAfterRun()!=null)
            addKeyValueLine("Flow's final state : " , flowExecutionDTO.getStateAfterRun());
        else
            addKeyProgressIndicator("Flow's final state :   ");

    }
    private void updateFlowFreeInputs(List<FreeInputExecutionDTO> flowFreeInputs, boolean mandatoryOrNot)
    {
        for (FreeInputExecutionDTO freeInput : flowFreeInputs) {
            if (freeInput.getData() != null) {
                if(freeInput.isMandatory()==mandatoryOrNot) {

                    addKeyValueLine("Name: ", freeInput.getName());
                    addKeyValueLine("Type: ", freeInput.getType());
                    addKeyValueLine("Input data: ", freeInput.getData());

                    if (mandatoryOrNot)
                        addKeyValueLine("This input is mandatory: ", "Yes\n\n");
                    else
                        addKeyValueLine("This input is mandatory: ", "No\n\n");
                }
            }
        }

    }

    private void updateOutputsHistoryData(List<DataExecutionDTO> outputs) {
        for (DataExecutionDTO output : outputs) {
            addKeyValueLine("Name: " , output.getName());
            addKeyValueLine("Type: " , output.getType());
            if (output.getData() != null) {
                if(output.getType().equals("List") || output.getType().equals("Relation")
                        || output.getType().equals("Json"))  {
                    addKeyHyperLinkValueLine("Data",output.getType(),output.getData());
                    addKeyValueLine("" ,"\n");
                }
                else
                    addKeyValueLine("Data: " ,output.getData().toString()+"\n\n");

            }
            else
                addKeyValueLine("Data: " ,"Not created due to failure in flow\n\n");
        }
    }

    private void updateStepsHistoryData(List<StepExecutionDTO> steps) {
        for (StepExecutionDTO step: steps) {
            addKeyValueLine("Step Name: " , step.getName());
            addKeyValueLine("Run time: " , step.getRunTime() + " ms");
            addKeyValueLine("Finish state: " , step.getStateAfterRun());
            addKeyValueLine("Step summary:" , step.getSummaryLine());
            addKeyValueLine("\nSTEP LOGS:","");
            addStepLogs(step.getStepExtensionDTO().getLogs());
        }
    }

    public String getID() {
        return flowExecutionDTO.getId();
    }

}
