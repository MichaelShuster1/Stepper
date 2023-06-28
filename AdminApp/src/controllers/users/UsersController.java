package controllers.users;

import controllers.AppController;
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

public class UsersController {
    @FXML
    private ListView<String> usersListView;

    @FXML
    private VBox userSelectedView;


    private AppController appController;

    @FXML
    public void initialize() {
        usersListView.setOrientation(Orientation.VERTICAL);
        usersListView.setOnMouseClicked(e->rowClick(new ActionEvent()));
        usersListView.getItems().add("user1");
        usersListView.getItems().add("user2");
    }

    private void rowClick(ActionEvent event) {
        System.out.println("row click");
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

    @FXML
    void SaveButtonClicked(ActionEvent event) {
        System.out.println("save click");
    }
}
