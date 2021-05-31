package com.gitlab.jeeto.oboco.manager;

import android.net.Uri;

import com.gitlab.jeeto.oboco.api.BookDto;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Request;
import com.squareup.picasso.RequestHandler;

import java.io.IOException;
import java.io.InputStream;

import okio.Okio;

public class BookBrowserRequestHandler extends RequestHandler {
    private final static String HANDLER_URI = "book-browser";
    private BookBrowserManager mBookBrowserManager;

    public BookBrowserRequestHandler(BookBrowserManager bookBrowserManager) {
        mBookBrowserManager = bookBrowserManager;
    }

    @Override
    public boolean canHandleRequest(Request request) {
        return HANDLER_URI.equals(request.uri.getScheme());
    }

    @Override
    public Result load(Request request, int networkPolicy) throws IOException {
        Long bookId = Long.parseLong(request.uri.getQueryParameter("bookId"));
        String scaleType = request.uri.getQueryParameter("scaleType");
        Integer scaleWidth = Integer.parseInt(request.uri.getQueryParameter("scaleWidth"));
        Integer scaleHeight = Integer.parseInt(request.uri.getQueryParameter("scaleHeight"));

        BookDto book = new BookDto();
        book.setId(bookId);

        InputStream inputStream = mBookBrowserManager.getBookPage(book, scaleType, scaleWidth, scaleHeight);

        return new Result(Okio.source(inputStream), Picasso.LoadedFrom.NETWORK);
    }

    public static Uri getBookPage(BookDto book, String scaleType, int scaleWidth, int scaleHeight) {
        Long bookId = book.getId();

        return new Uri.Builder()
                .scheme(HANDLER_URI)
                .authority("")
                .appendQueryParameter("bookId", Long.toString(bookId))
                .appendQueryParameter("scaleType", scaleType)
                .appendQueryParameter("scaleWidth", Integer.toString(scaleWidth))
                .appendQueryParameter("scaleHeight", Integer.toString(scaleHeight))
                .build();
    }
}
