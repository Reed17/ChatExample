package com.monochord.chat.common.data;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@XmlRootElement(name = "users")
public final class UserList {

    @XmlElement(name = "user")
    private final List<User> users = new ArrayList<>();

    @XmlTransient
    private final List<User> syncUsers = Collections.synchronizedList(users);


    public List<User> getUsers() {
        return users;
    }


    public List<User> getSynchronizedUsers() {
        return syncUsers;
    }

}
