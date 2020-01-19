package com.demologin.ui.signup;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.lifecycle.ViewModelProviders;

import com.demologin.AppController;
import com.demologin.BaseActivity;
import com.demologin.R;
import com.demologin.ui.login.LoginActivity;
import com.demologin.utils.Utils;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.hbb20.CountryCodePicker;

import java.util.concurrent.TimeUnit;

public class SignUpActivity extends BaseActivity {
    private static final String KEY_VERIFY_IN_PROGRESS = "key_verify_in_progress";
    private static final String TAG = "SignUpActivity";
    private ProgressBar loadingProgressBar;
    private SignUpViewModel signUpViewModel;
    private boolean mVerificationInProgress = false;
    private String mVerificationId;
    private PhoneAuthProvider.ForceResendingToken mResendToken;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;
    private AppCompatEditText userNumberEditText;
    private CountryCodePicker ccp;
    private TextInputEditText verifyNumberEditText;
    private TextInputEditText userNameEditText;
    private TextInputEditText userEmailEditText;
    private TextInputEditText passwordEditText;
    private AppCompatButton resendCodeBtn;
    private AppCompatButton verifyPhoneNumberBtn;
    private AppCompatButton signUpButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        signUpViewModel = ViewModelProviders.of(this, new SignUpViewModelFactory())
                .get(SignUpViewModel.class);
        userNameEditText = findViewById(R.id.userFullName);
        userEmailEditText = findViewById(R.id.useremail);
        resendCodeBtn = findViewById(R.id.resendCode);
        verifyNumberEditText = findViewById(R.id.verifyNumber);
        userNumberEditText = findViewById(R.id.userNumber);
        ccp = findViewById(R.id.ccp);
        ccp.registerCarrierNumberEditText(userNumberEditText);
        passwordEditText = findViewById(R.id.password);
        final TextInputEditText cnfPasswordEditText = findViewById(R.id.confirmPassword);
        signUpButton = findViewById(R.id.signUp);
        verifyPhoneNumberBtn = findViewById(R.id.verifyPhoneNumberBtn);
        loadingProgressBar = findViewById(R.id.loading);

        signUpViewModel.getSignUpFormState().observe(this, signUpFormState -> {
            if (signUpFormState == null) {
                return;
            }
            if (signUpFormState.getEmailError() != null) {
                userEmailEditText.setError(getString(signUpFormState.getEmailError()));
            }
            if (signUpFormState.getNumberError() != null && !ccp.isValidFullNumber()) {
                userNumberEditText.setError(getString(signUpFormState.getNumberError()));
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
                        cnfPasswordEditText.getText().toString(),
                        ccp.getFullNumberWithPlus());
            }
        };
        userNameEditText.addTextChangedListener(afterTextChangedListener);
        userEmailEditText.addTextChangedListener(afterTextChangedListener);
        passwordEditText.addTextChangedListener(afterTextChangedListener);
        userNumberEditText.addTextChangedListener(afterTextChangedListener);
        cnfPasswordEditText.addTextChangedListener(afterTextChangedListener);

        signUpButton.setOnClickListener(v -> {
            if (isNetworkAvailable()) {
                if (!TextUtils.isEmpty(userEmailEditText.getText().toString())
                        && !TextUtils.isEmpty(passwordEditText.getText().toString())
                        && !TextUtils.isEmpty(userNumberEditText.getText().toString())
                        && !TextUtils.isEmpty(cnfPasswordEditText.getText().toString())) {
                    if (!ccp.isValidFullNumber()) {
                        userNumberEditText.setError(getString(R.string.invalid_number));
                        return;
                    } else if (!Utils.isValidEmail(userEmailEditText.getText().toString())) {
                        userEmailEditText.setError(getString(R.string.invalid_username));
                        return;
                    }
                    Utils.hideKeyboard(this, cnfPasswordEditText);
                    loadingProgressBar.setVisibility(View.VISIBLE);
                    startPhoneNumberVerification(ccp.getFullNumberWithPlus());
                }
            } else {
                Toast.makeText(AppController.getInstance(), getString(R.string.internet_error), Toast.LENGTH_LONG).show();
            }
        });
        resendCodeBtn.setOnClickListener(v -> {
            if (!ccp.isValidFullNumber()) {
                userNumberEditText.setError(getString(R.string.invalid_number));
                return;
            }
            resendVerificationCode(ccp.getFullNumberWithPlus(), mResendToken);
        });
        verifyPhoneNumberBtn.setOnClickListener(v -> {
            String code = verifyNumberEditText.getText().toString();
            if (TextUtils.isEmpty(code)) {
                verifyNumberEditText.setError("Cannot be empty.");
                return;
            }

            loadingProgressBar.setVisibility(View.VISIBLE);
            verifyPhoneNumberWithCode(mVerificationId, code);
        });
        mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            @Override
            public void onVerificationCompleted(PhoneAuthCredential credential) {
                mVerificationInProgress = false;
                loadingProgressBar.setVisibility(View.GONE);
                // Set the verification text based on the credential
                verifyNumberEditText.setVisibility(View.VISIBLE);
                verifyPhoneNumberBtn.setVisibility(View.VISIBLE);
                signUpButton.setVisibility(View.GONE);
                if (credential != null) {
                    if (credential.getSmsCode() != null) {
                        verifyNumberEditText.setText(credential.getSmsCode());
                    } else {
                        verifyNumberEditText.setText(R.string.instant_validation);
                    }
                }
            }

            @Override
            public void onVerificationFailed(FirebaseException e) {
                Log.w(TAG, "onVerificationFailed", e);
                mVerificationInProgress = false;
                if (e instanceof FirebaseAuthInvalidCredentialsException) {
                    // Invalid request
                    userNumberEditText.setError(getString(R.string.invalid_phone_number));
                } else if (e instanceof FirebaseTooManyRequestsException) {

                }
                resendCodeBtn.setVisibility(View.VISIBLE);
            }

            @Override
            public void onCodeSent(@NonNull String verificationId,
                                   @NonNull PhoneAuthProvider.ForceResendingToken token) {
                Log.d(TAG, "onCodeSent:" + verificationId);
                loadingProgressBar.setVisibility(View.GONE);
                mVerificationId = verificationId;
                mResendToken = token;
                verifyNumberEditText.setVisibility(View.VISIBLE);
                resendCodeBtn.setVisibility(View.VISIBLE);
                verifyPhoneNumberBtn.setVisibility(View.VISIBLE);
                signUpButton.setVisibility(View.GONE);
            }
        };
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mVerificationInProgress && validatePhoneNumber()) {
            startPhoneNumberVerification(userNumberEditText.getText().toString());
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(KEY_VERIFY_IN_PROGRESS, mVerificationInProgress);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mVerificationInProgress = savedInstanceState.getBoolean(KEY_VERIFY_IN_PROGRESS);
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

    private void startPhoneNumberVerification(String phoneNumber) {
        // [START start_phone_auth]
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phoneNumber,        // Phone number to verify
                60,                 // Timeout duration
                TimeUnit.SECONDS,   // Unit of timeout
                this,               // Activity (for callback binding)
                mCallbacks);        // OnVerificationStateChangedCallbacks
        // [END start_phone_auth]

        mVerificationInProgress = true;
    }

    private void verifyPhoneNumberWithCode(String verificationId, String code) {
        // [START verify_with_code]
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, code);
        // [END verify_with_code]
        signInWithPhoneAuthCredential(credential);
    }

    // [START resend_verification]
    private void resendVerificationCode(String phoneNumber,
                                        PhoneAuthProvider.ForceResendingToken token) {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phoneNumber,        // Phone number to verify
                60,                 // Timeout duration
                TimeUnit.SECONDS,   // Unit of timeout
                this,               // Activity (for callback binding)
                mCallbacks,         // OnVerificationStateChangedCallbacks
                token);             // ForceResendingToken from callbacks
    }
    // [END resend_verification]

    // [START sign_in_with_phone]
    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    loadingProgressBar.setVisibility(View.GONE);
                    if (task.isSuccessful()) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d(TAG, "signInWithCredential:success");

                        FirebaseUser user = task.getResult().getUser();

                        signUpViewModel.signUp(userEmailEditText.getText().toString(),
                                userNameEditText.getText().toString(),
                                passwordEditText.getText().toString());
                    } else {
                        // Sign in failed, display a message and update the UI
                        Log.w(TAG, "signInWithCredential:failure", task.getException());
                        if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                            // The verification code entered was invalid
                            // [START_EXCLUDE silent]
                            verifyNumberEditText.setError("Invalid code.");
                            // [END_EXCLUDE]
                        }
                    }
                });
    }

    private boolean validatePhoneNumber() {
        String phoneNumber = userNumberEditText.getText().toString();
        if (TextUtils.isEmpty(phoneNumber)) {
            userNumberEditText.setError("Invalid phone number.");
            return false;
        }

        return true;
    }

}
