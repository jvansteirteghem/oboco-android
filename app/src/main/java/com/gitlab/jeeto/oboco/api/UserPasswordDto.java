package com.gitlab.jeeto.oboco.api;

import com.google.gson.annotations.Expose;

import java.io.Serializable;

public class UserPasswordDto implements Serializable {
    @Expose()
    private String password;
    @Expose()
    private String updatePassword;
    public UserPasswordDto() {
        super();
    }
    public String getPassword() {
        return password;
    }
    public void setPassword(String password) {
        this.password = password;
    }
    public String getUpdatePassword() {
        return updatePassword;
    }
    public void setUpdatePassword(String updatePassword) {
        this.updatePassword = updatePassword;
    }
}
