package controllers.roles;

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

public class RolesController {


    @FXML
    private ListView<String> rolesListView;

    @FXML
    private VBox roleSelectedView;
    private AppController appController;



    @FXML
    public void initialize() {
        rolesListView.setOrientation(Orientation.VERTICAL);
        rolesListView.setOnMouseClicked(e->rowClick(new ActionEvent()));
        rolesListView.getItems().add("role1");
        rolesListView.getItems().add("role2");
    }

    private void rowClick(ActionEvent event) {
        System.out.println("row click");
        if(!rolesListView.getSelectionModel().isEmpty()) {
            roleSelectedView.getChildren().clear();
            addTitleLine(rolesListView.getSelectionModel().getSelectedItem());
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
        roleSelectedView.getChildren().add(hBox);
    }


    @FXML
    void SaveButtonClicked(ActionEvent event) {
        System.out.println("save click");
    }

    @FXML
    void newButtonClicked(ActionEvent event) {
        System.out.println("new click");
    }


    public void setAppController(AppController appController) {
        this.appController = appController;
    }
}
