package model;

import java.io.Serializable;

public class User implements Serializable {
    private static final long serialVersionUID = 1L;

    private String username;
    private String password;   // In production: store hashed password
    private boolean isAdmin;

    public User(String username, String password, boolean isAdmin) {
        this.username = username;
        this.password = password;
        this.isAdmin  = isAdmin;
    }

    public boolean checkPassword(String input) {
        return this.password.equals(input);
    }

    public String getUsername() { return username; }
    public boolean isAdmin()    { return isAdmin; }

    @Override
    public String toString() {
        return username + (isAdmin ? " [Admin]" : " [Student]");
    }
}
