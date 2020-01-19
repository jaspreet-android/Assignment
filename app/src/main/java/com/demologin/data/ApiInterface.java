package com.demologin.data;

import com.demologin.data.respsonses.BaseResponse;
import com.demologin.data.respsonses.NewUserModel;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

public interface ApiInterface {

    //For registering new user
    @FormUrlEncoded
    @POST("user/newregistration")
    Call<NewUserModel> createUser(@Field("email") String email,
                                  @Field("fullname") String fullName,
                                  @Field("is_social") String isSocial,
                                  @Field("password") String password);

    //For login of the exiting user
    @FormUrlEncoded
    @POST("user/newlogin")
    Call<NewUserModel> userLogin(@Field("email") String email,
                                 @Field("password") String password);


    // For email verification Link
    @FormUrlEncoded
    @POST("user/sendVerificationEmail")
    Call<BaseResponse> sendVerificationEmail(@Field("username") String username,
                                             @Field("session_id") String sessionId,
                                             @Field("email") String email);
}
