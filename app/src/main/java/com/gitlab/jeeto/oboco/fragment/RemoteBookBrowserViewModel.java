package com.gitlab.jeeto.oboco.fragment;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;

import com.gitlab.jeeto.oboco.client.ApplicationService;
import com.gitlab.jeeto.oboco.client.AuthenticationManager;
import com.gitlab.jeeto.oboco.client.BookCollectionDto;
import com.gitlab.jeeto.oboco.client.BookDto;
import com.gitlab.jeeto.oboco.client.BookMarkDto;
import com.gitlab.jeeto.oboco.client.PageableListDto;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
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

public class RemoteBookBrowserViewModel extends BookBrowserViewModel {
    private Long mBookCollectionId;
    private String mFilterType;
    private Integer mPage;
    private Integer mPageSize;
    private Integer mNextPage;
    private Integer mNextPageSize;

    private String mBaseUrl;

    private AuthenticationManager mAuthenticationManager;
    private Disposable mAuthenticationManagerDisposable;
    private ApplicationService mApplicationService;

    private BookBrowserRequestHandler mRequestHandler;
    private Picasso mPicasso;

    public RemoteBookBrowserViewModel(Application application, Bundle arguments) {
        super(application, arguments);

        mBookCollectionId = getArguments().getLong(BookBrowserViewModel.PARAM_BOOK_COLLECTION_ID);
        mFilterType = getArguments().getString(BookBrowserViewModel.PARAM_FILTER_TYPE);
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

        mRequestHandler = new RemoteBookBrowserRequestHandler(mApplicationService);

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
        mFilterTypeObservable.setValue(mFilterType);

        mIsLoadingObservable.setValue(true);

        Single<BookCollectionDto> single;
        if(mBookCollectionId == null) {
            single = mApplicationService.getRootBookCollection("(parentBookCollection)");
        } else {
            single = mApplicationService.getBookCollection(mBookCollectionId, "(parentBookCollection)");
        }
        single = single.observeOn(AndroidSchedulers.mainThread());
        single = single.subscribeOn(Schedulers.io());
        single.subscribe(new SingleObserver<BookCollectionDto>() {
            @Override
            public void onSubscribe(Disposable d) {

            }

            @Override
            public void onSuccess(BookCollectionDto bookCollectionDto) {
                mBookCollectionId = bookCollectionDto.getId();
                mBookCollectionObservable.setValue(bookCollectionDto);

                Single<PageableListDto<BookDto>> single = mApplicationService.getBooksByBookCollection(mBookCollectionId, mFilterTypeObservable.getValue(), mPage, mPageSize, "(bookMark)");
                single = single.observeOn(AndroidSchedulers.mainThread());
                single = single.subscribeOn(Schedulers.io());
                single.subscribe(new SingleObserver<PageableListDto<BookDto>>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onSuccess(PageableListDto<BookDto> bookPageableListDto) {
                        mNextPage = bookPageableListDto.getNextPage();

                        List<BookDto> bookList = bookPageableListDto.getElements();
                        mBookListObservable.setValue(bookList);

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
            public void onError(Throwable e) {
                mMessageObservable.setValue(toMessage(e));
                mShowMessageObservable.setValue(true);

                mIsLoadingObservable.setValue(false);
            }
        });
    }

    @Override
    public void loadBookList() {
        mIsLoadingObservable.setValue(true);

        Single<PageableListDto<BookDto>> single = mApplicationService.getBooksByBookCollection(mBookCollectionId, mFilterTypeObservable.getValue(), mPage, mPageSize, "(bookMark)");
        single = single.observeOn(AndroidSchedulers.mainThread());
        single = single.subscribeOn(Schedulers.io());
        single.subscribe(new SingleObserver<PageableListDto<BookDto>>() {
            @Override
            public void onSubscribe(Disposable d) {

            }

            @Override
            public void onSuccess(PageableListDto<BookDto> bookPageableListDto) {
                mNextPage = bookPageableListDto.getNextPage();

                List<BookDto> bookList = bookPageableListDto.getElements();
                mBookListObservable.setValue(bookList);

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
    public Boolean hasNextBookList() {
        if(mNextPage != null) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void loadNextBookList() {
        if(hasNextBookList()) {
            mIsLoadingObservable.setValue(true);

            Single<PageableListDto<BookDto>> single = mApplicationService.getBooksByBookCollection(mBookCollectionId, mFilterTypeObservable.getValue(), mNextPage, mNextPageSize, "(bookMark)");
            single = single.observeOn(AndroidSchedulers.mainThread());
            single = single.subscribeOn(Schedulers.io());
            single.subscribe(new SingleObserver<PageableListDto<BookDto>>() {
                @Override
                public void onSubscribe(Disposable d) {

                }

                @Override
                public void onSuccess(PageableListDto<BookDto> bookPageableListDto) {
                    mNextPage = bookPageableListDto.getNextPage();

                    List<BookDto> bookList = mBookListObservable.getValue();
                    bookList.addAll(bookPageableListDto.getElements());
                    mBookListObservable.setValue(bookList);

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
        BookDto selectedBookDto = mSelectedBookObservable.getValue();

        BookMarkDto bookMarkDto = new BookMarkDto();
        bookMarkDto.setPage(selectedBookDto.getNumberOfPages());

        Single<BookMarkDto> single = mApplicationService.createOrUpdateBookMarkByBook(selectedBookDto.getId(), bookMarkDto);
        single = single.observeOn(AndroidSchedulers.mainThread());
        single = single.subscribeOn(Schedulers.io());
        single.subscribe(new SingleObserver<BookMarkDto>() {
            @Override
            public void onSubscribe(Disposable d) {

            }

            @Override
            public void onSuccess(BookMarkDto bookMarkDto) {
                selectedBookDto.setBookMark(bookMarkDto);

                List<BookDto> updatedBookListDto = new ArrayList<BookDto>();
                updatedBookListDto.add(selectedBookDto);

                updateBookList(updatedBookListDto);
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
        BookDto selectedBookDto = mSelectedBookObservable.getValue();

        Completable completable = mApplicationService.deleteBookMarkByBook(selectedBookDto.getId());
        completable = completable.observeOn(AndroidSchedulers.mainThread());
        completable = completable.subscribeOn(Schedulers.io());
        completable.subscribe(new CompletableObserver() {
            @Override
            public void onSubscribe(Disposable d) {

            }

            @Override
            public void onComplete() {
                selectedBookDto.setBookMark(null);

                List<BookDto> updatedBookListDto = new ArrayList<BookDto>();
                updatedBookListDto.add(selectedBookDto);

                updateBookList(updatedBookListDto);
            }

            @Override
            public void onError(Throwable e) {
                mMessageObservable.setValue(toMessage(e));
                mShowMessageObservable.setValue(true);
            }
        });
    }

    @Override
    public Uri getBookPageUri(BookDto bookDto, String scaleType, int scaleWidth, int scaleHeight) {
        return mRequestHandler.getBookPageUri(bookDto, scaleType, scaleWidth, scaleHeight);
    }

    @Override
    public Picasso getPicasso() {
        return mPicasso;
    }
}
