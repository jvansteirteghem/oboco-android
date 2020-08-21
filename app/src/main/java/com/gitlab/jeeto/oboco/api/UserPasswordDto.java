package com.gitlab.jeeto.oboco.api;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class UserPasswordDto implements Serializable {
    private String password;
    private String updatePassword;

    public UserPasswordDto() {
        super();
    }

    @SerializedName("password")
    public String getPassword() {
        return password;
    }
    public void setPassword(String password) {
        this.password = password;
    }

    @SerializedName("updatePassword")
    public String getUpdatePassword() {
        return updatePassword;
    }
    public void setUpdatePassword(String updatePassword) {
        this.updatePassword = updatePassword;
    }
}
