package com.demologin.data;

import com.demologin.AppController;
import com.demologin.data.model.LoggedInUser;
import com.demologin.data.respsonses.NewUserModel;
import com.demologin.utils.Prefs;

import java.io.IOException;

import retrofit2.Call;
import retrofit2.Response;

/**
 * Class that handles authentication w/ login credentials and retrieves user information.
 */
public class LoginDataSource {

    public Result<LoggedInUser> login(String username, String password) {
        return loginApiCall(username, password);
    }

    public void logout() {
        // TODO: revoke authentication
    }

    // sending data to the sever and receiving user id and session id
    private Result loginApiCall(String email, String password) {
        ApiInterface apiInterface = ApiClient.getRetrofitInstance().create(ApiInterface.class);
        Call<NewUserModel> loginCall = apiInterface.userLogin(email, password);
        try {
            Response<NewUserModel> response = loginCall.execute();
            NewUserModel user = response.body();
            if (null != user) {
                int userId = user.username;
                String statusMsg = user.getStatus();
                String isVerified = user.isVerified;
                if (userId >= 0) {
                    if (statusMsg.equals("1")) {
                        Prefs.getPrefs(AppController.getInstance()).setIsVerified(isVerified.equals("1"));
                        return new Result.Success<>(new LoggedInUser(String.valueOf(userId), user.fullName));
                    } else {
                        return new Result.Error(user.getMsg());
                    }
                } else {
                    return new Result.Error(user.getMsg());
                }
            } else {
                return new Result.Error(response.message());
            }
        } catch (IOException e) {
            return new Result.Error(new IOException("Error logging in", e));
        }
    }
}
