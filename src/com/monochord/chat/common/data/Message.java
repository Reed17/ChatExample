package com.monochord.chat.common.data;

import com.monochord.chat.common.data.MessageTimeAdapter;

import javax.xml.bind.UnmarshalException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.*;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.time.LocalTime;
import java.util.Objects;

@XmlRootElement(name = "message")
@XmlAccessorType(XmlAccessType.FIELD)
public final class Message {

    private static final String ALL = "*ALL*";

    @XmlElement(name = "room")
    private String roomName;

    @XmlJavaTypeAdapter(MessageTimeAdapter.class)
    private LocalTime time;

    @XmlElement
    private String from;

    @XmlElement
    private String to;

    @XmlElement
    private String text;

    @XmlElement
    private boolean isPrivate;


    private Message() {}


    public Message(String roomName, LocalTime time, String from, String to, String text, boolean isPrivate) {
        this.roomName = checkParameter("Chat room name ", roomName);
        this.time = checkParameter("Message time", time);
        this.from = checkParameter("From name", from);
        this.text = checkParameter("Message text", text);
        this.to = to != null && !to.isEmpty() ? to : ALL;
        this.isPrivate = isPrivate;
    }


    private static <T> T checkParameter(String name, T value) {
        if (!isValid(value))
            throw new IllegalArgumentException('"' + name + "\" parameter is invalid: " + value);
        return value;
    }


    private static boolean isValid(Object value) {
        return value != null && !(value instanceof String && ((String)value).trim().isEmpty());
    }


    private static void checkMissing(String name, Object value)
            throws UnmarshalException {
        if (!isValid(value))
            throw new UnmarshalException("Missing required element: " + name);
    }


    private void afterUnmarshal(Unmarshaller u, Object parent) throws UnmarshalException {
        checkMissing("Room name", roomName);
        checkMissing("Message time", time);
        checkMissing("From name", from);
        checkMissing("To name", to);
        checkMissing("Message text", text);
    }


    public String getRoomName() {
        return roomName;
    }


    public String getFrom() {
        return from;
    }


    public String getTo() {
        return to;
    }


    public boolean isPrivate() {
        return isPrivate;
    }


    @Override
    public String toString() {
        return  "[" + time.format(MessageTimeAdapter.TIME_FORMAT) + "]" +
                (isPrivate ? "(PM)" : "") + "<" + from + " -> " + to + ">" +
                ' ' + text;
    }

}
