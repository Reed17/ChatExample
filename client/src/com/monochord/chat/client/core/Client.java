package com.monochord.chat.client.core;


import com.monochord.chat.common.data.ChatRoom;
import com.monochord.chat.common.data.ChatRoomNames;
import com.monochord.chat.common.data.Credentials;
import com.monochord.chat.common.data.Message;
import com.monochord.chat.common.util.Marshalling;
import com.monochord.chat.client.core.Request.Method;

import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalTime;
import java.util.Objects;

public final class Client {

    private boolean isLoggedIn;
    private Credentials credentials;
    private String sessionCookie;


    public boolean isLoggedIn() {
        return isLoggedIn;
    }


    public synchronized void logIn(Credentials credentials) {
        if (isLoggedIn)
            return;
        this.credentials = Objects.requireNonNull(credentials);

        String postData;
        try {
            postData = Marshalling.marshal(credentials);
        }
        catch (JAXBException e) {
            throw new ClientException(e);
        }

        String url = credentials.getServer() + "user";
        try (Request request = new Request(url, Method.POST, postData, null)) {
            perform(request);
            sessionCookie = request.getSessionCookie();
            if (sessionCookie == null || sessionCookie.isEmpty())
                throw new ClientException("Session cookie was not received");
        }
        isLoggedIn = true;
    }


    public synchronized void logOut() {
        if (!isLoggedIn)
            return;

        String url = credentials.getServer() + "user?action=logout";
        try (Request request = new Request(url, Method.GET, null, sessionCookie)) {
            perform(request);
        }
        sessionCookie = null;
        isLoggedIn = false;
    }


    public void setAway(boolean isAway) {
        checkLoggedIn();

        String url = credentials.getServer() + "user?action=away&value=" + Boolean.toString(isAway);
        try (Request request = new Request(url, Method.GET, null, sessionCookie)) {
            perform(request);
        }
    }


    public void createChatRoom(String roomName) {
        checkLoggedIn();
        Objects.requireNonNull(roomName);

        String url = encodeURL(credentials.getServer() + "room?action=new&name=" + roomName);
        try (Request request = new Request(url, Method.GET, null, sessionCookie)) {
            perform(request);
        }
    }


    public void joinChatRoom(String roomName) {
        checkLoggedIn();
        Objects.requireNonNull(roomName);

        String url = encodeURL(credentials.getServer() + "room?action=join&name=" + roomName);
        try (Request request = new Request(url, Method.GET, null, sessionCookie)) {
            perform(request);
        }
    }


    public void leaveChatRoom(String roomName) {
        checkLoggedIn();
        Objects.requireNonNull(roomName);

        String url = encodeURL(credentials.getServer() + "room?action=leave&name=" + roomName);
        try (Request request = new Request(url, Method.GET, null, sessionCookie)) {
            perform(request);
        }
    }


    public ChatRoom getChatRoom(String roomName, int messagesFrom) {
        checkLoggedIn();
        Objects.requireNonNull(roomName);
        if (messagesFrom < 0)
            throw new IllegalArgumentException("Invalid messagesFrom parameter value: " + messagesFrom);

        String url = encodeURL(credentials.getServer()
                + String.format("room?action=get&name=%1$s&from=%2$d", roomName, messagesFrom));
        try (Request request = new Request(url, Method.GET, null, sessionCookie)) {
            perform(request);
            return convertResponse(request.getResponse(), ChatRoom.class);
        }
    }



    public ChatRoomNames getChatRoomNames() {
        checkLoggedIn();

        String url = credentials.getServer() + "room?action=names";
        try (Request request = new Request(url, Method.GET, null, sessionCookie)) {
            perform(request);
            return convertResponse(request.getResponse(), ChatRoomNames.class);
        }
    }


    public void sendMessage(String roomName, String to, String text, boolean isPrivate) {
        checkLoggedIn();

        String messageData;
        try {
            messageData = Marshalling.marshal(new Message(
                    roomName,
                    LocalTime.now(),
                    credentials.getLogin(),
                    to,
                    text,
                    isPrivate));
        }
        catch (JAXBException e) {
            throw new ClientException(e);
        }

        String url = credentials.getServer() + "message";
        try (Request request = new Request(url, Method.POST, messageData, sessionCookie)) {
            perform(request);
        }
    }



    private void checkLoggedIn() {
        if (!isLoggedIn)
            throw new ClientException("Client is not logged in");
    }


    private static String encodeURL(String url) {
        try {
            return new URI(null, url, null).toASCIIString();
        }
        catch (URISyntaxException e) {
            throw new ClientException(e);
        }
    }


    private static void perform(Request request) {
        try {
            request.perform();
        }
        catch (IOException e) {
            throw new ClientException(e, request.getResponseCode());
        }
    }


    private static <T> T convertResponse(String response, Class<T> clazz) {
        if (response == null)
            return null;
        try {
            return Marshalling.unmarshal(response, clazz);
        }
        catch (JAXBException e) {
            throw new ClientException(e);
        }
    }

}
