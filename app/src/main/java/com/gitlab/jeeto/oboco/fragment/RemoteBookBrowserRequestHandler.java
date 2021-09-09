package com.gitlab.jeeto.oboco.fragment;

import android.net.Uri;

import com.gitlab.jeeto.oboco.client.ApplicationService;
import com.gitlab.jeeto.oboco.client.BookDto;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Request;
import com.squareup.picasso.RequestHandler;

import java.io.IOException;
import java.io.InputStream;

import okhttp3.ResponseBody;
import okio.Okio;

public class RemoteBookBrowserRequestHandler extends BookBrowserRequestHandler {
    private ApplicationService mApplicationService;

    public RemoteBookBrowserRequestHandler(ApplicationService applicationService) {
        mApplicationService = applicationService;
    }

    @Override
    public Uri getBookPageUri(BookDto bookDto, String scaleType, int scaleWidth, int scaleHeight) {
        return new Uri.Builder()
                .scheme("bookBrowser")
                .authority("")
                .path("/bookPage")
                .appendQueryParameter("bookId", Long.toString(bookDto.getId()))
                .appendQueryParameter("scaleType", scaleType)
                .appendQueryParameter("scaleWidth", Integer.toString(scaleWidth))
                .appendQueryParameter("scaleHeight", Integer.toString(scaleHeight))
                .build();
    }

    @Override
    public boolean canHandleRequest(Request request) {
        return request.uri.getScheme().equals("bookBrowser");
    }

    @Override
    public RequestHandler.Result load(Request request, int networkPolicy) throws IOException {
        if(request.uri.getPath().equals("/bookPage")) {
            Long bookId = Long.parseLong(request.uri.getQueryParameter("bookId"));
            String scaleType = request.uri.getQueryParameter("scaleType");
            Integer scaleWidth = Integer.parseInt(request.uri.getQueryParameter("scaleWidth"));
            Integer scaleHeight = Integer.parseInt(request.uri.getQueryParameter("scaleHeight"));

            ResponseBody responseBody = mApplicationService.downloadBookPage(bookId, 1, scaleType, scaleWidth, scaleHeight).blockingGet();

            InputStream inputStream = responseBody.byteStream();

            return new RequestHandler.Result(Okio.source(inputStream), Picasso.LoadedFrom.NETWORK);
        } else {
            throw new IOException("uri is invalid");
        }
    }
}
