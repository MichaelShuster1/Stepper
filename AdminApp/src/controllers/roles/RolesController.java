package controllers.roles;

import controllers.AppController;
import dto.RoleInfoDTO;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.util.Pair;
import okhttp3.*;
import utils.Constants;
import utils.HttpClientUtil;

import java.io.IOException;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class RolesController {
    @FXML
    private ListView<String> rolesListView;
    @FXML
    private VBox roleSelectedView;

    @FXML
    private Button deleteButton;

    @FXML
    private Button saveButton;

    private AppController appController;

    private List<CheckBox> checkBoxes;

    private Consumer<String> roleAdder;

    private Consumer<String> roleDeleter;

    private String roleName;

    private Set<String> users;


    @FXML
    public void initialize() {
        rolesListView.setOrientation(Orientation.VERTICAL);
        checkBoxes=new ArrayList<>();
        rolesListView.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null)
                rowSelect(false);
            else
                roleSelectedView.getChildren().clear();
        });
    }

    private boolean canNotEdit(){
        String selectedRoleName=rolesListView.getSelectionModel().getSelectedItem();
        return selectedRoleName.equals("All Flows") || selectedRoleName.equals("Read Only Flows");
    }


    public void rowSelect(Boolean fromTab) {
        if(!rolesListView.getSelectionModel().isEmpty()) {
            roleSelectedView.getChildren().clear();

            roleName=rolesListView.getSelectionModel().getSelectedItem();

            String finalUrl = HttpUrl
                    .parse(Constants.FULL_SERVER_PATH + "/role")
                    .newBuilder()
                    .addQueryParameter("roleName", roleName)
                    .build()
                    .toString();

            HttpClientUtil.runAsync(finalUrl, new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    HttpClientUtil.showErrorAlert(Constants.CONNECTION_ERROR,appController);
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if(response.code()==200){
                        RoleInfoDTO roleInfo = Constants.GSON_INSTANCE
                                .fromJson(response.body().string(), RoleInfoDTO.class);
                        Platform.runLater(()->showRoleDetails(roleInfo,fromTab));
                    }
                    else
                        HttpClientUtil.errorMessage(response.body(),appController);

                    if(response.body()!=null)
                        response.body().close();
                }
            });


        }
    }


    @FXML
    void deleteButtonClicked(ActionEvent event) {
        String RESOURCE="/role";

        String finalUrl = HttpUrl
                .parse(Constants.FULL_SERVER_PATH + RESOURCE)
                .newBuilder()
                .addQueryParameter("roleName", roleName)
                .build()
                .toString();

        final String roleToDelete=roleName;

        HttpClientUtil.runAsyncDelete(finalUrl, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                HttpClientUtil.showErrorAlert(Constants.CONNECTION_ERROR,appController);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if(response.code()==200){
                    Platform.runLater(()->{
                        if(roleToDelete.equals(roleName)) {
                            if (rolesListView.getSelectionModel() != null) {
                                //roleName = rolesListView.getSelectionModel().getSelectedItem();
                                roleName = null;
                                rolesListView.getSelectionModel().clearSelection();
                            }
                            else
                                roleName = null;
                        }
                        rolesListView.getItems().remove(roleToDelete);
                        roleSelectedView.getChildren().clear();
                        roleDeleter.accept(roleToDelete);
                        saveButton.setDisable(true);
                        deleteButton.setDisable(true);
                        showInfoAlert("the role was deleted successfully");
                    });
                }
                else{
                    HttpClientUtil.errorMessage(response.body(),appController);
                }

                if(response.body()!=null)
                    response.body().close();;

            }
        });


    }

    private void showRoleDetails(RoleInfoDTO roleInfoDTO,Boolean fromTab){

        String roleName=roleInfoDTO.getName();

        saveButton.setDisable(canNotEdit());
        deleteButton.setDisable(canNotEdit());

        addTitleLine("ROLE DETAILS:\n");
        addKeyValueLine("role name: ",roleName);
        addKeyValueLine("role description: ",roleInfoDTO.getDescription());
        addTitleLine("\nASSIGNED USERS:\n");


        users=roleInfoDTO.getUsersAssigned();

        if(users==null||users.size()==0)
            addKeyValueLine("not assigned users to this role","");
        else
            showAssignedUsers(users);

        addTitleLine("\nFLOWS:\n");
        if(checkBoxes.size()==0)
            addKeyValueLine("no flows had been added to the system","");
        else
            showFlowsCheckBoxes(roleInfoDTO.getFlowsAssigned(),fromTab);

    }

    private void showAssignedUsers(Set<String> users){
        for(String userName:users){
            addKeyValueLine(userName,"");
        }
    }


    private void showFlowsCheckBoxes(Set<String> flows,Boolean fromTab){
        Boolean selected;
        for(CheckBox checkBox :checkBoxes){
            selected=false;

            if(flows!=null)
                selected=flows.contains(checkBox.getText());

            if(!fromTab)
                checkBox.setSelected(selected);
            checkBox.setDisable(canNotEdit());
            addCheckBox(checkBox,roleSelectedView);
        }
    }

    public void updateFlows(Set<String> newFlows){
        if(newFlows!=null)
            newFlows.forEach(flowName->checkBoxes.add(new CheckBox(flowName)));
        if(rolesListView.getSelectionModel().getSelectedItem() != null) {
            String selectedRole = rolesListView.getSelectionModel().getSelectedItem();
            rolesListView.getSelectionModel().clearSelection();
            rolesListView.getSelectionModel().select(rolesListView.getItems().indexOf(selectedRole));
        }
    }

    public void setRolesListView(Set<String> rolesName){
        rolesName.forEach(roleName->rolesListView.getItems().add(roleName));
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


    @FXML
    void SaveButtonClicked(ActionEvent event) {
        Set<String> flowsChoice=checkBoxes.stream()
                .filter(CheckBox::isSelected)
                .map(Labeled::getText)
                .collect(Collectors.toSet());

        String RESOURCE="/role";

        String finalUrl = HttpUrl
                .parse(Constants.FULL_SERVER_PATH + RESOURCE)
                .newBuilder()
                .addQueryParameter("roleName", roleName)
                .build()
                .toString();


        RequestBody requestBody =new FormBody.Builder()
                .add("flows",Constants.GSON_INSTANCE.toJson(flowsChoice))
                .build();

        HttpClientUtil.runAsyncPost(finalUrl,requestBody,new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                HttpClientUtil.showErrorAlert(Constants.CONNECTION_ERROR,appController);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if(response.code()==200){
                    Platform.runLater(()->{
                        showInfoAlert("the changes were updated successfully");
                    });
                }
                else
                    HttpClientUtil.errorMessage(response.body(),appController);

                if(response.body()!=null)
                    response.body().close();

            }
        });



    }

    @FXML
    void newButtonClicked(ActionEvent event) {


        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Add Role");
        dialog.setHeaderText("Enter Role information");

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

        Button okButton=(Button) dialog.getDialogPane().lookupButton(ButtonType.OK);

        okButton.setDisable(true);

        nameTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            okButton.setDisable(newValue.trim().isEmpty() || descriptionTextField.getText().trim().isEmpty());
        });

        descriptionTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            okButton.setDisable(newValue.trim().isEmpty() || nameTextField.getText().trim().isEmpty());
        });


        // Wait for dialog response
        if(appController.getPrimaryStage().getScene().getStylesheets().size()!=0)
            dialog.getDialogPane().getStylesheets().add(appController.getPrimaryStage().getScene().getStylesheets().get(0));
        dialog.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                // Process form data here

                String name = nameTextField.getText();
                String description = descriptionTextField.getText();
                Set<String> selectedFlows = new HashSet<>(listView.getSelectionModel().getSelectedItems());


                RoleInfoDTO roleInfo=new RoleInfoDTO(name,description,selectedFlows,null);
                processNewRoleInput(roleInfo);
            }
        });
    }

    private void processNewRoleInput(RoleInfoDTO roleInfo){
        String RESOURCE="/new-role";

        String finalUrl = HttpUrl
                .parse(Constants.FULL_SERVER_PATH + RESOURCE)
                .newBuilder()
                .build()
                .toString();

        RequestBody requestBody =new FormBody.Builder()
                .add("newRole",Constants.GSON_INSTANCE.toJson(roleInfo))
                .build();


        HttpClientUtil.runAsyncPost(finalUrl,requestBody,new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                HttpClientUtil.showErrorAlert(Constants.CONNECTION_ERROR,appController);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if(response.code()==200){
                    Platform.runLater(()->{
                        showInfoAlert("the new role was added to the system successfully");
                        roleAdder.accept(roleInfo.getName());
                        rolesListView.getItems().add(roleInfo.getName());
                    });
                }
                else
                    HttpClientUtil.errorMessage(response.body(),appController);

            }
        });


    }

    private void showInfoAlert(String message){

        Alert alert =new Alert(Alert.AlertType.INFORMATION);
        ObservableList<String> stylesheets = appController.getPrimaryStage()
                .getScene().getStylesheets();
        if(stylesheets.size()!=0)
            alert.getDialogPane().getStylesheets().add(stylesheets.get(0));
        alert.setTitle("Message");
        alert.setContentText(message);
        alert.showAndWait();

    }

    public void setAppController(AppController appController) {
        this.appController = appController;
    }

    public void setRoleAdder(Consumer<String> roleAdder){
        this.roleAdder = roleAdder;
    }

    public void setRoleDeleter(Consumer<String> roleDeleter) {
        this.roleDeleter = roleDeleter;
    }
}
