package com.geneflow.app.model;

/** Registered clinician/account. */
public class User {
    public long id;
    public String username;
    public String email;
    public String password;

    public User(String username, String email, String password) {
        this.username = username;
        this.email = email;
        this.password = password;
    }
}
