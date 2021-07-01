package com.gitlab.jeeto.oboco.manager;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;

import com.gitlab.jeeto.oboco.api.ApplicationService;
import com.gitlab.jeeto.oboco.api.AuthenticationManager;
import com.gitlab.jeeto.oboco.api.BookCollectionDto;
import com.gitlab.jeeto.oboco.api.PageableListDto;
import com.gitlab.jeeto.oboco.fragment.BookCollectionBrowserFragment;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Request;

import java.io.IOException;
import java.io.InputStream;

import io.reactivex.Completable;
import io.reactivex.CompletableObserver;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import okhttp3.ResponseBody;
import okio.Okio;

public class RemoteLatestBookCollectionBrowserManager extends BookCollectionBrowserManager {
    private BookCollectionBrowserFragment mBookCollectionBrowserFragment;

    private String mBaseUrl;

    private AuthenticationManager mAuthenticationManager;
    private Disposable mAuthenticationManagerDisposable;
    private ApplicationService mApplicationService;

    public RemoteLatestBookCollectionBrowserManager(BookCollectionBrowserFragment bookCollectionBrowserFragment) {
        super();
        mBookCollectionBrowserFragment = bookCollectionBrowserFragment;
    }

    public void create(Bundle savedInstanceState) {
        SharedPreferences sp = mBookCollectionBrowserFragment.getContext().getSharedPreferences("application", Context.MODE_PRIVATE);
        mBaseUrl = sp.getString("baseUrl", "");

        mAuthenticationManager = new AuthenticationManager(mBookCollectionBrowserFragment.getContext());
        Observable<Throwable> observable = mAuthenticationManager.getErrors();
        observable = observable.observeOn(AndroidSchedulers.mainThread());
        observable = observable.subscribeOn(Schedulers.io());
        mAuthenticationManagerDisposable = observable.subscribe(new Consumer<Throwable>() {
            @Override
            public void accept(Throwable e) throws Exception {
                mBookCollectionBrowserFragment.onError(e);
            }
        });

        mApplicationService = new ApplicationService(mBookCollectionBrowserFragment.getContext(), mBaseUrl, mAuthenticationManager);

    }

    public void destroy() {
        mAuthenticationManagerDisposable.dispose();
    }

    @Override
    public void saveInstanceState(Bundle outState) {

    }

    public void load(String bookCollectionName, int page, int pageSize) {
        BookCollectionDto bookCollectionDto = new BookCollectionDto();
        bookCollectionDto.setName("LATEST");

        Single<PageableListDto<BookCollectionDto>> single =  mApplicationService.getLatestBookCollections(bookCollectionName, page, pageSize, "()");
        single = single.observeOn(AndroidSchedulers.mainThread());
        single = single.subscribeOn(Schedulers.io());
        single.subscribe(new SingleObserver<PageableListDto<BookCollectionDto>>() {
            @Override
            public void onSubscribe(Disposable d) {

            }

            @Override
            public void onSuccess(PageableListDto<BookCollectionDto> bookCollectionPageableListDto) {
                mBookCollectionBrowserFragment.onLoad(bookCollectionDto, bookCollectionPageableListDto);
            }

            @Override
            public void onError(Throwable e) {
                mBookCollectionBrowserFragment.onError(e);
            }
        });
    }

    public void loadBookCollectionPageableList(String bookCollectionName, int page, int pageSize) {
        Single<PageableListDto<BookCollectionDto>> single = mApplicationService.getLatestBookCollections(bookCollectionName, page, pageSize, "()");
        single = single.observeOn(AndroidSchedulers.mainThread());
        single = single.subscribeOn(Schedulers.io());
        single.subscribe(new SingleObserver<PageableListDto<BookCollectionDto>>() {
            @Override
            public void onSubscribe(Disposable d) {

            }

            @Override
            public void onSuccess(PageableListDto<BookCollectionDto> bookCollectionPageableListDto) {
                mBookCollectionBrowserFragment.onLoadBookCollectionPageableList(bookCollectionPageableListDto);
            }

            @Override
            public void onError(Throwable e) {
                mBookCollectionBrowserFragment.onError(e);
            }
        });
    }

    public void addBookMark(BookCollectionDto bookCollectionDto) {
        Completable completable = mApplicationService.createOrUpdateBookMarks(bookCollectionDto.getId());
        completable = completable.observeOn(AndroidSchedulers.mainThread());
        completable = completable.subscribeOn(Schedulers.io());
        completable.subscribe(new CompletableObserver() {
            @Override
            public void onSubscribe(Disposable d) {

            }

            @Override
            public void onComplete() {
                mBookCollectionBrowserFragment.onAddBookMark(bookCollectionDto);
            }

            @Override
            public void onError(Throwable e) {
                mBookCollectionBrowserFragment.onError(e);
            }
        });
    }

    public void removeBookMark(BookCollectionDto bookCollectionDto) {
        Completable completable = mApplicationService.deleteBookMarks(bookCollectionDto.getId());
        completable = completable.observeOn(AndroidSchedulers.mainThread());
        completable = completable.subscribeOn(Schedulers.io());
        completable.subscribe(new CompletableObserver() {
            @Override
            public void onSubscribe(Disposable d) {

            }

            @Override
            public void onComplete() {
                mBookCollectionBrowserFragment.onRemoveBookMark(bookCollectionDto);
            }

            @Override
            public void onError(Throwable e) {
                mBookCollectionBrowserFragment.onError(e);
            }
        });
    }

    public Uri getBookCollectionPageUri(BookCollectionDto bookCollectionDto, String scaleType, int scaleWidth, int scaleHeight) {
        return new Uri.Builder()
                .scheme("bookCollectionBrowserManager")
                .authority("")
                .path("/bookCollectionPage")
                .appendQueryParameter("bookCollectionId", Long.toString(bookCollectionDto.getId()))
                .appendQueryParameter("scaleType", scaleType)
                .appendQueryParameter("scaleWidth", Integer.toString(scaleWidth))
                .appendQueryParameter("scaleHeight", Integer.toString(scaleHeight))
                .build();
    }

    @Override
    public boolean canHandleRequest(Request request) {
        return request.uri.getScheme().equals("bookCollectionBrowserManager");
    }

    @Override
    public Result load(Request request, int networkPolicy) throws IOException {
        if(request.uri.getPath().equals("/bookCollectionPage")) {
            Long bookCollectionId = Long.parseLong(request.uri.getQueryParameter("bookCollectionId"));
            String scaleType = request.uri.getQueryParameter("scaleType");
            Integer scaleWidth = Integer.parseInt(request.uri.getQueryParameter("scaleWidth"));
            Integer scaleHeight = Integer.parseInt(request.uri.getQueryParameter("scaleHeight"));

            ResponseBody responseBody = mApplicationService.downloadBookCollectionPage(bookCollectionId, scaleType, scaleWidth, scaleHeight).blockingGet();

            InputStream inputStream = responseBody.byteStream();

            return new Result(Okio.source(inputStream), Picasso.LoadedFrom.NETWORK);
        } else {
            throw new IOException("uri is invalid");
        }
    }
}
