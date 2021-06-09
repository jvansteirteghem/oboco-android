package com.gitlab.jeeto.oboco.api;

import com.google.gson.annotations.Expose;

import java.io.Serializable;

public class LinkableDto<T> implements Serializable {
    @Expose()
    private T element;
    @Expose()
    private T previousElement;
    @Expose()
    private T nextElement;
    public LinkableDto() {
        super();
    }
    public T getElement() {
        return element;
    }
    public void setElement(T element) {
        this.element = element;
    }
    public T getPreviousElement() {
        return previousElement;
    }
    public void setPreviousElement(T previousElement) {
        this.previousElement = previousElement;
    }
    public T getNextElement() {
        return nextElement;
    }
    public void setNextElement(T nextElement) {
        this.nextElement = nextElement;
    }
}
