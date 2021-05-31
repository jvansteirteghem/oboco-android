package com.gitlab.jeeto.oboco.manager;

import android.net.Uri;

import com.gitlab.jeeto.oboco.api.BookDto;
import com.gitlab.jeeto.oboco.fragment.LibraryBrowserFragment;

import java.io.IOException;
import java.io.InputStream;

public abstract class BookBrowserManager {
    public abstract void create(LibraryBrowserFragment libraryBrowserFragment);
    public abstract void destroy();
    public abstract void load(String bookMarkStatus, int page, int pageSize);
    public abstract void loadBookPageableList(String bookMarkStatus, int page, int pageSize);
    public abstract void addBookMark(BookDto book);
    public abstract void removeBookMark(BookDto book);
    public abstract InputStream getBookPage(BookDto book, String scaleType, int scaleWidth, int scaleHeight) throws IOException;
}
