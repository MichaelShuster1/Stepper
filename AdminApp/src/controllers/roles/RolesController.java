package controllers.roles;

import controllers.AppController;
import dto.RoleInfoDTO;
import dto.UserInfoDTO;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldListCell;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Popup;
import okhttp3.HttpUrl;
import utils.Constants;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class RolesController {
    @FXML
    private ListView<RoleInfoDTO> rolesListView;
    @FXML
    private VBox roleSelectedView;
    private AppController appController;

    private List<CheckBox> checkBoxes;

    private Consumer<String> rolesOption;


    @FXML
    public void initialize() {
        rolesListView.setOrientation(Orientation.VERTICAL);
        rolesListView.setCellFactory(listView -> new TextFieldListCell<RoleInfoDTO>() {
            @Override
            public void updateItem(RoleInfoDTO roleInfoDTO, boolean empty) {
                super.updateItem(roleInfoDTO, empty);
                if (empty || roleInfoDTO == null) {
                    setText(null);
                } else {
                    setText(roleInfoDTO.getName());
                }
            }
        });
        rolesListView.setOnMouseClicked(event -> rowClick(new ActionEvent()));
        initRoles();
        checkBoxes=new ArrayList<>();
    }


    private void initRoles()
    {
        RoleInfoDTO roleInfoDTO =new RoleInfoDTO("Read Only Flows", "Permission to use " +
                "all the available read-only flows in the system",null,null);
        rolesListView.getItems().add(roleInfoDTO);

        roleInfoDTO=new RoleInfoDTO("All Flows", "Permission to use " +
                "all the available flows in the system",null,null);
        rolesListView.getItems().add(roleInfoDTO);
    }

    private void rowClick(ActionEvent event) {
        if(!rolesListView.getSelectionModel().isEmpty()) {
            roleSelectedView.getChildren().clear();
            RoleInfoDTO roleInfo=rolesListView.getSelectionModel().getSelectedItem();
            showRoleDetails(roleInfo);
        }
    }

    private void showRoleDetails(RoleInfoDTO roleInfoDTO){
        addTitleLine("ROLE DETAILS:\n");
        addKeyValueLine("role name: ",roleInfoDTO.getName());
        addKeyValueLine("role description: ",roleInfoDTO.getDescription());
        addTitleLine("\nASSIGNED USERS:\n");

        Set<String> users=roleInfoDTO.getUsersAssigned();

        if(users==null||users.size()==0)
            addKeyValueLine("not assigned users to this role","");
        else
            showAssignedUsers(users);

        addTitleLine("\nFLOWS:\n");
        if(checkBoxes.size()==0)
            addKeyValueLine("no flows had been added to the system","");
        else
            showFlowsCheckBoxes(roleInfoDTO.getFlowsAssigned());

    }

    private void showAssignedUsers(Set<String> users){
        for(String userName:users){
            addKeyValueLine(userName,"");
        }
    }

    private void showFlowsCheckBoxes(Set<String> flows){
        Boolean selected;
        for(CheckBox checkBox :checkBoxes){
            selected=false;

            if(flows!=null)
                selected=flows.contains(checkBox.getText());

            checkBox.setSelected(selected);
            addCheckBox(checkBox,roleSelectedView);
        }
    }



    private void addCheckBox(CheckBox checkBox,VBox vbox)
    {
        vbox.getChildren().add(checkBox);
        VBox.setMargin(checkBox, new Insets(10, 0, 0, 0));
    }

    private HBox getNewHbox()
    {
        HBox hBox =new HBox();
        hBox.setAlignment(Pos.BASELINE_LEFT);
        hBox.setPrefHeight(Region.USE_COMPUTED_SIZE);
        hBox.setPrefWidth(Region.USE_COMPUTED_SIZE);
        return  hBox;
    }

    private void addTitleLine(String title)
    {
        HBox hBox= getNewHbox();
        Label label = new Label(title);
        label.setFont(Font.font("System", FontWeight.BOLD,14));
        hBox.getChildren().add(label);
        roleSelectedView.getChildren().add(hBox);
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

        roleSelectedView.getChildren().add(hBox);
    }


    private HBox createLabelTextFieldHBox(String labelName)
    {
        HBox hBox=getNewHbox();
        Label nameLabel = new Label(labelName);
        TextField nameTextField = new TextField();
        hBox.getChildren().add(nameLabel);
        hBox.getChildren().add(nameTextField);
        return hBox;
    }

    @FXML
    void SaveButtonClicked(ActionEvent event) {
        System.out.println("save click");
    }

    @FXML
    void newButtonClicked(ActionEvent event) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Add Role");
        dialog.setHeaderText("Enter your information");

        // Create form fields
        TextField nameTextField = new TextField();
        TextField descriptionTextField = new TextField();

        List<String> flows=checkBoxes.stream()
                .map(Labeled::getText)
                .collect(Collectors.toList());

        ListView<String> listView = new ListView<>(FXCollections.observableArrayList(flows));

        // Create labels
        Label nameLabel = new Label("Role Name:");
        Label descriptionLabel = new Label("Role Description:");
        Label optionLabel = new Label("Flows Options:");

        // Enable multiple selection in the ListView
        listView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        // Create grid pane and add form fields and labels
        GridPane gridPane = new GridPane();
        gridPane.setPadding(new Insets(10));
        gridPane.setHgap(10);
        gridPane.setVgap(10);
        gridPane.addRow(0, nameLabel, nameTextField);
        gridPane.addRow(1, descriptionLabel, descriptionTextField);
        gridPane.addRow(3, optionLabel, listView);

        // Set the grid pane as the dialog content
        dialog.getDialogPane().setContent(gridPane);

        // Add buttons to the dialog
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        // Wait for dialog response
        dialog.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                // Process form data here
                String name = nameTextField.getText();
                String description = descriptionTextField.getText();
                ObservableList<String> selectedOptions = listView.getSelectionModel().getSelectedItems();
                System.out.println("Name: " + name);
                System.out.println("Email: " + description);
                System.out.println("Selected Options: " + selectedOptions);
                rolesOption.accept(name);
                RoleInfoDTO roleInfo=new RoleInfoDTO(name,description,null,null);
                rolesListView.getItems().add(roleInfo);
            }
        });
    }
    public void setAppController(AppController appController) {
        this.appController = appController;
    }

    public void setRolesOption(Consumer<String> rolesOption){
        this.rolesOption=rolesOption;
    }
}
