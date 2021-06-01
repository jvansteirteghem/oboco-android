package com.gitlab.jeeto.oboco.manager;

import android.net.Uri;
import android.os.Bundle;

import com.gitlab.jeeto.oboco.api.BookDto;
import com.squareup.picasso.RequestHandler;

public abstract class BookBrowserManager extends RequestHandler {
    public abstract void create(Bundle savedInstanceState);
    public abstract void destroy();
    public abstract void saveInstanceState(Bundle outState);
    public abstract void load(String bookMarkStatus, int page, int pageSize);
    public abstract void loadBookPageableList(String bookMarkStatus, int page, int pageSize);
    public abstract void addBookMark(BookDto book);
    public abstract void removeBookMark(BookDto book);
    public abstract Uri getBookPageUri(BookDto book, String scaleType, int scaleWidth, int scaleHeight);
}
