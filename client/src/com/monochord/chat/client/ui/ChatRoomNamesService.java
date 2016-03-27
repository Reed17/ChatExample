package com.monochord.chat.client.ui;

import com.monochord.chat.common.data.ChatRoomNames;
import com.monochord.chat.client.core.Client;
import javafx.concurrent.ScheduledService;
import javafx.concurrent.Task;
import javafx.util.Duration;

import java.util.Optional;

class ChatRoomNamesService extends ScheduledService<Optional<ChatRoomNames>> {

    private final Client client;


    ChatRoomNamesService(Client client) {
        this.client = client;

        setDelay(Duration.millis(500));
        setPeriod(Duration.millis(3000));
        setRestartOnFailure(true);
    }


    @Override
    protected Task<Optional<ChatRoomNames>> createTask() {
        return new Task<Optional<ChatRoomNames>>() {
            @Override
            protected Optional<ChatRoomNames> call() throws Exception {
                return client.isLoggedIn()
                        ? Optional.of(client.getChatRoomNames())
                        : Optional.empty();
            }
        };
    }

}
