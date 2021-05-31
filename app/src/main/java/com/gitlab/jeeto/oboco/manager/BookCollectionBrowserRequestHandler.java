package com.gitlab.jeeto.oboco.manager;

import android.net.Uri;

import com.gitlab.jeeto.oboco.api.BookCollectionDto;
import com.gitlab.jeeto.oboco.api.BookDto;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Request;
import com.squareup.picasso.RequestHandler;

import java.io.IOException;
import java.io.InputStream;

import okio.Okio;

public class BookCollectionBrowserRequestHandler extends RequestHandler {
    private final static String HANDLER_URI = "book-collection-browser";
    private BookCollectionBrowserManager mBookCollectionBrowserManager;

    public BookCollectionBrowserRequestHandler(BookCollectionBrowserManager bookCollectionBrowserManager) {
        mBookCollectionBrowserManager = bookCollectionBrowserManager;
    }

    @Override
    public boolean canHandleRequest(Request request) {
        return HANDLER_URI.equals(request.uri.getScheme());
    }

    @Override
    public Result load(Request request, int networkPolicy) throws IOException {
        Long bookCollectionId = Long.parseLong(request.uri.getQueryParameter("bookCollectionId"));
        String scaleType = request.uri.getQueryParameter("scaleType");
        Integer scaleWidth = Integer.parseInt(request.uri.getQueryParameter("scaleWidth"));
        Integer scaleHeight = Integer.parseInt(request.uri.getQueryParameter("scaleHeight"));

        BookCollectionDto bookCollection = new BookCollectionDto();
        bookCollection.setId(bookCollectionId);

        InputStream inputStream = mBookCollectionBrowserManager.getBookCollectionPage(bookCollection, scaleType, scaleWidth, scaleHeight);

        return new Result(Okio.source(inputStream), Picasso.LoadedFrom.NETWORK);
    }

    public static Uri getBookCollectionPage(BookCollectionDto bookCollection, String scaleType, int scaleWidth, int scaleHeight) {
        Long bookCollectionId = bookCollection.getId();

        return new Uri.Builder()
                .scheme(HANDLER_URI)
                .authority("")
                .appendQueryParameter("bookCollectionId", Long.toString(bookCollectionId))
                .appendQueryParameter("scaleType", scaleType)
                .appendQueryParameter("scaleWidth", Integer.toString(scaleWidth))
                .appendQueryParameter("scaleHeight", Integer.toString(scaleHeight))
                .build();
    }
}
