package com.gitlab.jeeto.oboco.api;

import com.google.gson.annotations.Expose;

import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

public class BookDto implements Serializable {
    @Expose()
    private Long id;
    @Expose()
    private Date updateDate;
    @Expose()
    private String name;
    @Expose()
    private Integer numberOfPages;
    @Expose()
    private BookCollectionDto bookCollection;
    @Expose()
    private BookMarkDto bookMark;
    @Expose(serialize = false, deserialize = false)
    private String path;
    public BookDto() {
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
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public Integer getNumberOfPages() {
        return numberOfPages;
    }
    public void setNumberOfPages(Integer numberOfPages) {
        this.numberOfPages = numberOfPages;
    }
    public BookCollectionDto getBookCollection() {
        return bookCollection;
    }
    public void setBookCollection(BookCollectionDto bookCollection) {
        this.bookCollection = bookCollection;
    }
    public BookMarkDto getBookMark() {
        return bookMark;
    }
    public void setBookMark(BookMarkDto bookMark) {
        this.bookMark = bookMark;
    }
    public String getPath() {
        return path;
    }
    public void setPath(String path) {
        this.path = path;
    }
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BookDto that = (BookDto) o;
        return Objects.equals(id, that.id) && Objects.equals(path, that.path);
    }
    @Override
    public int hashCode() {
        return Objects.hash(id, path);
    }
}