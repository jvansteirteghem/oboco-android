package com.gitlab.jeeto.oboco.client;

import com.google.gson.annotations.Expose;

import java.io.Serializable;

public class UserTokenDto implements Serializable {
    @Expose()
    private String token;
    public UserTokenDto() {
        super();
    }
    public String getToken() {
        return token;
    }
    public void setToken(String token) {
        this.token = token;
    }
}