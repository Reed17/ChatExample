package com.monochord.chat.common.data;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Comparator;
import java.util.Objects;

@XmlRootElement(name = "user")
@XmlAccessorType(XmlAccessType.FIELD)
public final class User implements Comparable<User> {

    @XmlElement
    private String name;

    @XmlElement
    private boolean isAway;


    private User() {}


    public User(String name, boolean isAway) {
        this.name = Objects.requireNonNull(name);
        this.isAway = isAway;
    }


    @Override
    public int hashCode() {
        return name.hashCode();
    }


    @Override
    public boolean equals(Object other) {
        return this == other || other instanceof User
                && Objects.equals(name, ((User)other).name);
    }


    @Override
    public String toString() {
        return name;
    }


    @Override
    public int compareTo(User user) {
        return name.compareTo(user.name);
    }


    public String getName() {
        return name;
    }


    public boolean isAway() {
        return isAway;
    }


    public void setAway(boolean away) {
        isAway = away;
    }

}
