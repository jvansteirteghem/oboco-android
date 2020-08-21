package com.gitlab.jeeto.oboco.api;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

public class BookCollectionDto implements Serializable {
    private Long id;
    private Date updateDate;
    private String name;
    private BookCollectionDto parentBookCollection;
    private List<BookCollectionDto> bookCollections;
    private Integer numberOfBookCollections;
    private List<BookDto> books;
    private Integer numberOfBooks;
    public BookCollectionDto() {
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
    @SerializedName("parentBookCollection")
    public BookCollectionDto getParentBookCollection() {
        return parentBookCollection;
    }
    public void setParentBookCollection(BookCollectionDto parentBookCollection) {
        this.parentBookCollection = parentBookCollection;
    }
    @SerializedName("bookCollections")
    public List<BookCollectionDto> getBookCollections() {
        return bookCollections;
    }
    public void setBookCollections(List<BookCollectionDto> bookCollections) {
        this.bookCollections = bookCollections;
    }
    @SerializedName("numberOfBookCollections")
    public Integer getNumberOfBookCollections() {
        return numberOfBookCollections;
    }
    public void setNumberOfBookCollections(Integer numberOfBookCollections) {
        this.numberOfBookCollections = numberOfBookCollections;
    }
    @SerializedName("books")
    public List<BookDto> getBooks() {
        return books;
    }
    public void setBooks(List<BookDto> books) {
        this.books = books;
    }
    @SerializedName("numberOfBooks")
    public Integer getNumberOfBooks() {
        return numberOfBooks;
    }
    public void setNumberOfBooks(Integer numberOfBooks) {
        this.numberOfBooks = numberOfBooks;
    }
}