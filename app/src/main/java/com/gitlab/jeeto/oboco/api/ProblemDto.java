package com.gitlab.jeeto.oboco.api;

import com.google.gson.annotations.Expose;

import java.io.Serializable;

public class ProblemDto implements Serializable {
    @Expose()
    private Integer statusCode;
    @Expose()
    private String code;
    @Expose()
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
    public Integer getStatusCode() {
        return statusCode;
    }
    public void setStatusCode(Integer statusCode) {
        this.statusCode = statusCode;
    }
    public String getCode() {
        return code;
    }
    public void setCode(String code) {
        this.code = code;
    }
    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }
}