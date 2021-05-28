package com.gitlab.jeeto.oboco.manager;

import android.net.Uri;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Request;
import com.squareup.picasso.RequestHandler;

import java.io.IOException;
import java.io.InputStream;

import okhttp3.ResponseBody;
import okio.Okio;

public class BookReaderRequestHandler extends RequestHandler {
    private final static String HANDLER_URI = "book-reader";
    private BookReaderManager mBookReaderManager;

    public BookReaderRequestHandler(BookReaderManager bookReaderManager) {
        mBookReaderManager = bookReaderManager;
    }

    @Override
    public boolean canHandleRequest(Request request) {
        return HANDLER_URI.equals(request.uri.getScheme());
    }

    @Override
    public Result load(Request request, int networkPolicy) throws IOException {
        Integer bookPage = Integer.parseInt(request.uri.getQueryParameter("bookPage"));

        InputStream inputStream = mBookReaderManager.getBookPage(bookPage);

        if(mBookReaderManager instanceof LocalBookReaderManager) {
            return new Result(Okio.source(inputStream), Picasso.LoadedFrom.DISK);
        } else {
            return new Result(Okio.source(inputStream), Picasso.LoadedFrom.NETWORK);
        }
    }

    public static Uri getBookPage(int bookPage) {
        return new Uri.Builder()
                .scheme(HANDLER_URI)
                .authority("")
                .appendQueryParameter("bookPage", Integer.toString(bookPage))
                .build();
    }
}
