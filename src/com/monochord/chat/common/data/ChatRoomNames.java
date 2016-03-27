package com.monochord.chat.common.data;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement(name = "rooms")
public final class ChatRoomNames {

    @XmlElement(name = "name")
    private final List<String> roomNames = new ArrayList<>();


    public List<String> getRoomNames() {
        return roomNames;
    }

}
