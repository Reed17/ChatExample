package com.monochord.chat.client.ui;


import com.monochord.chat.client.ChatClient;
import com.monochord.chat.client.core.Client;
import com.monochord.chat.client.core.ClientException;
import com.monochord.chat.common.data.ChatRoom;
import com.monochord.chat.common.data.ChatRoomNames;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.text.Text;
import javafx.util.Callback;

import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class MainForm extends BorderPane {

    @FXML private TabPane chatRoomsTabPane;
    @FXML private Button logInOutButton;
    @FXML private Button newRoomButton;
    @FXML private ListView<String> roomList;
    @FXML private RadioButton online;
    @FXML private RadioButton away;
    @FXML private Text statusBar;

    private final Client client = new Client();
    private final BackgroundExecutor backgroundExecutor = new BackgroundExecutor();
    private final ChatRoomNamesService roomNamesUpdater;

    private ChatRoomViews chatRoomViews;


    private class ChatRoomViews implements Iterable<ChatRoomView> {

        private final ObservableList<Tab> tabs;

        ChatRoomViews() {
            tabs = chatRoomsTabPane.getTabs();

            String defaultName = ChatRoom.DEFAULT_CHAT_ROOM_NAME;
            Tab tab = tabs.get(0);
            tab.setText(defaultName);
            tab.setContent(ChatRoomView.create(defaultName, client, backgroundExecutor, tab));
        }


        void add(String roomName) {
            Tab tab = new Tab(roomName);
            tab.setContent(ChatRoomView.create(roomName, client, backgroundExecutor, tab));
            tab.setOnClosed(event -> backgroundExecutor.exec(
                    () -> {
                        if (client.isLoggedIn())
                            client.leaveChatRoom(roomName);
                    },
                    null,
                    MainForm.this::onFailure));

            tabs.add(tab);
            chatRoomsTabPane.getSelectionModel().select(tab);
        }


        ChatRoomView find(String roomName) {
            return StreamSupport.stream(spliterator(), false)
                    .filter(roomView -> roomName.equals(roomView.getName()))
                    .findFirst()
                    .orElse(null);
        }


        void select(ChatRoomView chatRoomView) {
            chatRoomsTabPane.getSelectionModel().select(chatRoomView.getParentTab());
        }


        void closeAll() {
            for (Iterator<Tab> itr = tabs.iterator(); itr.hasNext(); ) {
                Tab tab = itr.next();
                if (tab.isClosable())
                    itr.remove();
            }
        }


        ChatRoomView getSelected() {
            return tabs.stream()
                    .filter(Tab::isSelected)
                    .findFirst()
                    .map(this::roomFromTab)
                    .orElseThrow(AssertionError::new);
        }


        @Override
        public Iterator<ChatRoomView> iterator() {
            return tabs.stream().map(this::roomFromTab).iterator();
        }


        private ChatRoomView roomFromTab(Tab tab) {
            return (ChatRoomView)tab.getContent();
        }

    }


    private class ChatRoomListCellFactory implements Callback<ListView<String>, ListCell<String>> {

        private final ContextMenu menu;


        ChatRoomListCellFactory() {
            MenuItem join = new MenuItem("Join");
            join.setOnAction(event -> {
                String roomName = roomList.getSelectionModel().getSelectedItem();
                ChatRoomView roomView = chatRoomViews.find(roomName);
                if (roomView == null) {
                    backgroundExecutor.exec(
                            () -> client.joinChatRoom(roomName),
                            result -> chatRoomViews.add(roomName),
                            MainForm.this::onFailure
                    );
                }
                else
                    chatRoomViews.select(roomView);
            });
            menu = new ContextMenu(join);
        }


        @Override
        public ListCell<String> call(ListView<String> listView) {
            return new ListCell<String>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (item != null && !empty) {
                        setText(item);
                        setContextMenu(menu);
                    }
                    else {
                        setText(null);
                        setGraphic(null);
                    }
                }
            };
        }

    }


    private MainForm() {
        roomNamesUpdater = new ChatRoomNamesService(client);
        roomNamesUpdater.setOnFailed(event -> GuiUtils.handleServiceException(roomNamesUpdater));
        roomNamesUpdater.setOnSucceeded(event ->
                roomNamesUpdater.getValue().ifPresent(this::updateRoomNames));
    }


    public static MainForm create() {
        if (!Platform.isFxApplicationThread())
            throw new IllegalStateException("This component can be created only from JavaFX Application Thread");

        return GuiUtils.loadFxmlComponent(MainForm.class.getResource("MainForm.fxml"), MainForm::new);
    }


    public void shutdown() {
        chatRoomViews.forEach(ChatRoomView::shutdown);
        backgroundExecutor.shutdown();
        roomNamesUpdater.cancel();
        if (client.isLoggedIn()) {
            try {
                client.logOut();
            }
            catch (ClientException e) {
                e.printStackTrace();
            }
        }
    }


    @FXML
    private void initialize() {
        assert chatRoomsTabPane != null : "fx:id=\"chatRoomsTabPane\" was not injected: check your FXML file 'MainForm.fxml'.";
        assert logInOutButton != null : "fx:id=\"logInOutButton\" was not injected: check your FXML file 'MainForm.fxml'.";
        assert newRoomButton != null : "fx:id=\"newRoomButton\" was not injected: check your FXML file 'MainForm.fxml'.";
        assert roomList != null : "fx:id=\"roomList\" was not injected: check your FXML file 'MainForm.fxml'.";
        assert online != null : "fx:id=\"online\" was not injected: check your FXML file 'MainForm.fxml'.";
        assert away != null : "fx:id=\"away\" was not injected: check your FXML file 'MainForm.fxml'.";
        assert statusBar != null : "fx:id=\"statusBar\" was not injected: check your FXML file 'MainForm.fxml'.";

        chatRoomViews = new ChatRoomViews();
        roomList.setCellFactory(new ChatRoomListCellFactory());
        disableControls(true);
    }


    @FXML
    private void logInOut() {
        if (client.isLoggedIn()) {
            backgroundExecutor.exec(
                    client::logOut,
                    result -> {
                        logInOutButton.setText("Log In...");
                        chatRoomViews.closeAll();
                        roomList.getItems().clear();
                        roomNamesUpdater.cancel();
                        disableControls(true);
                        statusBar.setText("Logged out");
                    },
                    this::onFailure
            );
        }
        else {
            GuiUtils.logInDialog(ChatClient.getPrimaryStage()).ifPresent(credentials -> {
                credentials.setAway(away.isSelected());
                backgroundExecutor.exec(
                        () -> client.logIn(credentials),
                        result -> {
                            logInOutButton.setText("Log Out");
                            disableControls(false);
                            roomNamesUpdater.restart();
                            String status = "Logged in as " + credentials.getLogin()
                                    + ". Chat server: " + credentials.getServer();
                            statusBar.setText(status);
                        },
                        this::onFailure
                );
            });
        }
    }


    @FXML
    private void newRoom() {
        TextInputDialog input = new TextInputDialog();
        input.initOwner(ChatClient.getPrimaryStage());
        input.setTitle("New Chat Room");
        input.setHeaderText("Enter room name:");
        input.setGraphic(null);

        input.showAndWait().map(String::trim).ifPresent(roomName -> {
            if (!roomName.isEmpty()) {
                ChatRoomView roomView = chatRoomViews.find(roomName);
                if (roomView != null) {
                    chatRoomViews.select(roomView);
                    return;
                }
                backgroundExecutor.exec(
                        () -> client.createChatRoom(roomName),
                        result -> chatRoomViews.add(roomName),
                        this::onFailure);
            }
        });
    }


    @FXML
    private void onStatusChange() {
        boolean isAway = away.isSelected();
        // TODO change status (radio button) only on success
        backgroundExecutor.exec(() -> client.setAway(isAway), null, this::onFailure);
    }


    private void updateRoomNames(ChatRoomNames chatRoomNames) {
        List<String> roomNames = chatRoomNames.getRoomNames().stream()
                .sorted()
                .collect(Collectors.toList());
        ObservableList<String> roomListItems = roomList.getItems();
        if (!roomNames.equals(roomListItems))
            roomListItems.setAll(roomNames);
    }


    private void disableControls(boolean value) {
        Platform.runLater(() -> {
            chatRoomsTabPane.setDisable(value);
            roomList.setDisable(value);
            online.setDisable(value);
            away.setDisable(value);
            newRoomButton.setDisable(value);
        });
    }


    private void onFailure(Throwable ex) {
        GuiUtils.exceptionAlert(GuiUtils.maskExecutionException(ex), ChatClient.getPrimaryStage());
    }

}
