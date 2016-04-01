package com.monochord.chat.common.data;


import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

@XmlRootElement(name = "room")
@XmlAccessorType(XmlAccessType.FIELD)
public final class ChatRoom {

    public static final String DEFAULT_CHAT_ROOM_NAME = "General Chat";

    @XmlElement
    private String name;

    @XmlElement(name = "users")
    private UserList userList;

    @XmlElement(name = "messages")
    private MessageList messageList;

    @XmlElement(name = "hiddenCount")
    private int hiddenMessagesCount;


    private ChatRoom() {}


    public ChatRoom(String name) {
        this.name = Objects.requireNonNull(name);
        userList = new UserList();
        messageList = new MessageList();
    }


    public ChatRoom(ChatRoom other, Predicate<Message> messageFilter, int messagesFrom) {
        Objects.requireNonNull(messageFilter);
        if (messagesFrom < 0)
            throw new IllegalArgumentException("Invalid messagesFrom parameter value: " + messagesFrom);

        name = other.name;

        List<User> otherUserList = other.userList.getSynchronizedUsers();
        synchronized (otherUserList) {
            otherUserList.forEach(user -> userList.getUsers().add(user));
        }

        messageList = new MessageList();
        List<Message> otherMessages = other.messageList.getSynchronizedMessages();
        synchronized (otherMessages) {
            int msgCount = otherMessages.size();
            if (messagesFrom < msgCount) {
                for (Message msg : otherMessages.subList(messagesFrom, msgCount)) {
                    if (messageFilter.test(msg))
                        messageList.getMessages().add(msg);
                    else
                        hiddenMessagesCount++;
                }
            }
        }
    }


    public String getName() {
        return name;
    }


    public UserList getUserList() {
        return userList;
    }


    public MessageList getMessageList() {
        return messageList;
    }


    public int getHiddenMessagesCount() {
        return hiddenMessagesCount;
    }

}
