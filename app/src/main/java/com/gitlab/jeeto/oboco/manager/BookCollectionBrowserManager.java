package com.gitlab.jeeto.oboco.manager;

import android.net.Uri;
import android.os.Bundle;

import com.gitlab.jeeto.oboco.client.BookCollectionDto;
import com.squareup.picasso.RequestHandler;

public abstract class BookCollectionBrowserManager extends RequestHandler {
    public static final String PARAM_MODE = "PARAM_MODE";
    public enum Mode {
        MODE_REMOTE,
        MODE_REMOTE_LATEST;
    }
    public abstract void create(Bundle savedInstanceState);
    public abstract void destroy();
    public abstract void saveInstanceState(Bundle outState);
    public abstract void load(String bookCollectionName, int page, int pageSize);
    public abstract void loadBookCollectionPageableList(String bookCollectionName, int page, int pageSize);
    public abstract void addBookMark(BookCollectionDto bookCollectionDto);
    public abstract void removeBookMark(BookCollectionDto bookCollectionDto);
    public abstract Uri getBookCollectionPageUri(BookCollectionDto bookCollectionDto, String scaleType, int scaleWidth, int scaleHeight);
}
