package com.demologin.ui.signup;

/**
 * Class exposing authenticated user details to the UI.
 */
 class SignUpUserView {
    private String displayName;
    //... other data fields that may be accessible to the UI

    SignUpUserView(String displayName) {
        this.displayName = displayName;
    }

    String getDisplayName() {
        return displayName;
    }
}
