package com.gitlab.jeeto.oboco.api;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

public class PageableListDto<T> implements Serializable {
    private List<T> elements;
    private Long numberOfElements;
    private Integer page;
    private Integer pageSize;
    private Integer firstPage;
    private Integer lastPage;
    private Integer previousPage;
    private Integer nextPage;
    public PageableListDto() {
        super();
    }
    @SerializedName("elements")
    public List<T> getElements() {
        return elements;
    }
    public void setElements(List<T> elements) {
        this.elements = elements;
    }
    @SerializedName("numberOfElements")
    public Long getNumberOfElements() {
        return numberOfElements;
    }
    public void setNumberOfElements(Long numberOfElements) {
        this.numberOfElements = numberOfElements;
    }
    @SerializedName("page")
    public Integer getPage() {
        return page;
    }
    public void setPage(Integer page) {
        this.page = page;
    }
    @SerializedName("pageSize")
    public Integer getPageSize() {
        return pageSize;
    }
    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }
    @SerializedName("firstPage")
    public Integer getFirstPage() {
        return firstPage;
    }
    public void setFirstPage(Integer firstPage) {
        this.firstPage = firstPage;
    }
    @SerializedName("lastPage")
    public Integer getLastPage() {
        return lastPage;
    }
    public void setLastPage(Integer lastPage) {
        this.lastPage = lastPage;
    }
    @SerializedName("previousPage")
    public Integer getPreviousPage() {
        return previousPage;
    }
    public void setPreviousPage(Integer previousPage) {
        this.previousPage = previousPage;
    }
    @SerializedName("nextPage")
    public Integer getNextPage() {
        return nextPage;
    }
    public void setNextPage(Integer nextPage) {
        this.nextPage = nextPage;
    }
}
