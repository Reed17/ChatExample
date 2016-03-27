package com.monochord.chat.client.ui;


import com.monochord.chat.common.data.ChatRoom;
import com.monochord.chat.client.core.Client;
import javafx.concurrent.ScheduledService;
import javafx.concurrent.Task;
import javafx.util.Duration;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

class ChatRoomService extends ScheduledService<Optional<ChatRoom>> {

    private final Client client;
    private final String roomName;
    private final AtomicInteger messageCounter = new AtomicInteger();


    ChatRoomService(Client client, String roomName) {
        this.client = client;
        this.roomName = roomName;

        setDelay(Duration.millis(1000));
        setPeriod(Duration.millis(1500));
        setRestartOnFailure(true);
    }


    @Override
    protected Task<Optional<ChatRoom>> createTask() {
        return new Task<Optional<ChatRoom>>() {
            @Override
            protected Optional<ChatRoom> call() throws Exception {
                if (client.isLoggedIn()) {
                    ChatRoom chatRoom = client.getChatRoom(roomName, messageCounter.get());
                    messageCounter.addAndGet(
                            chatRoom.getMessageList().getCount() + chatRoom.getHiddenMessagesCount());
                    return Optional.of(chatRoom);
                }
                else
                    return Optional.empty();
            }
        };
    }
}
