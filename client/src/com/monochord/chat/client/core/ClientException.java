package com.monochord.chat.client.core;

public class ClientException extends RuntimeException {

    private final int serverResponseCode;


    public ClientException(Throwable cause) {
        this(cause, 0);
    }


    public ClientException(String message) {
        this(message, null, 0);
    }


    public ClientException(Throwable cause, int serverResponseCode) {
        this(cause == null ? null : cause.toString(), cause, serverResponseCode);
    }


    public ClientException(String message, Throwable cause, int serverResponseCode) {
        super(message, cause);
        this.serverResponseCode = serverResponseCode;
    }


    public int getServerResponseCode() {
        return serverResponseCode;
    }

}
