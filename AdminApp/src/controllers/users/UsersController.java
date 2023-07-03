package controllers.users;

import controllers.AppController;
import dto.UserInfoDTO;
import javafx.application.Platform;
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
                    setText(userInfoDTO.getName());
                }
            }
        });

        usersListView.setOnMouseClicked(e->rowClick(new ActionEvent()));
        checkBoxes=new ArrayList<>();
        checkBoxes.add(new CheckBox("Read Only Flows"));
        checkBoxes.add(new CheckBox("All Flows"));
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

        System.out.println(rolesChoice);


        String RESOURCE="/roles";

        String finalUrl = HttpUrl
                .parse(Constants.FULL_SERVER_PATH + RESOURCE)
                .newBuilder()
                .addQueryParameter("userName", userName)
                .build()
                .toString();

        MediaType mediaType = MediaType.parse("application/json");
        RequestBody requestBody=RequestBody.create(mediaType, Constants.GSON_INSTANCE.toJson(rolesChoice));

        HttpClientUtil.runAsyncPost(finalUrl,requestBody,new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                HttpClientUtil.showErrorAlert(Constants.CONNECTION_ERROR,appController);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if(response.code()!=200){
                    HttpClientUtil.errorMessage(response.body(),appController);
                }
            }
        });


    }

    public void updateUsersList(List<UserInfoDTO> usersFromRequest)
    {
        if(usersFromRequest!=null) {
            Platform.runLater(() -> {
                UserInfoDTO selectedUser=usersListView.getSelectionModel().getSelectedItem();
                String selectedUserName=null;
                if(selectedUser!=null)
                    selectedUserName=selectedUser.getName();

                Collection<UserInfoDTO> usersFromList=usersListView.getItems();

                if(!usersFromList.isEmpty())
                    usersFromList.clear();
                usersFromList.addAll(usersFromRequest);

                if(selectedUserName!=null){
                    int index=0,size=usersFromList.size();
                    for(UserInfoDTO user:usersFromList) {
                        if(user.getName().equals(selectedUserName)) {
                            usersListView.getSelectionModel().select(index);
                            //rowClick(new ActionEvent());
                        }
                        index++;
                    }
                }

            });
        }
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
        checkBoxes.add(new CheckBox(roleName));
    }



    private void rowClick(ActionEvent event) {
        if(!usersListView.getSelectionModel().isEmpty()) {
            userSelectedView.getChildren().clear();
            UserInfoDTO userInfoDTO=usersListView.getSelectionModel().getSelectedItem();
            userName=userInfoDTO.getName();
            addTitleLine(userName);
            addKeyValueLine("Number of flows that the user can run: "
                    , userInfoDTO.getNumOfDefinedFlows().toString());
            addKeyValueLine("Number of flows that had been executed by the user: "
                    ,userInfoDTO.getNumOfFlowsPerformed().toString());

            addTitleLine("MANAGER:");
            checkBoxManager.setSelected(userInfoDTO.getManager());
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

    private void addKeyValueLine(String name, String value)
    {
        HBox hBox = getNewHbox();

        Label key =new Label(name);
        key.setFont(Font.font("System", FontWeight.BOLD,12));

        Label data =new Label(value);
        data.setAlignment(Pos.TOP_LEFT);

        hBox.getChildren().add(key);
        hBox.getChildren().add(data);

        userSelectedView.getChildren().add(hBox);
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
