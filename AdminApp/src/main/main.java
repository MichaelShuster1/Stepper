package main;

import controllers.AppController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.net.URL;

public class main extends Application {
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        URL resource =getClass().getResource("/resources/fxml/MainScreen.fxml");
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(resource);
        Parent root = loader.load(resource.openStream());
        AppController controller = loader.getController();
        controller.setPrimaryStage(primaryStage);


        Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
        double widthFraction = 0.8;
        double heightFraction = 0.8;
        double desiredWidth = screenBounds.getWidth() * widthFraction;
        double desiredHeight = screenBounds.getHeight() * heightFraction;

        Scene scene = new Scene(root,desiredWidth,desiredHeight);
        scene.getStylesheets().add(getClass().getResource("/resources/css/Default.css").toExternalForm());
        primaryStage.setTitle("Stepper");
        primaryStage.setScene(scene);
        primaryStage.show();

    }


}
