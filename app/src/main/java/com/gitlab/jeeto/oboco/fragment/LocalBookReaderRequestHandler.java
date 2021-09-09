package com.gitlab.jeeto.oboco.fragment;

import android.net.Uri;

import com.gitlab.jeeto.oboco.reader.BookReader;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Request;
import com.squareup.picasso.RequestHandler;

import java.io.IOException;
import java.io.InputStream;

import okio.Okio;

public class LocalBookReaderRequestHandler extends BookReaderRequestHandler {
    private BookReader mBookReader;

    public LocalBookReaderRequestHandler(BookReader bookReader) {
        mBookReader = bookReader;
    }

    public Uri getBookPageUri(int bookPage) {
        return new Uri.Builder()
                .scheme("bookReader")
                .authority("")
                .path("/bookPage")
                .appendQueryParameter("bookPage", Integer.toString(bookPage))
                .build();
    }

    @Override
    public boolean canHandleRequest(Request request) {
        return request.uri.getScheme().equals("bookReader");
    }

    @Override
    public Result load(Request request, int networkPolicy) throws IOException {
        if(request.uri.getPath().equals("/bookPage")) {
            Integer bookPage = Integer.parseInt(request.uri.getQueryParameter("bookPage"));

            InputStream inputStream = mBookReader.getPage(bookPage);

            return new RequestHandler.Result(Okio.source(inputStream), Picasso.LoadedFrom.DISK);
        } else {
            throw new IOException("uri is invalid");
        }
    }
}
