package com.monochord.chat.client.ui;


import com.monochord.chat.client.ChatClient;
import com.monochord.chat.common.data.ChatRoom;
import com.monochord.chat.client.core.Client;
import com.monochord.chat.common.data.User;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;

import java.io.*;
import java.util.List;
import java.util.stream.Collectors;

class ChatRoomView extends HBox {

    private static final ByteArrayInputStream fxmlStream;
    static {
        try (InputStream in = new BufferedInputStream(
                ChatRoomView.class.getResourceAsStream("ChatRoomView.fxml"))) {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            for (int b; (b = in.read()) != -1; )
                out.write(b);
            fxmlStream = new ByteArrayInputStream(out.toByteArray());
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @FXML private ComboBox<String> toUser;
    @FXML private TextField messageText;
    @FXML private TextArea chatTextArea;
    @FXML private ListView<User> userList;
    @FXML private CheckBox isPrivate;

    private final String roomName;
    private final Client client;
    private final ChatRoomService roomUpdater;
    private final BackgroundExecutor backgroundExecutor;
    private final Tab parentTab;
    private boolean pendingShutdown;


    private ChatRoomView(String roomName, Client client,
                         BackgroundExecutor backgroundExecutor, Tab parentTab) {
        this.roomName = roomName;
        this.client = client;
        this.backgroundExecutor = backgroundExecutor;
        this.parentTab = parentTab;

        roomUpdater = new ChatRoomService(client, roomName);
        roomUpdater.setOnFailed(event -> GuiUtils.handleServiceException(roomUpdater));
        roomUpdater.setOnSucceeded(event -> roomUpdater.getValue().ifPresent(this::update));
        if (parentTab.isSelected())
            roomUpdater.start();

        parentTab.selectedProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.equals(oldValue)) {
                if (newValue)
                    roomUpdater.restart();
                else
                    roomUpdater.cancel();
            }
        });
    }


    static ChatRoomView create(String roomName, Client client,
                               BackgroundExecutor backgroundExecutor, Tab parentTab) {
        assert Platform.isFxApplicationThread();
        assert roomName != null;
        assert client != null;
        assert backgroundExecutor != null;
        assert parentTab != null;

        fxmlStream.reset();
        return GuiUtils.loadFxmlComponent(fxmlStream,
                () -> new ChatRoomView(roomName, client, backgroundExecutor, parentTab));
    }


    String getName() {
        return roomName;
    }


    Tab getParentTab() {
        return parentTab;
    }


    void shutdown() {
        pendingShutdown = true;
        roomUpdater.cancel();
    }


    @FXML
    private void initialize() {
        assert toUser != null : "fx:id=\"toUser\" was not injected: check your FXML file 'ChatRoomView.fxml'.";
        assert messageText != null : "fx:id=\"messageText\" was not injected: check your FXML file 'ChatRoomView.fxml'.";
        assert chatTextArea != null : "fx:id=\"chatTextArea\" was not injected: check your FXML file 'ChatRoomView.fxml'.";
        assert userList != null : "fx:id=\"userList\" was not injected: check your FXML file 'ChatRoomView.fxml'.";
        assert isPrivate != null : "fx:id=\"isPrivate\" was not injected: check your FXML file 'ChatRoomView.fxml'.";

        messageText.addEventHandler(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.ENTER)
                sendMessage();
        });

        userList.setCellFactory(listView -> new ListCell<User>() {
            @Override
            protected void updateItem(User user, boolean empty) {
                super.updateItem(user, empty);
                if (user != null && !empty) {
                    setText(user.toString());
                    setStyle(user.isAway() ? "-fx-font-weight: normal" : "-fx-font-weight: bold");
                }
                else {
                    setText(null);
                    setGraphic(null);
                }
            }
        });
    }


    private void update(ChatRoom chatRoom) {
        chatRoom.getMessageList().getMessages().forEach(
                message -> chatTextArea.appendText(message.toString() + System.lineSeparator()));

        List<User> updatedUsers = chatRoom.getUserList().getUsers().stream()
                .sorted()
                .collect(Collectors.toList());
        ObservableList<User> userListItems = userList.getItems();
        userListItems.setAll(updatedUsers);

        List<String> updatedUserNames = updatedUsers.stream()
                .map(User::getName)
                .collect(Collectors.toList());
        ObservableList<String> toUserItems = toUser.getItems();
        if (!toUserItems.stream()
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList())
                .equals(updatedUserNames)) {
            String selectedVal = toUser.getValue();
            toUserItems.clear();
            toUserItems.add("");
            toUserItems.addAll(updatedUserNames);
            if (toUserItems.contains(selectedVal))
                toUser.setValue(selectedVal);
        }
    }


    @FXML
    private void sendMessage() {
        String msg = messageText.getText().trim();
        if (msg.isEmpty())
            return;
        String to = toUser.getValue();
        boolean isPm = isPrivate.isSelected();

        backgroundExecutor.exec(
                () -> client.sendMessage(roomName, to, msg, isPm),
                result -> messageText.clear(),
                exception -> {
                    if (!pendingShutdown)
                        GuiUtils.exceptionAlert(
                                GuiUtils.maskExecutionException(exception),
                                ChatClient.getPrimaryStage());
                });
    }


    @FXML
    private void onToUserChange() {
        String toUserStr = toUser.getValue();
        if (toUserStr == null || toUserStr.isEmpty()) {
            isPrivate.setSelected(false);
            isPrivate.setDisable(true);
        }
        else
            isPrivate.setDisable(false);
    }

}
