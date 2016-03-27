package com.monochord.chat.server;


import com.monochord.chat.common.data.ChatRoom;
import com.monochord.chat.common.data.ChatRoomNames;
import com.monochord.chat.common.data.Message;
import com.monochord.chat.common.data.User;

import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

class ChatRoomList {

    private final static ChatRoomList INSTANCE = new ChatRoomList();

    private final ConcurrentHashMap<String, ChatRoom> chatRooms = new ConcurrentHashMap<>();


    private ChatRoomList() {
        String defaultName = ChatRoom.DEFAULT_CHAT_ROOM_NAME;
        chatRooms.put(defaultName, new ChatRoom(defaultName));
    }


    public static ChatRoomList getInstance() {
        return INSTANCE;
    }


    public ChatRoom getChatRoom(String roomName) {
        return chatRooms.get(roomName);
    }


    public boolean addChatRoom(String roomName) {
        if (chatRooms.containsKey(roomName))
            return false;
        chatRooms.computeIfAbsent(roomName, ChatRoom::new);
        return true;
    }


    public boolean addMessage(Message message) {
        ChatRoom chatRoom = chatRooms.get(message.getRoomName());
        if (chatRoom == null)
            return false;
        chatRoom.getMessageList().getSynchronizedMessages().add(message);
        return true;
    }


    public boolean addUser(String roomName, User user) {
        Objects.requireNonNull(user);
        ChatRoom chatRoom = chatRooms.get(roomName);
        return chatRoom != null && chatRoom.getUserList().getSynchronizedUsers().add(user);
    }


    public boolean removeUser(String roomName, User user) {
        Objects.requireNonNull(user);
        ChatRoom chatRoom = chatRooms.get(roomName);
        return chatRoom != null && chatRoom.getUserList().getSynchronizedUsers().remove(user);
    }


    public boolean removeUser(User user) {
        Objects.requireNonNull(user);
        chatRooms.forEachValue(Long.MAX_VALUE, room -> room.getUserList().getSynchronizedUsers().remove(user));
        return true;
    }


    public ChatRoomNames getChatRoomNames() {
        ChatRoomNames roomNames = new ChatRoomNames();
        roomNames.getRoomNames().addAll(chatRooms.keySet());
        return roomNames;
    }

}
