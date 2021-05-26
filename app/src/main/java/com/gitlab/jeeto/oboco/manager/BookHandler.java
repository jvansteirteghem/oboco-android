package com.gitlab.jeeto.oboco.manager;

import android.net.Uri;

import com.squareup.picasso.RequestHandler;

public abstract class BookHandler extends RequestHandler {
    public abstract Uri getPageUri(int page);
}
