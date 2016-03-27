package com.monochord.chat.client.core;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

class Request implements AutoCloseable {

    private final String url;
    private final Method method;
    private final String postData;
    private final String sessionCookie;
    private HttpURLConnection connection;
    private String response;
    private boolean isClosed;


    enum Method {GET, POST}


    Request(String url, Method method, String postData, String sessionCookie) {
        this.url = Objects.requireNonNull(url);
        this.method = Objects.requireNonNull(method);
        this.sessionCookie = sessionCookie;

        if (method == Method.POST && (postData == null || postData.trim().isEmpty()))
            throw new IllegalArgumentException("Post data must be specified for POST request");
        this.postData = postData;
    }


    void perform() throws IOException {
        if (isClosed)
            throw new IOException("Request is closed");

        connection = (HttpURLConnection)new URL(url).openConnection();
        connection.setRequestMethod(method.toString());
        connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        connection.setRequestProperty("Charset", "UTF-8");
        if (sessionCookie != null)
            connection.setRequestProperty("Cookie", sessionCookie);

        Charset charset = StandardCharsets.UTF_8;
        if (method == Method.POST) {
            connection.setDoOutput(true);
            try (OutputStream out = connection.getOutputStream()) {
                out.write(postData.getBytes(charset));
            }
        }
        try (BufferedReader reader =
                     new BufferedReader(new InputStreamReader(connection.getInputStream(), charset))) {
            response = reader.lines().collect(Collectors.joining());
        }
    }


    String getSessionCookie() {
        if (connection != null) {
            List<String> cookies = connection.getHeaderFields().get("Set-Cookie");
            if (cookies != null) {
                String id = "JSESSIONID";
                for (String cookie : cookies) {
                    int i1 = cookie.indexOf(id);
                    if (i1 > -1) {
                        int i2 = cookie.indexOf(";", i1);
                        return i2 > -1 ? cookie.substring(i1, i2) : cookie.substring(i1);
                    }
                }
            }
        }
        return null;
    }


    int getResponseCode() {
        if (connection != null) {
            int responseCode;
            try {
                responseCode = connection.getResponseCode();
            }
            catch (IOException e) {
                System.out.println("Unable to get server response code: " + e.getMessage());
                responseCode = -1;
            }
            return responseCode;
        }
        else
            return 0;
    }


    String getResponse() {
        return response;
    }


    @Override
    public void close() {
        if (connection != null) {
            connection.disconnect();
            connection = null;
        }
        isClosed = true;
    }

}
