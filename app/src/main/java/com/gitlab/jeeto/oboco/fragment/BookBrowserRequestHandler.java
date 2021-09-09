package com.gitlab.jeeto.oboco.fragment;

import android.net.Uri;

import com.gitlab.jeeto.oboco.client.BookDto;
import com.squareup.picasso.RequestHandler;

public abstract class BookBrowserRequestHandler extends RequestHandler {
    public abstract Uri getBookPageUri(BookDto bookDto, String scaleType, int scaleWidth, int scaleHeight);
}
