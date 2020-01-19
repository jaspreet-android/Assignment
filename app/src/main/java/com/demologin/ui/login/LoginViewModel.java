package com.demologin.ui.login;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.text.TextUtils;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.demologin.R;
import com.demologin.data.LoginRepository;
import com.demologin.data.Result;
import com.demologin.data.model.LoggedInUser;
import com.demologin.utils.Utils;

public class LoginViewModel extends ViewModel {

    private MutableLiveData<LoginFormState> loginFormState = new MutableLiveData<>();
    private MutableLiveData<LoginResult> loginResult = new MutableLiveData<>();
    private LoginRepository loginRepository;

    LoginViewModel(LoginRepository loginRepository) {
        this.loginRepository = loginRepository;
    }

    LiveData<LoginFormState> getLoginFormState() {
        return loginFormState;
    }

    LiveData<LoginResult> getLoginResult() {
        return loginResult;
    }

    @SuppressLint("StaticFieldLeak")
    private final class LoginOperation extends AsyncTask<String, String, Result<LoggedInUser>> {

        @Override
        protected Result<LoggedInUser> doInBackground(String... params) {
            return loginRepository.login(params[0], params[1]);
        }

        @Override
        protected void onPostExecute(Result<LoggedInUser> result) {
            if (result instanceof Result.Success) {
                LoggedInUser data = ((Result.Success<LoggedInUser>) result).getData();
                loginResult.setValue(new LoginResult(new LoggedInUserView(data.getDisplayName())));
            } else if (result instanceof Result.Error) {
                String errMessage = ((Result.Error) result).getErrMessage();
                loginResult.setValue(new LoginResult(errMessage));
            } else {
                loginResult.setValue(new LoginResult(R.string.login_failed));
            }
        }
    }

    public void login(String username, String password) {
        // can be launched in a separate asynchronous job
        new LoginOperation().execute(username, password);
    }

    public void loginDataChanged(String username, String password) {
        if (!isUserNameValid(username)) {
            loginFormState.setValue(new LoginFormState(R.string.invalid_username, null));
        } else if (!isPasswordValid(password)) {
            loginFormState.setValue(new LoginFormState(null, R.string.invalid_password));
        } else {
            loginFormState.setValue(new LoginFormState(true));
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
}
