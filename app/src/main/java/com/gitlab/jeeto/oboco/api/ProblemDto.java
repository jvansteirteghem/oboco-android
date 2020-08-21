package com.gitlab.jeeto.oboco.api;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class ProblemDto implements Serializable {
    private Integer statusCode;
    private String code;
    private String description;
    public ProblemDto() {
        super();
    }
    public ProblemDto(Integer statusCode, String code, String description) {
        super();
        this.statusCode = statusCode;
        this.code = code;
        this.description = description;
    }
    @SerializedName("statusCode")
    public Integer getStatusCode() {
        return statusCode;
    }
    public void setStatusCode(Integer statusCode) {
        this.statusCode = statusCode;
    }
    @SerializedName("code")
    public String getCode() {
        return code;
    }
    public void setCode(String code) {
        this.code = code;
    }
    @SerializedName("description")
    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }
}