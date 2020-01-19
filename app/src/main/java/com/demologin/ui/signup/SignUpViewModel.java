package com.demologin.ui.signup;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.text.TextUtils;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.demologin.R;
import com.demologin.data.Result;
import com.demologin.data.SignUpRepository;
import com.demologin.data.model.SignedUpUser;
import com.demologin.utils.Utils;

public class SignUpViewModel extends ViewModel {

    private MutableLiveData<SignUpFormState> signUpFormState = new MutableLiveData<>();
    private MutableLiveData<SignUpResult> signUpResult = new MutableLiveData<>();
    private SignUpRepository signUpRepository;

    SignUpViewModel(SignUpRepository signUpRepository) {
        this.signUpRepository = signUpRepository;
    }

    LiveData<SignUpFormState> getSignUpFormState() {
        return signUpFormState;
    }

    LiveData<SignUpResult> getSignUpResult() {
        return signUpResult;
    }

    @SuppressLint("StaticFieldLeak")
    private final class SignUpOperation extends AsyncTask<String, String, Result<SignedUpUser>> {

        @Override
        protected Result<SignedUpUser> doInBackground(String... params) {
            return signUpRepository.signUp(params[0], params[1], params[2]);
        }

        @Override
        protected void onPostExecute(Result<SignedUpUser> result) {
            if (result instanceof Result.Success) {
                SignedUpUser data = ((Result.Success<SignedUpUser>) result).getData();
                signUpResult.setValue(new SignUpResult(new SignUpUserView(data.getDisplayName())));
            } else if (result instanceof Result.Error) {
                String errMessage = ((Result.Error) result).getErrMessage();
                signUpResult.setValue(new SignUpResult(errMessage));
            } else {
                signUpResult.setValue(new SignUpResult(R.string.login_failed));
            }
        }
    }

    public void signUp(String email, String fullName, String password) {
        // can be launched in a separate asynchronous job
        new SignUpOperation().execute(email, fullName, password);
    }

    public void signUpDataChanged(String username, String password, String cnfPassword, String number) {
        if (!isUserNameValid(username)) {
            signUpFormState.setValue(new SignUpFormState(R.string.invalid_username, null, null, null));
        } else if (!isNumberValid(number)) {
            signUpFormState.setValue(new SignUpFormState(null, null, null, R.string.invalid_number));
        } else if (!isPasswordValid(password)) {
            signUpFormState.setValue(new SignUpFormState(null, R.string.invalid_password, null, null));
        } else if (!isCnfPasswordValid(cnfPassword)) {
            signUpFormState.setValue(new SignUpFormState(null, null, R.string.invalid_password, null));
        } else if (!isBothPasswordMatch(password, cnfPassword)) {
            signUpFormState.setValue(new SignUpFormState(null, null, R.string.password_not_matched, null));
        } else {
            signUpFormState.setValue(new SignUpFormState(true));
        }
    }

    // A placeholder username validation check
    private boolean isUserNameValid(String username) {
        if (TextUtils.isEmpty(username)) {
            return false;
        }
        if (!Utils.isValidEmail(username)) {
            return false;
        } else {
            return true;
        }
    }

    // A placeholder password validation check
    private boolean isPasswordValid(String password) {
        return password != null && password.trim().length() > 5;
    }

    // A placeholder password validation check
    private boolean isNumberValid(String number) {
        return number != null && number.trim().length() > 8;
    }

    // A placeholder cnf password validation check
    private boolean isCnfPasswordValid(String password) {
        return password != null && password.trim().length() > 5;
    }

    // A placeholder cnf password validation check
    private boolean isBothPasswordMatch(String password, String cnfPassword) {
        return cnfPassword.equals(password);
    }
}
