package com.gitlab.jeeto.oboco.client;

import com.google.gson.annotations.Expose;

import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

public class BookCollectionMarkDto implements Serializable {
    @Expose()
    private Long id;
    @Expose()
    private Date createDate;
    @Expose()
    private Date updateDate;
    @Expose()
    private Integer numberOfBookPages;
    @Expose()
    private Integer bookPage;
    @Expose()
    private BookCollectionDto bookCollection;
    public BookCollectionMarkDto() {
        super();
    }
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public Date getCreateDate() {
        return createDate;
    }
    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }
    public Date getUpdateDate() {
        return updateDate;
    }
    public void setUpdateDate(Date updateDate) {
        this.updateDate = updateDate;
    }
    public Integer getNumberOfBookPages() {
        return numberOfBookPages;
    }
    public void setNumberOfBookPages(Integer numberOfBookPages) {
        this.numberOfBookPages = numberOfBookPages;
    }
    public Integer getBookPage() {
        return bookPage;
    }
    public void setBookPage(Integer bookPage) {
        this.bookPage = bookPage;
    }
    public BookCollectionDto getBookCollection() {
        return bookCollection;
    }
    public void setBookCollection(BookCollectionDto bookCollection) {
        this.bookCollection = bookCollection;
    }
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BookCollectionMarkDto that = (BookCollectionMarkDto) o;
        return Objects.equals(id, that.id);
    }
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}