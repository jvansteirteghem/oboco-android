package com.gitlab.jeeto.oboco.fragment;

import android.net.Uri;

import com.gitlab.jeeto.oboco.client.ApplicationService;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Request;
import com.squareup.picasso.RequestHandler;

import java.io.IOException;
import java.io.InputStream;

import okhttp3.ResponseBody;
import okio.Okio;

public class RemoteBookReaderRequestHandler extends BookReaderRequestHandler {
    private ApplicationService mApplicationService;
    private Long mBookId;

    public RemoteBookReaderRequestHandler(ApplicationService applicationService, Long bookId) {
        mApplicationService = applicationService;
        mBookId = bookId;
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

            ResponseBody responseBody = mApplicationService.downloadBookPage(mBookId, bookPage, null, null, null).blockingGet();

            InputStream inputStream = responseBody.byteStream();

            return new RequestHandler.Result(Okio.source(inputStream), Picasso.LoadedFrom.NETWORK);
        } else {
            throw new IOException("uri is invalid");
        }
    }
}
