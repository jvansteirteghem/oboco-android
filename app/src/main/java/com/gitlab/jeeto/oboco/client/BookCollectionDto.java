package com.gitlab.jeeto.oboco.client;

import com.google.gson.annotations.Expose;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Objects;

public class BookCollectionDto implements Serializable {
    @Expose()
    private Long id;
    @Expose()
    private Date updateDate;
    @Expose()
    private String name;
    @Expose()
    private BookCollectionDto parentBookCollection;
    @Expose()
    private List<BookCollectionDto> bookCollections;
    @Expose()
    private Integer numberOfBookCollections;
    @Expose()
    private List<BookDto> books;
    @Expose()
    private Integer numberOfBooks;
    @Expose(serialize = false, deserialize = false)
    private String path;
    public BookCollectionDto() {
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
    public BookCollectionDto getParentBookCollection() {
        return parentBookCollection;
    }
    public void setParentBookCollection(BookCollectionDto parentBookCollection) {
        this.parentBookCollection = parentBookCollection;
    }
    public List<BookCollectionDto> getBookCollections() {
        return bookCollections;
    }
    public void setBookCollections(List<BookCollectionDto> bookCollections) {
        this.bookCollections = bookCollections;
    }
    public Integer getNumberOfBookCollections() {
        return numberOfBookCollections;
    }
    public void setNumberOfBookCollections(Integer numberOfBookCollections) {
        this.numberOfBookCollections = numberOfBookCollections;
    }
    public List<BookDto> getBooks() {
        return books;
    }
    public void setBooks(List<BookDto> books) {
        this.books = books;
    }
    public Integer getNumberOfBooks() {
        return numberOfBooks;
    }
    public void setNumberOfBooks(Integer numberOfBooks) {
        this.numberOfBooks = numberOfBooks;
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
        BookCollectionDto that = (BookCollectionDto) o;
        return Objects.equals(id, that.id) && Objects.equals(path, that.path);
    }
    @Override
    public int hashCode() {
        return Objects.hash(id, path);
    }
}