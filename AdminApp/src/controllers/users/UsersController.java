package controllers.users;

import controllers.AppController;
import dto.UserInfoDTO;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldListCell;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import okhttp3.*;
import utils.Constants;
import utils.HttpClientUtil;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class UsersController {
    @FXML
    private ListView<UserInfoDTO> usersListView;
    @FXML
    private VBox userSelectedView;
    private AppController appController;
    private Timer timer;
    private List<CheckBox> checkBoxes;
    private CheckBox checkBoxManager;
    private String userName;
    private Stage primaryStage;
    private boolean selectionListenerEnabled = true;
    private HBox firstLine;
    private HBox secondLine;

    @FXML
    public void initialize() {
        usersListView.setOrientation(Orientation.VERTICAL);
        Label placeholderLabel = new Label("No users in the system");
        usersListView.setPlaceholder(placeholderLabel);
        usersListView.setCellFactory(listView -> new TextFieldListCell<UserInfoDTO>() {
            @Override
            public void updateItem(UserInfoDTO userInfoDTO, boolean empty) {
                super.updateItem(userInfoDTO, empty);
                if (empty || userInfoDTO == null) {
                    setText(null);
                } else {
                    setText(userInfoDTO.getUserDetailsDTO().getUserName());
                }
            }
        });

        checkBoxes=new ArrayList<>();
        checkBoxManager=new CheckBox("Manager");

        usersListView.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (selectionListenerEnabled) {
                if (newSelection != null)
                    rowClick();
            }
        });
    }

    @FXML
    private void SaveButtonClicked(ActionEvent event) {
        Set<String> rolesChoice=checkBoxes.stream()
                .filter(CheckBox::isSelected)
                .map(Labeled::getText)
                .collect(Collectors.toSet());

        if (checkBoxManager.isSelected()) {
            rolesChoice.add("Manager");
        }



        String RESOURCE="/user-roles";

        String finalUrl = HttpUrl
                .parse(Constants.FULL_SERVER_PATH + RESOURCE)
                .newBuilder()
                .addQueryParameter("userName", userName)
                .build()
                .toString();


        RequestBody requestBody =new FormBody.Builder()
                .add("roles",Constants.GSON_INSTANCE.toJson(rolesChoice))
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
                        Alert alert =new Alert(Alert.AlertType.INFORMATION);
                        ObservableList<String> stylesheets = primaryStage.getScene().getStylesheets();
                        if(stylesheets.size()!=0)
                            alert.getDialogPane().getStylesheets().add(stylesheets.get(0));


                        alert.setTitle("Message");
                        alert.setContentText("the changes were updated successfully");
                        alert.showAndWait();

                    });
                }
                else
                    HttpClientUtil.errorMessage(response.body(),appController);

            }
        });


    }

    public void updateUsersList(List<UserInfoDTO> usersFromRequest)
    {
        if(usersFromRequest!=null) {
            Platform.runLater(() -> {
                selectionListenerEnabled = false;
                UserInfoDTO selectedUser=usersListView.getSelectionModel().getSelectedItem();
                String selectedUserName=null;
                if(selectedUser!=null)
                    selectedUserName=selectedUser.getUserDetailsDTO().getUserName();

                Collection<UserInfoDTO> usersFromList=usersListView.getItems();

                if(!usersFromList.isEmpty())
                    usersFromList.clear();
                usersFromList.addAll(usersFromRequest);

                if(selectedUserName!=null){
                    int index=0,size=usersFromList.size();
                    boolean found=false;
                    for(UserInfoDTO user:usersFromList) {
                        if(user.getUserDetailsDTO().getUserName().equals(selectedUserName)) {
                            usersListView.getSelectionModel().select(index);
                            found=true;
                            updateTwoFirstLines(user);
                        }
                        index++;
                    }
                    if(!found){
                        userSelectedView.getChildren().clear();
                        if(appController.CurrentTab()!=null&&appController.CurrentTab()==3) {
                            Alert alert = new Alert(Alert.AlertType.INFORMATION);
                            alert.setTitle("Message");
                            if (primaryStage.getScene().getStylesheets().size() != 0)
                                alert.getDialogPane().getStylesheets().add(primaryStage.getScene().getStylesheets().get(0));
                            alert.setContentText("The user: " + selectedUserName + " has logged out of the system");
                            alert.showAndWait();
                        }
                    }
                }
                selectionListenerEnabled = true;

            });
        }
    }

    private void updateTwoFirstLines(UserInfoDTO userInfo){
        firstLine.getChildren().remove(1);
        firstLine.getChildren().add(new Label(userInfo.getNumOfDefinedFlows().toString()));
        secondLine.getChildren().remove(1);
        secondLine.getChildren().add(new Label(userInfo.getNumOfFlowsPerformed().toString()));
    }

    public void StartUsersRefresher()
    {
        TimerTask usersRefresher=new UsersRefresher(this::updateUsersList,appController);

        timer = new Timer();
        timer.schedule(usersRefresher, 200, 2000);
    }


    public void StopUsersRefresher()
    {
        timer.cancel();
    }

    public void addRole(String roleName){
        CheckBox checkBox=new CheckBox(roleName);
        checkBox.setSelected(false);
        checkBoxes.add(checkBox);
        if(!usersListView.getSelectionModel().isEmpty()){
            addCheckBox(checkBox);
        }
    }

    public void removeRole(String roleName){
        Optional<CheckBox> res=checkBoxes
                .stream()
                .filter(checkBox -> checkBox.getText().equals(roleName))
                .findFirst();

        res.ifPresent(checkBox -> {
            checkBoxes.remove(checkBox);
            if(userSelectedView.getChildren()!=null){
                userSelectedView.getChildren().remove(checkBox);
            }
        });

    }

    public void setRolesOptions(Set<String> roles){
        if(roles!=null)
            roles.forEach(role->checkBoxes.add(new CheckBox(role)));
    }


    public void setStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }

    private void rowClick() {
        if(!usersListView.getSelectionModel().isEmpty()) {
            userSelectedView.getChildren().clear();
            UserInfoDTO userInfoDTO=usersListView.getSelectionModel().getSelectedItem();
            userName=userInfoDTO.getUserDetailsDTO().getUserName();
            addTitleLine(userName);
            firstLine=addKeyValueLine("Number of flows that the user can run: "
                    , userInfoDTO.getNumOfDefinedFlows().toString());
            secondLine=addKeyValueLine("Number of flows that had been executed by the user: "
                    ,userInfoDTO.getNumOfFlowsPerformed().toString());

            addTitleLine("MANAGER:");
            checkBoxManager.setSelected(userInfoDTO.getUserDetailsDTO().getManager());
            addCheckBox(checkBoxManager);

            addTitleLine("ROLES:");

            for(CheckBox checkBox :checkBoxes){
                checkBox.setSelected(userInfoDTO.getRoles().contains(checkBox.getText()));
                addCheckBox(checkBox);
            }

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

    private void addTitleLine(String title)
    {
        HBox hBox= getNewHbox();
        Label label = new Label(title);
        label.setFont(Font.font("System", FontWeight.BOLD,14));
        hBox.getChildren().add(label);
        userSelectedView.getChildren().add(hBox);
    }

    private HBox addKeyValueLine(String name, String value)
    {
        HBox hBox = getNewHbox();

        Label key =new Label(name);
        key.setFont(Font.font("System", FontWeight.BOLD,12));

        Label data =new Label(value);
        data.setAlignment(Pos.TOP_LEFT);

        hBox.getChildren().add(key);
        hBox.getChildren().add(data);

        userSelectedView.getChildren().add(hBox);

        return  hBox;
    }

    private void addCheckBox(CheckBox checkBox)
    {
        userSelectedView.getChildren().add(checkBox);
        VBox.setMargin(checkBox, new Insets(10, 0, 0, 0));
    }

    public void setAppController(AppController appController) {
        this.appController = appController;
    }

}
