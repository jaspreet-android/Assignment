package com.demologin.data.model;

/**
 * Data class that captures user information for signed up users retrieved from LoginRepository
 */
public class SignedUpUser {

    private String userId;
    private String email;
    private String displayName;

    public SignedUpUser(String userId, String email, String displayName) {
        this.userId = userId;
        this.email = email;
        this.displayName = displayName;
    }

    public String getUserId() {
        return userId;
    }

    public String getEmail() {
        return email;
    }

    public String getDisplayName() {
        return displayName;
    }
}
