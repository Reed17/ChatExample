package com.monochord.chat.server;


import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.net.HttpURLConnection;

interface ActionHandler {

    ActionHandler UNKNOWN = new ActionHandler() {
        @Override
        public String getName() {
            return null;
        }
        @Override
        public void handle(HttpSession session, HttpServletRequest request, HttpServletResponse response)
                throws ServletException, IOException {
            response.sendError(HttpURLConnection.HTTP_BAD_REQUEST, "Action not specified or unsupported");
        }
    };


    String getName();


    void handle(HttpSession session, HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException;


    static <H extends ActionHandler> ActionHandler forAction(String actionName, H[] handlers) {
        if (actionName != null) {
            for (H handler : handlers)
                if (handler.getName().equalsIgnoreCase(actionName))
                    return handler;
        }
        return UNKNOWN;
    }

}
