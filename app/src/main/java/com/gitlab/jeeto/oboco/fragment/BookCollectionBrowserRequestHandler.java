package com.gitlab.jeeto.oboco.fragment;

import android.net.Uri;

import com.gitlab.jeeto.oboco.client.BookCollectionDto;
import com.squareup.picasso.RequestHandler;

public abstract class BookCollectionBrowserRequestHandler extends RequestHandler {
    public abstract Uri getBookCollectionPageUri(BookCollectionDto bookCollectionDto, String scaleType, int scaleWidth, int scaleHeight);
}
