package net.rebeyond.behinder.utils;

import java.net.Authenticator;
import java.net.PasswordAuthentication;

public class BasicAuthenticator extends Authenticator {
    String password;
    String userName;

    public BasicAuthenticator(String userName2, String password2) {
        this.userName = userName2;
        this.password = password2;
    }

    /* access modifiers changed from: protected */
    public PasswordAuthentication getPasswordAuthentication() {
        return new PasswordAuthentication(this.userName, this.password.toCharArray());
    }
}
