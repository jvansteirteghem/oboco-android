package com.gitlab.jeeto.oboco.fragment;

import android.net.Uri;

import com.squareup.picasso.RequestHandler;

public abstract class BookReaderRequestHandler extends RequestHandler {
    public abstract Uri getBookPageUri(int bookPage);
}
