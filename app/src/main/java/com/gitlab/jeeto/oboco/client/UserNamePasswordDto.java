package com.gitlab.jeeto.oboco.client;

import com.google.gson.annotations.Expose;

import java.io.Serializable;

public class UserNamePasswordDto implements Serializable {
    @Expose()
    private String name;
    @Expose()
    private String password;
    public UserNamePasswordDto() {
        super();
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getPassword() {
        return password;
    }
    public void setPassword(String password) {
        this.password = password;
    }
}