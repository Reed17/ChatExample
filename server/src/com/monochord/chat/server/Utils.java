package com.monochord.chat.server;


import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static java.net.HttpURLConnection.HTTP_UNAUTHORIZED;

class Utils {

    private Utils () {}


    static HttpSession getAuthorizedSession(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        HttpSession session = request.getSession(false);
        if (session == null)
            response.sendError(HTTP_UNAUTHORIZED);
        return session;
    }


    static String getLogin(HttpSession session) {
        return (String)session.getAttribute("login");
    }


    static String readRequestBody(HttpServletRequest request) throws IOException {
        BufferedInputStream input = new BufferedInputStream(request.getInputStream());
        ByteArrayOutputStream requestBody = new ByteArrayOutputStream();
            for (int b; (b = input.read()) > 0; )
                requestBody.write(b);
        return requestBody.size() > 0
                ? new String(requestBody.toByteArray(), StandardCharsets.UTF_8)
                : "";
    }

}
