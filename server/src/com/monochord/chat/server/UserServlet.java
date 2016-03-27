package com.monochord.chat.server;

import com.monochord.chat.common.data.ChatRoom;
import com.monochord.chat.common.data.Credentials;
import com.monochord.chat.common.data.User;
import com.monochord.chat.common.util.Marshalling;

import static java.net.HttpURLConnection.*;

import javax.servlet.ServletException;
import javax.servlet.http.*;
import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;


public class UserServlet extends HttpServlet {

    private static final Map<String, String> passwords = new HashMap<>();
    static {
        passwords.put("user1", "12345");
        passwords.put("user2", "aaa");
        passwords.put("user3", "bbb");
        passwords.put("Иван Иванов", "аааббб");
    }


    private enum GetHandler implements ActionHandler {

        LOGOUT {
            @Override
            public void handle(HttpSession session, HttpServletRequest request, HttpServletResponse response)
                    throws ServletException, IOException {
                String login = Utils.getLogin(session);
                Users users = Users.getInstance();
                User user = users.get(login);
                ChatRoomList.getInstance().removeUser(user);
                users.remove(login);
                session.invalidate();

                System.out.println(login + " logged out");
            }
        },

        AWAY {
            @Override
            public void handle(HttpSession session, HttpServletRequest request, HttpServletResponse response)
                    throws ServletException, IOException {
                String value = request.getParameter("value");
                if (value == null) {
                    response.sendError(HTTP_BAD_REQUEST, "Value not specified");
                    return;
                }
                User user = Users.getInstance().get(Utils.getLogin(session));
                if (user != null)
                    user.setAway(Boolean.parseBoolean(value));
            }
        };


        @Override
        public String getName() {
            return toString();
        }

    }


    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = Utils.getAuthorizedSession(request, response);
        if (session == null)
            return;

        ActionHandler.forParameter(request.getParameter("action"), GetHandler.values())
                .handle(session, request, response);
    }


    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (request.getSession(false) != null) {
            response.sendError(HTTP_FORBIDDEN, "Already authenticated");
            return;
        }

        String content = Utils.readRequestBody(request);
        if (content.isEmpty()) {
            response.sendError(HTTP_BAD_REQUEST);
            return;
        }
        Credentials credentials;
        try {
            credentials = Marshalling.unmarshal(content, Credentials.class);
        }
        catch (JAXBException e) {
            e.printStackTrace();
            response.sendError(HTTP_BAD_REQUEST);
            return;
        }

        String login = credentials.getLogin();
        String pwd = passwords.get(credentials.getLogin());
        if (pwd != null && pwd.equals(credentials.getPassword())) {
            Users users = Users.getInstance();
            HttpSession session = request.getSession();
            session.setAttribute("login", login);
            User user = new User(login, credentials.isAway());
            users.add(user);
            ChatRoomList.getInstance().addUser(ChatRoom.DEFAULT_CHAT_ROOM_NAME, user);

            System.out.println(login + " logged in");
        }
        else
            response.sendError(HTTP_UNAUTHORIZED);
    }

}
