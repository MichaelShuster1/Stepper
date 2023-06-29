package controllers.users;

import controllers.AppController;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.util.Collection;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class UsersController {
    @FXML
    private ListView<String> usersListView;

    @FXML
    private VBox userSelectedView;


    private AppController appController;

    private Timer timer;

    @FXML
    public void initialize() {
        usersListView.setOrientation(Orientation.VERTICAL);
        Label placeholderLabel = new Label("No users in the system");
        usersListView.setPlaceholder(placeholderLabel);
        usersListView.setOnMouseClicked(e->rowClick(new ActionEvent()));
    }

    @FXML
    private void SaveButtonClicked(ActionEvent event) {
        System.out.println("save click");
    }

    public void updateUsersList(List<String> usersName)
    {
        if(usersName!=null) {
            Platform.runLater(() -> {
                Collection<String> users = usersListView.getItems();
                for (String userName : usersName) {
                    if (!users.contains(userName))
                        users.add(userName);
                }
            });
        }
    }

    public void StartUsersRefresher()
    {
        TimerTask usersRefresher=new UsersRefresher(this::updateUsersList);

        timer = new Timer();
        timer.schedule(usersRefresher, 200, 2000);
    }


    public void StopUsersRefresher()
    {
        timer.cancel();
    }



    private void rowClick(ActionEvent event) {
        if(!usersListView.getSelectionModel().isEmpty()) {
            userSelectedView.getChildren().clear();
            addTitleLine(usersListView.getSelectionModel().getSelectedItem());
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

    public void setAppController(AppController appController) {
        this.appController = appController;
    }

}
