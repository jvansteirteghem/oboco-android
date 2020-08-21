package com.gitlab.jeeto.oboco.api;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class UserTokenDto implements Serializable {
    private String token;
    public UserTokenDto() {
        super();
    }
    @SerializedName("token")
    public String getToken() {
        return token;
    }
    public void setToken(String token) {
        this.token = token;
    }
}