package com.demologin.ui.signup;

import androidx.annotation.Nullable;

/**
 * Data validation state of the signup form.
 */
class SignUpFormState {
    @Nullable
    private Integer emailError;
    @Nullable
    private Integer passwordError;
    @Nullable
    private Integer cnfpasswordError;
    @Nullable
    private Integer numberError;

    private boolean isDataValid;

    SignUpFormState(@Nullable Integer emailError, @Nullable Integer passwordError,
                    @Nullable Integer cnfpasswordError, @Nullable Integer numberError) {
        this.emailError = emailError;
        this.passwordError = passwordError;
        this.cnfpasswordError = cnfpasswordError;
        this.numberError = numberError;
        this.isDataValid = false;
    }

    SignUpFormState(boolean isDataValid) {
        this.emailError = null;
        this.passwordError = null;
        this.cnfpasswordError = null;
        this.numberError = null;
        this.isDataValid = isDataValid;
    }

    @Nullable
    Integer getEmailError() {
        return emailError;
    }

    @Nullable
    public Integer getCnfpasswordError() {
        return cnfpasswordError;
    }

    @Nullable
    Integer getPasswordError() {
        return passwordError;
    }

    @Nullable
    public Integer getNumberError() {
        return numberError;
    }

    boolean isDataValid() {
        return isDataValid;
    }
}
