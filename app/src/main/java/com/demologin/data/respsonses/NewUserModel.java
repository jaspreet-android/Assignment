package com.demologin.data.respsonses;

import com.google.gson.annotations.SerializedName;

public class NewUserModel extends BaseResponse{
    @SerializedName("username")
    public int username;

    @SerializedName("fullname")
    public String fullName;

    @SerializedName("email")
    public String email;

    @SerializedName("session_id")
    public String sessionId;

    @SerializedName("avatar")
    public String avatar;

    @SerializedName("country_code")
    public String countryCode;

    @SerializedName("phone_number")
    public String phoneNumber;

    @SerializedName("is_verified")
    public String isVerified;

    public int getUsername() {
        return username;
    }

    public void setUsername(int username) {
        this.username = username;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getIsVerified() {
        return isVerified;
    }

    public void setIsVerified(String isVerified) {
        this.isVerified = isVerified;
    }
}
