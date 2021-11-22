package com.gitlab.jeeto.oboco.client;

import com.google.gson.annotations.Expose;

import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

public class BookMarkDto implements Serializable {
    @Expose()
    private Long id;
    @Expose()
    private Date createDate;
    @Expose()
    private Date updateDate;
    @Expose()
    private Integer numberOfPages;
    @Expose()
    private Integer page;
    @Expose()
    private BookDto book;
    @Expose(serialize = false, deserialize = false)
    private Integer progress;
    public BookMarkDto() {
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
    public Integer getNumberOfPages() {
        return numberOfPages;
    }
    public void setNumberOfPages(Integer numberOfPages) {
        this.numberOfPages = numberOfPages;
        progress = calculateProgress();
    }
    public Integer getPage() {
        return page;
    }
    public void setPage(Integer page) {
        this.page = page;
        progress = calculateProgress();
    }
    public BookDto getBook() {
        return book;
    }
    public void setBook(BookDto book) {
        this.book = book;
    }
    public Integer getProgress() {
        if(progress == null) {
            progress = calculateProgress();
        }
        return progress;
    }
    private Integer calculateProgress() {
        Integer progress = 0;

        if(page != null && numberOfPages != null) {
            if(numberOfPages != 0) {
                progress = (page * 100) / numberOfPages;

                if(progress == 0 && page != 0) {
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
        BookMarkDto that = (BookMarkDto) o;
        return Objects.equals(id, that.id);
    }
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}