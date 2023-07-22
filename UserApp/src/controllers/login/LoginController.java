package controllers.login;

import controllers.AppController;
import dto.ResultDTO;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
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


    private Stage primaryStage;

    private final StringProperty errorMessageProperty = new SimpleStringProperty();




    @FXML
    public void initialize() {
        loginButton.disableProperty().bind(userNameField.textProperty().isEmpty());
        errorLabel.textProperty().bind(errorMessageProperty);


        userNameField.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.ENTER) {
                event.consume(); // Prevent the default behavior of adding a new line
                // Custom action to perform when Enter key is pressed
                userLogin(new ActionEvent());
            }
        });
    }


    public void setPrimaryStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
        primaryStage.setOnCloseRequest(event -> {
            event.consume();
            primaryStage.close();
            HttpClientUtil.shutdown();
        });
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
                        errorMessageProperty.set(Constants.SOMETHING_WRONG + e.getMessage())
                );
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if (response.code() != 200) {
                    String responseBody = response.body().string();
                    try {
                        ResultDTO resultDTO = Constants.GSON_INSTANCE.fromJson(responseBody, ResultDTO.class);
                        Platform.runLater(() -> {
                            errorMessageProperty.set(resultDTO.getMessage());
                        });
                    }
                    catch (Exception e) {
                        Platform.runLater(() -> {
                            errorMessageProperty.set(Constants.SOMETHING_WRONG + "please try again");
                        });
                    }
                }
                else {
                    Platform.runLater(() -> {
                        showMainScreen(userName);
                    });
                }
                if(response.body() != null)
                    response.body().close();

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
            controller.setModel();
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
            controller.startUpdatesRefresher();
            mainStage.show();

            primaryStage.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}
