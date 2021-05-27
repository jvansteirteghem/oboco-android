package com.gitlab.jeeto.oboco.manager;

import com.gitlab.jeeto.oboco.reader.BookReader;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Request;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import okio.Okio;

public class LocalBookPageRequestHandler extends BookPageRequestHandler {
    private File mBookFile;
    private BookReader mBookReader;

    public LocalBookPageRequestHandler(BookReader bookReader, File bookFile) {
        mBookReader = bookReader;
        mBookFile = bookFile;
    }

    @Override
    public boolean canHandleRequest(Request request) {
        return HANDLER_URI.equals(request.uri.getScheme());
    }

    @Override
    public Result load(Request request, int networkPolicy) throws IOException {
        Integer page = Integer.parseInt(request.uri.getQueryParameter("page"));

        InputStream inputStream = mBookReader.getPage(page);

        return new Result(Okio.source(inputStream), Picasso.LoadedFrom.DISK);
    }
}
