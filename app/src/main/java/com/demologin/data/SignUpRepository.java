package com.demologin.data;

import com.demologin.data.model.SignedUpUser;

public class SignUpRepository {

    private static volatile SignUpRepository instance;

    private SignUpDataSource dataSource;

    private SignedUpUser user = null;

    // private constructor : singleton access
    private SignUpRepository(SignUpDataSource dataSource) {
        this.dataSource = dataSource;
    }

    public static SignUpRepository getInstance(SignUpDataSource dataSource) {
        if (instance == null) {
            instance = new SignUpRepository(dataSource);
        }
        return instance;
    }

    public boolean isLoggedIn() {
        return user != null;
    }

    public void logout() {
        user = null;
        dataSource.logout();
    }

    private void setSignedUpUser(SignedUpUser user) {
        this.user = user;
    }

    public Result<SignedUpUser> signUp(String email, String fullname, String password) {
        // handle login
        Result<SignedUpUser> result = dataSource.signUp(email,fullname, password);
        if (result instanceof Result.Success) {
            setSignedUpUser(((Result.Success<SignedUpUser>) result).getData());
        }
        return result;
    }
}
