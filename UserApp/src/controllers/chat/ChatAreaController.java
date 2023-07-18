package controllers.chat;

import controllers.AppController;
import controllers.chat.model.ChatLinesWithVersion;
import controllers.flowexecution.ExecutionController;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.control.ToggleButton;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;
import utils.Constants;
import utils.HttpClientUtil;

import java.io.IOException;
import java.util.Timer;
import java.util.stream.Collectors;

import static utils.Constants.CHAT_LINE_FORMATTING;
import static utils.Constants.REFRESH_RATE;


public class ChatAreaController {

    @FXML
    private ToggleButton autoScrollButton;
    @FXML
    private TextArea chatLineTextArea;
    @FXML
    private TextArea mainChatLinesTextArea;

    private AppController appController;

    private final IntegerProperty chatVersion;
    private final BooleanProperty autoScroll;
    private ChatAreaRefresher chatAreaRefresher;
    private Timer timer;



    public ChatAreaController() {
        chatVersion = new SimpleIntegerProperty();
        autoScroll = new SimpleBooleanProperty();
    }

    @FXML
    public void initialize() {
        autoScroll.bind(autoScrollButton.selectedProperty());
        mainChatLinesTextArea.setWrapText(true);

        chatLineTextArea.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.ENTER) {
                event.consume(); // Prevent the default behavior of adding a new line
                // Custom action to perform when Enter key is pressed
                sendButtonClicked(new ActionEvent());
            }
        });
    }

    @FXML
    void sendButtonClicked(ActionEvent event) {
        String chatLine = chatLineTextArea.getText();
        String finalUrl = HttpUrl
                .parse(Constants.FULL_SERVER_PATH + "/sendChat")
                .newBuilder()
                .addQueryParameter("userstring", chatLine)
                .build()
                .toString();


        HttpClientUtil.runAsync(finalUrl, new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                System.out.println("there was a problem with sending the message");
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if(response.code()!=200){
                    System.out.println("there was a problem with sending the message");
                }

                if(response.body()!=null)
                    response.body().close();
            }
        });

        chatLineTextArea.clear();
    }


    @FXML
    void closeChatCLick(ActionEvent event) {
        close();
        appController.closeChat();
    }

    public void setAppController(AppController appController) {
        this.appController = appController;
    }

    private void updateChatLines(ChatLinesWithVersion chatLinesWithVersion) {
        if (chatLinesWithVersion.getVersion() != chatVersion.get()) {
            String deltaChatLines = chatLinesWithVersion
                    .getEntries()
                    .stream()
                    .map(singleChatLine -> {
                        long time = singleChatLine.getTime();
                        return String.format(CHAT_LINE_FORMATTING, time, time, time, singleChatLine.getUsername(), singleChatLine.getChatString());
                    }).collect(Collectors.joining());

            Platform.runLater(() -> {
                chatVersion.set(chatLinesWithVersion.getVersion());

                if (autoScroll.get()) {
                    mainChatLinesTextArea.appendText(deltaChatLines);
                    mainChatLinesTextArea.selectPositionCaret(mainChatLinesTextArea.getLength());
                    mainChatLinesTextArea.deselect();
                } else {
                    int originalCaretPosition = mainChatLinesTextArea.getCaretPosition();
                    mainChatLinesTextArea.appendText(deltaChatLines);
                    mainChatLinesTextArea.positionCaret(originalCaretPosition);
                }
            });
        }
    }

    public void startListRefresher() {
        if(chatAreaRefresher==null&&timer==null) {
            chatAreaRefresher = new ChatAreaRefresher(chatVersion, this::updateChatLines);
            timer = new Timer();
            timer.schedule(chatAreaRefresher, 0, REFRESH_RATE);
        }
    }


    public void close()  {
        //chatVersion.set(0);
        //chatLineTextArea.clear();
        if (chatAreaRefresher != null && timer != null) {
            chatAreaRefresher.cancel();
            timer.cancel();
            chatAreaRefresher=null;
            timer=null;
        }
    }

}