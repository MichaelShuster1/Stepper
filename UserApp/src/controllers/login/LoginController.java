package controllers.login;

import controllers.AppController;
import enginemanager.EngineApi;
import enginemanager.Manager;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.stage.Screen;
import javafx.stage.Stage;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;
import progress.ProgressTracker;
import utils.Constants;
import utils.CookieManager;
import utils.HttpClientUtil;

import java.io.IOException;
import java.net.URL;

public class LoginController {


    @FXML
    private TextField userNameField;

    @FXML
    private Button loginButton;

    @FXML
    private Label errorLabel;

    private EngineApi engine;

    private Stage primaryStage;

    private final StringProperty errorMessageProperty = new SimpleStringProperty();



    public void setEngine(EngineApi engine) {
        this.engine = engine;
    }

    @FXML
    public void initialize() {
        loginButton.disableProperty().bind(userNameField.textProperty().isEmpty());
        errorLabel.textProperty().bind(errorMessageProperty);
    }


    public void setModel(Manager manager) {
        this.engine = manager;
    }

    public void setPrimaryStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }


    @FXML
    void userLogin(ActionEvent event) {
        String userName = userNameField.getText();
        String finalUrl = HttpUrl
                .parse(Constants.LOGIN_PAGE)
                .newBuilder()
                .addQueryParameter("username", userName)
                .build()
                .toString();

        HttpClientUtil.runAsync(finalUrl, new Callback() {

            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                Platform.runLater(() ->
                        errorMessageProperty.set("Something went wrong: " + e.getMessage())
                );
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if (response.code() != 200) {
                    String responseBody = response.body().string();
                    Platform.runLater(() -> {
                        if(response.code() == 409)
                          errorMessageProperty.set("Something went wrong, please try again.");
                        else {
                            errorMessageProperty.set(responseBody);
                        }

                    });
                } else {
                    Platform.runLater(() -> {
                        showMainScreen(userName);
                    });
                }

            }
        });
    }

    public void showMainScreen(String userName) {
        try {
            URL resource = getClass().getResource("/resources/fxml/MainScreen.fxml");
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(resource);
            Parent root = loader.load(resource.openStream());
            AppController controller = loader.getController();
            controller.setModel(new Manager());
            Stage mainStage = new Stage();
            controller.setPrimaryStage(mainStage);


            Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
            double widthFraction = 0.8;
            double heightFraction = 0.8;
            double desiredWidth = screenBounds.getWidth() * widthFraction;
            double desiredHeight = screenBounds.getHeight() * heightFraction;

            Scene scene = new Scene(root, desiredWidth, desiredHeight);
            Image icon = new Image(getClass().getResource("/resources/pictures/Icon.png").toExternalForm());
            scene.getStylesheets().add(getClass().getResource("/resources/css/Default.css").toExternalForm());
            mainStage.getIcons().add(icon);
            mainStage.setTitle("Stepper");
            mainStage.setScene(scene);
            controller.setUserName(userName);
            controller.setFlowRefreshActive();
            mainStage.show();

            primaryStage.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}
