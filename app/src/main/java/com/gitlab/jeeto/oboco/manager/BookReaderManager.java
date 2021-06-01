package com.gitlab.jeeto.oboco.manager;

import android.net.Uri;
import android.os.Bundle;

import com.squareup.picasso.RequestHandler;

public abstract class BookReaderManager extends RequestHandler {
    public static final String PARAM_MODE = "PARAM_MODE";
    public enum Mode {
        MODE_REMOTE,
        MODE_LOCAL;
    }
    public abstract void create(Bundle savedInstanceState);
    public abstract void destroy();
    public abstract void saveInstanceState(Bundle outState);
    public abstract void load();
    public abstract void addBookMark(int bookPage);
    public abstract Uri getBookPageUri(int bookPage);
}
