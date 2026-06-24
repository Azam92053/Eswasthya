package com.eswasthya.desktop;

import com.eswasthya.desktop.model.UserInfo;

/** In-memory session state for the authenticated desktop user. */
public class AuthSession {

    private static AuthSession instance;

    private String    token;
    private UserInfo  user;

    private AuthSession() {}

    public static AuthSession getInstance() {
        if (instance == null) instance = new AuthSession();
        return instance;
    }

    public void login(String token, UserInfo user) {
        this.token = token;
        this.user  = user;
    }

    public void logout() {
        this.token = null;
        this.user  = null;
    }

    public String   getToken()     { return token; }
    public UserInfo getUser()      { return user; }
    public boolean  isLoggedIn()   { return token != null && user != null; }
    public boolean  isAdmin()      { return user != null && "ADMIN".equals(user.getRole()); }
    public String   getUsername()  { return user != null ? user.getUsername() : ""; }
    public String   getFullName()  { return user != null ? user.getName() : ""; }
}
