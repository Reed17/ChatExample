package com.monochord.chat.common.data;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@XmlRootElement(name = "messages")
public final class MessageList {

    @XmlElement(name = "message")
    private final List<Message> messages = new ArrayList<>();

    @XmlTransient
    private final List<Message> syncMessages = Collections.synchronizedList(messages);


    public int getCount() {
        return messages.size();
    }


    public List<Message> getMessages() {
        return messages;
    }


    public List<Message> getSynchronizedMessages() {
        return syncMessages;
    }

}
