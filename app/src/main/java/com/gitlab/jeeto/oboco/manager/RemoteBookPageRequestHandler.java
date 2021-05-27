package com.gitlab.jeeto.oboco.manager;

import com.gitlab.jeeto.oboco.api.ApplicationService;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Request;

import java.io.IOException;
import java.io.InputStream;

import okhttp3.ResponseBody;
import okio.Okio;

public class RemoteBookPageRequestHandler extends BookPageRequestHandler {
    private ApplicationService mApplicationService;
    private Long mBookId;

    public RemoteBookPageRequestHandler(ApplicationService applicationService, Long bookId) {
        mApplicationService = applicationService;
        mBookId = bookId;
    }

    @Override
    public boolean canHandleRequest(Request request) {
        return HANDLER_URI.equals(request.uri.getScheme());
    }

    @Override
    public Result load(Request request, int networkPolicy) throws IOException {
        Integer page = Integer.parseInt(request.uri.getQueryParameter("page"));

        ResponseBody responseBody = mApplicationService.downloadBookPage(mBookId, page, null, null, null).blockingGet();

        InputStream inputStream = responseBody.byteStream();

        return new Result(Okio.source(inputStream), Picasso.LoadedFrom.NETWORK);
    }
}
