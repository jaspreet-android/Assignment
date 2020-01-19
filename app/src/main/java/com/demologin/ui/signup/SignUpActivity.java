package com.demologin.ui.signup;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.StringRes;
import androidx.appcompat.widget.AppCompatButton;
import androidx.lifecycle.ViewModelProviders;

import com.demologin.AppController;
import com.demologin.BaseActivity;
import com.demologin.R;
import com.demologin.ui.login.LoginActivity;
import com.demologin.utils.Utils;
import com.google.android.material.textfield.TextInputEditText;

public class SignUpActivity extends BaseActivity {
    private static final String TAG = "SignUpActivity";
    private ProgressBar loadingProgressBar;
    private SignUpViewModel signUpViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        signUpViewModel = ViewModelProviders.of(this, new SignUpViewModelFactory())
                .get(SignUpViewModel.class);
        final TextInputEditText userNameEditText = findViewById(R.id.userFullName);
        final TextInputEditText userEmailEditText = findViewById(R.id.useremail);
        final TextInputEditText passwordEditText = findViewById(R.id.password);
        final TextInputEditText cnfPasswordEditText = findViewById(R.id.confirmPassword);
        final AppCompatButton signUpButton = findViewById(R.id.signUp);
        loadingProgressBar = findViewById(R.id.loading);

        signUpViewModel.getSignUpFormState().observe(this, signUpFormState -> {
            if (signUpFormState == null) {
                return;
            }
            if (signUpFormState.getEmailError() != null) {
                userEmailEditText.setError(getString(signUpFormState.getEmailError()));
            }
            if (signUpFormState.getPasswordError() != null) {
                passwordEditText.setError(getString(signUpFormState.getPasswordError()), null);
            }
            if (signUpFormState.getCnfpasswordError() != null) {
                cnfPasswordEditText.setError(getString(signUpFormState.getCnfpasswordError()));
            }
        });

        signUpViewModel.getSignUpResult().observe(this, signUpResult -> {
            if (signUpResult == null) {
                return;
            }
            loadingProgressBar.setVisibility(View.GONE);
            if (signUpResult.getErrorMsg() != null) {
                showSignUpFailed(signUpResult.getErrorMsg());
            }
            if (signUpResult.getError() != null) {
                showSignUpFailed(signUpResult.getError());
            }
            if (signUpResult.getSuccess() != null) {
                updateUiWithUser(signUpResult.getSuccess());
            }
        });

        TextWatcher afterTextChangedListener = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // ignore
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // ignore
            }

            @Override
            public void afterTextChanged(Editable s) {
                signUpViewModel.signUpDataChanged(userEmailEditText.getText().toString(),
                        passwordEditText.getText().toString(),
                        cnfPasswordEditText.getText().toString());
            }
        };
        userNameEditText.addTextChangedListener(afterTextChangedListener);
        userEmailEditText.addTextChangedListener(afterTextChangedListener);
        passwordEditText.addTextChangedListener(afterTextChangedListener);
        cnfPasswordEditText.addTextChangedListener(afterTextChangedListener);

        signUpButton.setOnClickListener(v -> {
            if (isNetworkAvailable()) {
                if (!TextUtils.isEmpty(userEmailEditText.getText().toString())
                        && !TextUtils.isEmpty(passwordEditText.getText().toString())
                        && !TextUtils.isEmpty(cnfPasswordEditText.getText().toString())) {
                    if (!Utils.isValidEmail(userEmailEditText.getText().toString())) {
                        userEmailEditText.setError(getString(R.string.invalid_username));
                        return;
                    }
                    Utils.hideKeyboard(this, cnfPasswordEditText);
                    loadingProgressBar.setVisibility(View.VISIBLE);

                    signUpViewModel.signUp(userEmailEditText.getText().toString(),
                            userNameEditText.getText().toString(),
                            passwordEditText.getText().toString());
                }
            } else {
                Toast.makeText(AppController.getInstance(), getString(R.string.internet_error), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void showSignUpFailed(@StringRes Integer errorString) {
        runOnUiThread(() -> Toast.makeText(getApplicationContext(), errorString, Toast.LENGTH_SHORT).show());
    }

    private void showSignUpFailed(String errorString) {
        runOnUiThread(() -> Toast.makeText(getApplicationContext(), errorString, Toast.LENGTH_SHORT).show());
    }

    private void updateUiWithUser(SignUpUserView model) {
        prefs.setIsRegisterDone(true);
        createNewFirebaseUser(prefs.getEmail(), prefs.getUserPassword());
    }

    private void createNewFirebaseUser(String email, String password) {
        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this, task -> {
            if (task.isSuccessful()) {
                prefs.setIsRegisterDone(true);
                prefs.setIsVerified(false);
                Toast.makeText(AppController.getInstance(), getString(R.string.user_registered_msg), Toast.LENGTH_LONG)
                        .show();
                startActivity(new Intent(SignUpActivity.this, LoginActivity.class));
                finish();
            }
        });
    }

}
