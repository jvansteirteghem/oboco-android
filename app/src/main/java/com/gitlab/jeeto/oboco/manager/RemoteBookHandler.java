package com.gitlab.jeeto.oboco.manager;

import android.net.Uri;

import com.gitlab.jeeto.oboco.api.ApplicationService;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Request;

import java.io.IOException;
import java.io.InputStream;

import okhttp3.ResponseBody;
import okio.Okio;

public class RemoteBookHandler extends BookHandler {
    private final static String HANDLER_URI = "remote-book";
    private ApplicationService mApplicationService;
    private Long mBookId;

    public RemoteBookHandler(ApplicationService applicationService, Long bookId) {
        mApplicationService = applicationService;
        mBookId = bookId;
    }

    @Override
    public boolean canHandleRequest(Request request) {
        return HANDLER_URI.equals(request.uri.getScheme());
    }

    @Override
    public Result load(Request request, int networkPolicy) throws IOException {
        Integer page = Integer.parseInt(request.uri.getFragment());

        ResponseBody responseBody = mApplicationService.downloadBookPage(mBookId, page).blockingGet();

        InputStream inputStream = responseBody.byteStream();

        return new Result(Okio.source(inputStream), Picasso.LoadedFrom.NETWORK);
    }

    public Uri getPageUri(int page) {
        return new Uri.Builder()
                .scheme(HANDLER_URI)
                .authority("")
                .fragment(Integer.toString(page))
                .build();
    }
}
