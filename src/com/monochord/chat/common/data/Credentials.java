package com.monochord.chat.common.data;


import javax.xml.bind.annotation.*;

@XmlRootElement(name = "credentials")
@XmlAccessorType(XmlAccessType.FIELD)
public final class Credentials {

    @XmlTransient
    private String server;

    @XmlElement
    private String login;

    @XmlElement
    private String password;

    @XmlElement
    boolean isAway;


    private Credentials() {}


    public Credentials(String server, String login, String password) {
        this.login = checkArg(login, "Login not specified");
        this.password = checkArg(password, "Password not specified");

        String srv = checkArg(server, "Server not specified");
        this.server = srv.endsWith("/") ? srv : srv + "/";
    }


    private String checkArg(String value, String errMsg) {
        if (value == null || value.trim().isEmpty())
            throw new IllegalArgumentException(errMsg);
        return value;
    }


    public String getServer() {
        return server;
    }


    public String getLogin() {
        return login;
    }


    public String getPassword() {
        return password;
    }


    public boolean isAway() {
        return isAway;
    }


    public void setAway(boolean away) {
        isAway = away;
    }

}
