package com.monochord.chat.server;

import com.monochord.chat.common.data.Message;
import com.monochord.chat.common.util.Marshalling;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.xml.bind.JAXBException;
import java.io.IOException;

import static java.net.HttpURLConnection.HTTP_BAD_REQUEST;
import static java.net.HttpURLConnection.HTTP_NOT_IMPLEMENTED;


public class AddMessageServlet extends HttpServlet {

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = Utils.getAuthorizedSession(request, response);
        if (session == null)
            return;

        String content = Utils.readRequestBody(request);
        try {
            ChatRoomList.getInstance().addMessage(Marshalling.unmarshal(content, Message.class));
        }
        catch (JAXBException e) {
            e.printStackTrace();
            response.sendError(HTTP_BAD_REQUEST);
        }
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.sendError(HTTP_NOT_IMPLEMENTED);
    }

}
