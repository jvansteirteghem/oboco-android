package com.gitlab.jeeto.oboco.api;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.Date;

public class BookDto implements Serializable {
    private Long id;
    private Date updateDate;
    private String name;
    private Integer numberOfPages;
    private BookCollectionDto bookCollection;
    private BookMarkDto bookMark;
    public BookDto() {
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
    @SerializedName("name")
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    @SerializedName("numberOfPages")
    public Integer getNumberOfPages() {
        return numberOfPages;
    }
    public void setNumberOfPages(Integer numberOfPages) {
        this.numberOfPages = numberOfPages;
    }
    @SerializedName("bookCollection")
    public BookCollectionDto getBookCollection() {
        return bookCollection;
    }
    public void setBookCollection(BookCollectionDto bookCollection) {
        this.bookCollection = bookCollection;
    }
    @SerializedName("bookMark")
    public BookMarkDto getBookMark() {
        return bookMark;
    }
    public void setBookMark(BookMarkDto bookMark) {
        this.bookMark = bookMark;
    }
}