package com.demologin.ui.signup;

import androidx.annotation.Nullable;

/**
 * Authentication result : success (user details) or error message.
 */
class SignUpResult {
    @Nullable
    private SignUpUserView success;
    @Nullable
    private Integer error;

    @Nullable
    private String errorMsg;

    SignUpResult(@Nullable String error) {
        this.errorMsg = error;
    }

    SignUpResult(@Nullable Integer error) {
        this.error = error;
    }

    SignUpResult(@Nullable SignUpUserView success) {
        this.success = success;
    }

    @Nullable
    SignUpUserView getSuccess() {
        return success;
    }

    @Nullable
    Integer getError() {
        return error;
    }

    @Nullable
    public String getErrorMsg() {
        return errorMsg;
    }
}
