package com.demologin.ui.login;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.lifecycle.ViewModelProviders;

import com.demologin.AppController;
import com.demologin.BaseActivity;
import com.demologin.R;
import com.demologin.ui.home.HomeActivity;
import com.demologin.ui.signup.SignUpActivity;
import com.demologin.utils.Utils;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.hbb20.CountryCodePicker;

import java.util.concurrent.TimeUnit;

public class LoginActivity extends BaseActivity {
    private static final String KEY_VERIFY_IN_PROGRESS = "key_verify_in_progress";
    private static final String TAG = "LoginActivity";
    private static final int RC_SIGN_IN = 32032;
    private ProgressBar loadingProgressBar;
    private LoginViewModel loginViewModel;
    private CallbackManager mCallbackManager;
    private boolean mVerificationInProgress = false;
    private String mVerificationId;
    private PhoneAuthProvider.ForceResendingToken mResendToken;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;
    private AppCompatEditText userNumberEditText;
    private CountryCodePicker ccp;
    private TextInputEditText verifyNumberEditText;
    private AppCompatButton resendCodeBtn;
    private AppCompatButton verifyPhoneNumberBtn;
    private AppCompatButton sendCodeBtn;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        loginViewModel = ViewModelProviders.of(this, new LoginViewModelFactory())
                .get(LoginViewModel.class);
        final TextInputEditText userEmailEditText = findViewById(R.id.useremail);
        final TextInputEditText passwordEditText = findViewById(R.id.password);
        final AppCompatButton loginButton = findViewById(R.id.login);
        final AppCompatButton signUpButton = findViewById(R.id.signUp);
        verifyNumberEditText = findViewById(R.id.verifyNumber);
        sendCodeBtn = findViewById(R.id.sendCodeBtn);
        resendCodeBtn = findViewById(R.id.resendCode);
        userNumberEditText = findViewById(R.id.userNumber);
        ccp = findViewById(R.id.ccp);
        ccp.registerCarrierNumberEditText(userNumberEditText);
        verifyPhoneNumberBtn = findViewById(R.id.verifyPhoneNumberBtn);
        ccp.setOnCountryChangeListener(() -> {
            sendCodeBtn.setVisibility(View.VISIBLE);
            verifyPhoneNumberBtn.setVisibility(View.GONE);
            resendCodeBtn.setVisibility(View.INVISIBLE);
            verifyNumberEditText.setVisibility(View.GONE);
        });
        final AppCompatImageView googleLogin = findViewById(R.id.googleLogin);
        final LoginButton fbLogin = findViewById(R.id.fbLogin);
        loadingProgressBar = findViewById(R.id.loading);

        loginViewModel.getLoginFormState().observe(this, loginFormState -> {
            if (loginFormState == null) {
                return;
            }
            loginButton.setEnabled(loginFormState.isDataValid());
            if (loginFormState.getUsernameError() != null) {
                userEmailEditText.setError(getString(loginFormState.getUsernameError()));
            }
            if (loginFormState.getPasswordError() != null) {
                passwordEditText.setError(getString(loginFormState.getPasswordError()), null);
            }
        });

        loginViewModel.getLoginResult().observe(this, loginResult -> {
            if (loginResult == null) {
                return;
            }
            loadingProgressBar.setVisibility(View.GONE);
            if (loginResult.getErrorMsg() != null) {
                showLoginFailed(loginResult.getErrorMsg());
            }
            if (loginResult.getError() != null) {
                showLoginFailed(loginResult.getError());
            }
            if (loginResult.getSuccess() != null) {
                updateUiWithUser(loginResult.getSuccess());
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
                loginViewModel.loginDataChanged(userEmailEditText.getText().toString(),
                        passwordEditText.getText().toString());
            }
        };
        userNumberEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() == 0 || s.length() < 7) {
                    sendCodeBtn.setVisibility(View.VISIBLE);
                    verifyPhoneNumberBtn.setVisibility(View.GONE);
                    resendCodeBtn.setVisibility(View.INVISIBLE);
                    verifyNumberEditText.setVisibility(View.GONE);
                }
            }
        });
        userEmailEditText.addTextChangedListener(afterTextChangedListener);
        passwordEditText.addTextChangedListener(afterTextChangedListener);
        passwordEditText.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                loginViewModel.login(userEmailEditText.getText().toString(),
                        passwordEditText.getText().toString());
            }
            return false;
        });

        loginButton.setOnClickListener(v -> {
            if (isNetworkAvailable()) {
                if (!TextUtils.isEmpty(userEmailEditText.getText().toString())
                        && !TextUtils.isEmpty(passwordEditText.getText().toString())) {
                    Utils.hideKeyboard(this, passwordEditText);
                    loadingProgressBar.setVisibility(View.VISIBLE);
                    loginViewModel.login(userEmailEditText.getText().toString(),
                            passwordEditText.getText().toString());
                }
            } else {
                Toast.makeText(AppController.getInstance(), getString(R.string.internet_error), Toast.LENGTH_LONG).show();
            }
        });

        googleLogin.setOnClickListener(v -> signIn());

        signUpButton.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, SignUpActivity.class));
            finish();
        });

        // Initialize Facebook Login button
        mCallbackManager = CallbackManager.Factory.create();
        fbLogin.setPermissions("email", "public_profile");
        fbLogin.registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.d(TAG, "facebook:onSuccess:" + loginResult);
                handleFacebookAccessToken(loginResult.getAccessToken());
            }

            @Override
            public void onCancel() {
                Log.d(TAG, "facebook:onCancel");
            }

            @Override
            public void onError(FacebookException error) {
                Log.d(TAG, "facebook:onError", error);
            }
        });
        sendCodeBtn.setOnClickListener(v -> {
            if (isNetworkAvailable()) {
                if (!ccp.isValidFullNumber()) {
                    userNumberEditText.setError(getString(R.string.invalid_number));
                    return;
                }
                Utils.hideKeyboard(this, userEmailEditText);
                loadingProgressBar.setVisibility(View.VISIBLE);
                startPhoneNumberVerification(ccp.getFullNumberWithPlus());
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
            verifyNumberEditText.requestFocus();
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
                sendCodeBtn.setVisibility(View.GONE);
                if (credential != null) {
                    if (credential.getSmsCode() != null) {
                        verifyNumberEditText.setText(credential.getSmsCode());
                    } else {
                        verifyNumberEditText.setText(R.string.instant_validation);
                    }
                }
                verifyNumberEditText.requestFocus();
                signInWithPhoneAuthCredential(credential);
            }

            @Override
            public void onVerificationFailed(FirebaseException e) {
                Log.w(TAG, "onVerificationFailed", e);
                showLoginFailed(e.getMessage());
                loadingProgressBar.setVisibility(View.GONE);
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
                sendCodeBtn.setVisibility(View.GONE);
                verifyNumberEditText.requestFocus();
            }
        };
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mVerificationInProgress && validatePhoneNumber()) {
            startPhoneNumberVerification(userNumberEditText.getText().toString());
        } else {
            updateUI();
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

    private void updateUiWithUser(LoggedInUserView model) {
        //Complete and destroy login activity once successful
        startActivity(new Intent(this, HomeActivity.class));
        finish();
    }

    private void showLoginFailed(@StringRes Integer errorString) {
        Toast.makeText(getApplicationContext(), errorString, Toast.LENGTH_SHORT).show();
    }

    private void showLoginFailed(String errorString) {
        Toast.makeText(getApplicationContext(), errorString, Toast.LENGTH_SHORT).show();
    }

    private void handleFacebookAccessToken(AccessToken token) {
        Log.d(TAG, "handleFacebookAccessToken:" + token);

        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d(TAG, "signInWithCredential:success");
                        FirebaseUser user = mAuth.getCurrentUser();
                        prefs.setEmail(user.getEmail());
                        prefs.setIsSocial(true);
                        prefs.setFacebookLoggedIn(true);
                        prefs.setGoogleLoggedIn(false);
                        prefs.setFbId(token.getUserId());
                        prefs.setFbToken(token.getToken());
                        prefs.setIsRegisterDone(true);
                        prefs.setIsVerified(true);
                        updateUI();
                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w(TAG, "signInWithCredential:failure", task.getException());
                        Toast.makeText(AppController.getInstance(), getString(R.string.auth_failed), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void updateUI() {
        if (isLoggedIn()) {
            startActivity(new Intent(LoginActivity.this, HomeActivity.class));
            finish();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mCallbackManager.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                Log.w(TAG, "Google sign in failed", e);
            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.getId());

        loadingProgressBar.setVisibility(View.VISIBLE);

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "signInWithCredential:success");
                        FirebaseUser user = mAuth.getCurrentUser();
                        prefs.setEmail(user.getEmail());
                        prefs.setIsSocial(true);
                        prefs.setFacebookLoggedIn(false);
                        prefs.setGoogleLoggedIn(true);
                        prefs.setIsRegisterDone(true);
                        prefs.setIsVerified(true);
                        updateUI();
                    } else {
                        Log.w(TAG, "signInWithCredential:failure", task.getException());
                        Toast.makeText(AppController.getInstance(), getString(R.string.auth_failed), Toast.LENGTH_SHORT).show();
                    }

                    loadingProgressBar.setVisibility(View.GONE);
                });
    }

    private void signIn() {
        if (isNetworkAvailable()) {
            Intent signInIntent = mGoogleSignInClient.getSignInIntent();
            startActivityForResult(signInIntent, RC_SIGN_IN);
        } else {
            Toast.makeText(AppController.getInstance(), getString(R.string.internet_error), Toast.LENGTH_LONG).show();
        }
    }

    private void revokeAccess() {
        // Firebase sign out
        mAuth.signOut();

        // Google revoke access
        mGoogleSignInClient.revokeAccess().addOnCompleteListener(this,
                task -> {
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
                        prefs.setEmail(user.getPhoneNumber());
                        prefs.setIsSocial(false);
                        prefs.setFacebookLoggedIn(false);
                        prefs.setGoogleLoggedIn(false);
                        prefs.setIsRegisterDone(true);
                        prefs.setIsVerified(true);
                        updateUI();
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
