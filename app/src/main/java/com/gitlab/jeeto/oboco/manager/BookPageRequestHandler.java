package com.gitlab.jeeto.oboco.manager;

import android.net.Uri;

import com.squareup.picasso.RequestHandler;

public abstract class BookPageRequestHandler extends RequestHandler {
    public final static String HANDLER_URI = "book-page";

    public Uri getPageUri(int page) {
        return new Uri.Builder()
                .scheme(HANDLER_URI)
                .authority("")
                .appendQueryParameter("page", Integer.toString(page))
                .build();
    }
}
