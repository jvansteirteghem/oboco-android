package com.gitlab.jeeto.oboco.manager;

import android.net.Uri;

import com.gitlab.jeeto.oboco.reader.BookReader;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Request;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import okio.Okio;

public class LocalBookHandler extends BookHandler {
    private final static String HANDLER_URI = "local-book";
    private File mBookFile;
    private BookReader mBookReader;

    public LocalBookHandler(BookReader bookReader, File bookFile) {
        mBookReader = bookReader;
        mBookFile = bookFile;
    }

    @Override
    public boolean canHandleRequest(Request request) {
        return HANDLER_URI.equals(request.uri.getScheme());
    }

    @Override
    public Result load(Request request, int networkPolicy) throws IOException {
        int page = Integer.parseInt(request.uri.getFragment());

        InputStream inputStream = mBookReader.getPage(page);

        return new Result(Okio.source(inputStream), Picasso.LoadedFrom.DISK);
    }

    public Uri getPageUri(int page) {
        return new Uri.Builder()
                .scheme(HANDLER_URI)
                .authority("")
                .fragment(Integer.toString(page))
                .build();
    }
}
