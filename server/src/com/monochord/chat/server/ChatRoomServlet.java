package com.monochord.chat.server;

import com.monochord.chat.common.data.ChatRoom;
import com.monochord.chat.common.data.User;
import com.monochord.chat.common.util.Marshalling;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static java.net.HttpURLConnection.*;

public class ChatRoomServlet extends HttpServlet {


    private enum GetHandler implements ActionHandler {

        NEW {
            @Override
            public void handle(HttpSession session, HttpServletRequest request, HttpServletResponse response)
                    throws ServletException, IOException {
                String roomName = readChatRoomName(request, response);
                if (roomName == null)
                    return;
                ChatRoomList chatRoomList = ChatRoomList.getInstance();
                if (chatRoomList.addChatRoom(roomName))
                    chatRoomList.addUser(roomName, Users.getInstance().get(Utils.getLogin(session)));
                else
                    response.sendError(HTTP_FORBIDDEN, "Room with the specified name already exists");
            }
        },

        JOIN {
            @Override
            public void handle(HttpSession session, HttpServletRequest request, HttpServletResponse response)
                    throws ServletException, IOException {
                String roomName = readChatRoomName(request, response);
                if (roomName == null)
                    return;
                ChatRoomList.getInstance()
                        .addUser(roomName, Users.getInstance().get(Utils.getLogin(session)));
            }
        },

        LEAVE {
            @Override
            public void handle(HttpSession session, HttpServletRequest request, HttpServletResponse response)
                    throws ServletException, IOException {
                String roomName = readChatRoomName(request, response);
                if (roomName == null)
                    return;
                else if (roomName.equals(ChatRoom.DEFAULT_CHAT_ROOM_NAME)) {
                    response.sendError(HTTP_FORBIDDEN, "Can't leave the default chat room");
                    return;
                }
                User user = Users.getInstance().get(Utils.getLogin(session));
                if (user != null)
                    ChatRoomList.getInstance().removeUser(roomName, user);
            }
        },

        GET {
            @Override
            public void handle(HttpSession session, HttpServletRequest request, HttpServletResponse response)
                    throws ServletException, IOException {
                String roomName = readChatRoomName(request, response);
                if (roomName == null)
                    return;

                int messagesFrom;
                try {
                    messagesFrom = Integer.parseInt(request.getParameter("from"));
                }
                catch (NumberFormatException e) {
                    response.sendError(HTTP_BAD_REQUEST, "From parameter not specified or not valid");
                    return;
                }

                ChatRoom chatRoom = ChatRoomList.getInstance().getChatRoom(roomName);
                if (chatRoom == null) {
                    response.sendError(HTTP_BAD_REQUEST, "No room with that name");
                    return;
                }

                String userName = Utils.getLogin(session);
                ChatRoom copy = new ChatRoom(chatRoom, message ->
                        !message.isPrivate()
                                || userName.equals(message.getFrom())
                                || userName.equals(message.getTo()),
                        messagesFrom);
                try {
                    String data = Marshalling.marshal(copy);
                    response.getOutputStream().write(data.getBytes(StandardCharsets.UTF_8));
                }
                catch (JAXBException e) {
                    e.printStackTrace();
                    response.sendError(HTTP_INTERNAL_ERROR);
                }

            }
        },

        NAMES {
            @Override
            public void handle(HttpSession session, HttpServletRequest request, HttpServletResponse response)
                    throws ServletException, IOException {
                try {
                    String data = Marshalling.marshal(ChatRoomList.getInstance().getChatRoomNames());
                    response.getOutputStream().write(data.getBytes(StandardCharsets.UTF_8));
                }
                catch (JAXBException e) {
                    e.printStackTrace();
                    response.sendError(HTTP_INTERNAL_ERROR);
                }
            }
        };


        @Override
        public String getName() {
            return toString();
        }


        private static String readChatRoomName(HttpServletRequest request, HttpServletResponse response)
                throws IOException {
            String roomName = request.getParameter("name");
            if (roomName == null || roomName.isEmpty()) {
                response.sendError(HTTP_BAD_REQUEST, "Room name not specified");
                return null;
            }
            return roomName;
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
        response.sendError(HTTP_NOT_IMPLEMENTED);
    }
}
