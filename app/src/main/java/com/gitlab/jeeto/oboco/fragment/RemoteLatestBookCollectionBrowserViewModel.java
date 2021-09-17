package com.gitlab.jeeto.oboco.fragment;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;

import com.gitlab.jeeto.oboco.client.ApplicationService;
import com.gitlab.jeeto.oboco.client.AuthenticationManager;
import com.gitlab.jeeto.oboco.client.BookCollectionDto;
import com.gitlab.jeeto.oboco.client.PageableListDto;
import com.squareup.picasso.Picasso;

import java.util.List;

import io.reactivex.Completable;
import io.reactivex.CompletableObserver;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class RemoteLatestBookCollectionBrowserViewModel extends BookCollectionBrowserViewModel {
    private Integer mPage;
    private Integer mPageSize;
    private Integer mNextPage;
    private Integer mNextPageSize;

    private String mBaseUrl;

    private AuthenticationManager mAuthenticationManager;
    private Disposable mAuthenticationManagerDisposable;
    private ApplicationService mApplicationService;

    private BookCollectionBrowserRequestHandler mRequestHandler;
    private Picasso mPicasso;

    public RemoteLatestBookCollectionBrowserViewModel(Application application, Bundle arguments) {
        super(application, arguments);

        mPage = 1;
        mPageSize = 100;
        mNextPage = null;
        mNextPageSize = 100;

        SharedPreferences sp = getApplication().getSharedPreferences("application", Context.MODE_PRIVATE);
        mBaseUrl = sp.getString("baseUrl", "");

        mAuthenticationManager = new AuthenticationManager(getApplication().getApplicationContext());
        Observable<Throwable> observable = mAuthenticationManager.getErrors();
        observable = observable.observeOn(AndroidSchedulers.mainThread());
        observable = observable.subscribeOn(Schedulers.io());
        mAuthenticationManagerDisposable = observable.subscribe(new Consumer<Throwable>() {
            @Override
            public void accept(Throwable e) throws Exception {
                mMessageObservable.setValue(toMessage(e));
                mShowMessageObservable.setValue(true);
            }
        });

        mApplicationService = new ApplicationService(getApplication().getApplicationContext(), mBaseUrl, mAuthenticationManager);

        mRequestHandler = new RemoteBookCollectionBrowserRequestHandler(mApplicationService);

        mPicasso = new Picasso.Builder(getApplication())
                .addRequestHandler(mRequestHandler)
                .listener(new Picasso.Listener() {
                    @Override
                    public void onImageLoadFailed(Picasso picasso, Uri uri, Exception exception) {
                        mMessageObservable.setValue(toMessage(exception));
                        mShowMessageObservable.setValue(true);
                    }
                })
                //.loggingEnabled(true)
                //.indicatorsEnabled(true)
                .build();

        load();
    }

    @Override
    protected void onCleared() {
        mAuthenticationManagerDisposable.dispose();
    }

    @Override
    public void load() {
        BookCollectionDto bookCollection = new BookCollectionDto();
        bookCollection.setName("LATEST");
        mBookCollectionObservable.setValue(bookCollection);

        loadBookCollectionList();
    }

    @Override
    public void loadBookCollectionList() {
        mIsLoadingObservable.setValue(true);

        Single<PageableListDto<BookCollectionDto>> single =  mApplicationService.getLatestBookCollections(mBookCollectionNameObservable.getValue(), mPage, mPageSize, "()");
        single = single.observeOn(AndroidSchedulers.mainThread());
        single = single.subscribeOn(Schedulers.io());
        single.subscribe(new SingleObserver<PageableListDto<BookCollectionDto>>() {
            @Override
            public void onSubscribe(Disposable d) {

            }

            @Override
            public void onSuccess(PageableListDto<BookCollectionDto> bookCollectionPageableListDto) {
                mNextPage = bookCollectionPageableListDto.getNextPage();

                List<BookCollectionDto> bookCollectionList = bookCollectionPageableListDto.getElements();
                mBookCollectionListObservable.setValue(bookCollectionList);

                mIsLoadingObservable.setValue(false);
            }

            @Override
            public void onError(Throwable e) {
                mMessageObservable.setValue(toMessage(e));
                mShowMessageObservable.setValue(true);

                mIsLoadingObservable.setValue(false);
            }
        });
    }

    @Override
    public Boolean hasNextBookCollectionList() {
        if(mNextPage != null) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void loadNextBookCollectionList() {
        if(hasNextBookCollectionList()) {
            mIsLoadingObservable.setValue(true);

            Single<PageableListDto<BookCollectionDto>> single = mApplicationService.getLatestBookCollections(mBookCollectionNameObservable.getValue(), mNextPage, mNextPageSize, "()");
            single = single.observeOn(AndroidSchedulers.mainThread());
            single = single.subscribeOn(Schedulers.io());
            single.subscribe(new SingleObserver<PageableListDto<BookCollectionDto>>() {
                @Override
                public void onSubscribe(Disposable d) {

                }

                @Override
                public void onSuccess(PageableListDto<BookCollectionDto> bookCollectionPageableListDto) {
                    mNextPage = bookCollectionPageableListDto.getNextPage();

                    List<BookCollectionDto> bookCollectionList = mBookCollectionListObservable.getValue();
                    bookCollectionList.addAll(bookCollectionPageableListDto.getElements());
                    mBookCollectionListObservable.setValue(bookCollectionList);

                    mIsLoadingObservable.setValue(false);
                }

                @Override
                public void onError(Throwable e) {
                    mMessageObservable.setValue(toMessage(e));
                    mShowMessageObservable.setValue(true);

                    mIsLoadingObservable.setValue(false);
                }
            });
        }
    }

    @Override
    public void addBookMark() {
        BookCollectionDto selectedBookCollectionDto = mSelectedBookCollectionObservable.getValue();

        Completable completable = mApplicationService.createOrUpdateBookMarks(selectedBookCollectionDto.getId());
        completable = completable.observeOn(AndroidSchedulers.mainThread());
        completable = completable.subscribeOn(Schedulers.io());
        completable.subscribe(new CompletableObserver() {
            @Override
            public void onSubscribe(Disposable d) {

            }

            @Override
            public void onComplete() {
                // do nothing
            }

            @Override
            public void onError(Throwable e) {
                mMessageObservable.setValue(toMessage(e));
                mShowMessageObservable.setValue(true);
            }
        });
    }

    @Override
    public void removeBookMark() {
        BookCollectionDto selectedBookCollectionDto = mSelectedBookCollectionObservable.getValue();

        Completable completable = mApplicationService.deleteBookMarks(selectedBookCollectionDto.getId());
        completable = completable.observeOn(AndroidSchedulers.mainThread());
        completable = completable.subscribeOn(Schedulers.io());
        completable.subscribe(new CompletableObserver() {
            @Override
            public void onSubscribe(Disposable d) {

            }

            @Override
            public void onComplete() {
                // do nothing
            }

            @Override
            public void onError(Throwable e) {
                mMessageObservable.setValue(toMessage(e));
                mShowMessageObservable.setValue(true);
            }
        });
    }

    @Override
    public Uri getBookCollectionPageUri(BookCollectionDto bookCollectionDto, String scaleType, int scaleWidth, int scaleHeight) {
        return mRequestHandler.getBookCollectionPageUri(bookCollectionDto, scaleType, scaleWidth, scaleHeight);
    }

    @Override
    public Picasso getPicasso() {
        return mPicasso;
    }
}
