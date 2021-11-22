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
    @Expose(serialize = false, deserialize = false)
    private Integer progress;
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
        progress = calculateProgress();
    }
    public Integer getBookPage() {
        return bookPage;
    }
    public void setBookPage(Integer bookPage) {
        this.bookPage = bookPage;
        progress = calculateProgress();
    }
    public BookCollectionDto getBookCollection() {
        return bookCollection;
    }
    public void setBookCollection(BookCollectionDto bookCollection) {
        this.bookCollection = bookCollection;
    }
    public Integer getProgress() {
        if(progress == null) {
            progress = calculateProgress();
        }
        return progress;
    }
    private Integer calculateProgress() {
        Integer progress = 0;

        if(bookPage != null && numberOfBookPages != null) {
            if(numberOfBookPages != 0) {
                progress = (bookPage * 100) / numberOfBookPages;

                if(progress == 0 && bookPage != 0) {
                    progress = 1;
                }
            }
        }

        return progress;
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