package com.monochord.chat.server;


import com.monochord.chat.common.data.User;

import java.util.*;

class Users {

    private Users() {}

    private final static Users INSTANCE = new Users();

    private final Map<String, User> users = Collections.synchronizedMap(new HashMap<>());


    static Users getInstance() {
        return INSTANCE;
    }


    boolean add(User user) {
        return users.put(user.getName(), user) == null;
    }


    User get(String name) {
        return users.get(name);
    }


    boolean remove(String name) {
        return users.remove(name) != null;
    }

}
