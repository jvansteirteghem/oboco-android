package com.gitlab.jeeto.oboco.api;

import com.google.gson.annotations.Expose;

import java.io.Serializable;
import java.util.Date;

public class BookMarkDto implements Serializable {
    @Expose()
    private Long id;
    @Expose()
    private Date updateDate;
    @Expose()
    private Integer page;
    @Expose()
    private BookDto book;
    public BookMarkDto() {
        super();
    }
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public Date getUpdateDate() {
        return updateDate;
    }
    public void setUpdateDate(Date updateDate) {
        this.updateDate = updateDate;
    }
    public Integer getPage() {
        return page;
    }
    public void setPage(Integer page) {
        this.page = page;
    }
    public BookDto getBook() {
        return book;
    }
    public void setBook(BookDto book) {
        this.book = book;
    }
}