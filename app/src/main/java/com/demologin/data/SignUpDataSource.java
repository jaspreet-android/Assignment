package com.demologin.data;

import com.demologin.AppController;
import com.demologin.data.model.SignedUpUser;
import com.demologin.data.respsonses.BaseResponse;
import com.demologin.data.respsonses.NewUserModel;
import com.demologin.utils.Prefs;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Class that handles authentication w/ login credentials and retrieves user information.
 */
public class SignUpDataSource {
    public Result<SignedUpUser> signUp(String email, String fullname, String password) {

        return signUpApiCall(email, fullname, password);
    }

    public void logout() {
        // TODO: revoke authentication
    }

    // sending data to the sever and receiving user id and session id
    private Result<SignedUpUser> signUpApiCall(String email, String fullName, String password) {
        ApiInterface apiInterface = ApiClient.getRetrofitInstance().create(ApiInterface.class);
        Call<NewUserModel> signUpCall = apiInterface.createUser(email, fullName, "0", password);
        try {
            Response<NewUserModel> response = signUpCall.execute();
            NewUserModel user = response.body();
            if (null != user) {
                int userId = user.username;
                String statusMsg = user.getStatus();
                if (userId >= 0) {
                    if (statusMsg.equals("1")) {
                        sendVerificationEmailApiCall(email, String.valueOf(userId), user.getSessionId());
                        AppController instance = AppController.getInstance();
                        Prefs.getPrefs(instance).setEmail(email);
                        Prefs.getPrefs(instance).setUserPassword(password);
                        Prefs.getPrefs(instance).setIsSocial(false);
                        return new Result.Success<>(new SignedUpUser(String.valueOf(userId), email, user.fullName));
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
            return new Result.Error(new IOException("Error Signing up", e));
        }
    }

    private void sendVerificationEmailApiCall(String email, String username, String password) {
        ApiInterface apiInterface = ApiClient.getRetrofitInstance().create(ApiInterface.class);
        Call<BaseResponse> signUpCall = apiInterface.sendVerificationEmail(username, password, email);
        signUpCall.enqueue(new Callback<BaseResponse>() {
            @Override
            public void onResponse(@NotNull Call<BaseResponse> call, @NotNull Response<BaseResponse> response) {

            }

            @Override
            public void onFailure(@NotNull Call<BaseResponse> call, @NotNull Throwable t) {

            }
        });
    }
}
