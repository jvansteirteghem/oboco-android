package com.gitlab.jeeto.oboco.api;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

public class UserIdDto implements Serializable {
    private String name;
    private List<String> roles;
    private String accessToken;
    private String refreshToken;
    public UserIdDto() {
        super();
    }
    @SerializedName("name")
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    @SerializedName("roles")
    public List<String> getRoles() {
        return roles;
    }
    public void setRoles(List<String> roles) {
        this.roles = roles;
    }
    @SerializedName("accessToken")
    public String getAccessToken() {
        return accessToken;
    }
    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }
    @SerializedName("refreshToken")
    public String getRefreshToken() {
        return refreshToken;
    }
    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }
}