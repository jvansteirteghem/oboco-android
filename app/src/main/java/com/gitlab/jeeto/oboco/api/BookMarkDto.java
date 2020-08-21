package com.gitlab.jeeto.oboco.api;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.Date;

public class BookMarkDto implements Serializable {
    private Long id;
    private Date updateDate;
    private Integer page;
    private BookDto book;
    public BookMarkDto() {
        super();
    }
    @SerializedName("id")
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    @SerializedName("updateDate")
    public Date getUpdateDate() {
        return updateDate;
    }
    public void setUpdateDate(Date updateDate) {
        this.updateDate = updateDate;
    }
    @SerializedName("page")
    public Integer getPage() {
        return page;
    }
    public void setPage(Integer page) {
        this.page = page;
    }
    @SerializedName("book")
    public BookDto getBook() {
        return book;
    }
    public void setBook(BookDto book) {
        this.book = book;
    }
}