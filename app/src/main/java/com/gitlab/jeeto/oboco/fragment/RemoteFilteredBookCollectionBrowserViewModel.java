package com.gitlab.jeeto.oboco.fragment;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;

import com.gitlab.jeeto.oboco.R;
import com.gitlab.jeeto.oboco.client.ApplicationService;
import com.gitlab.jeeto.oboco.client.AuthenticationManager;
import com.gitlab.jeeto.oboco.client.BookCollectionDto;
import com.gitlab.jeeto.oboco.client.BookCollectionMarkDto;
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

public class RemoteFilteredBookCollectionBrowserViewModel extends BookCollectionBrowserViewModel {
    private BookCollectionBrowserViewModel.Mode mMode;
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

    public RemoteFilteredBookCollectionBrowserViewModel(Application application, Bundle arguments) {
        super(application, arguments);

        mMode = (BookCollectionBrowserViewModel.Mode) getArguments().getSerializable(BookCollectionBrowserViewModel.PARAM_MODE);
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

    private void setTitle() {
        String title = "";

        BookCollectionDto bookCollection = mBookCollectionObservable.getValue();

        if(bookCollection != null && bookCollection.getParentBookCollection() != null) {
            title = bookCollection.getName();
        } else {
            if (BookCollectionBrowserViewModel.Mode.MODE_REMOTE_ALL.equals(mMode)) {
                title = getApplication().getResources().getString(R.string.drawer_menu_book_collection_browser_all);
            } else if (BookCollectionBrowserViewModel.Mode.MODE_REMOTE_NEW.equals(mMode)) {
                title = getApplication().getResources().getString(R.string.drawer_menu_book_collection_browser_new);
            } else if (BookCollectionBrowserViewModel.Mode.MODE_REMOTE_TO_READ.equals(mMode)) {
                title = getApplication().getResources().getString(R.string.drawer_menu_book_collection_browser_to_read);
            } else if (BookCollectionBrowserViewModel.Mode.MODE_REMOTE_LATEST_READ.equals(mMode)) {
                title = getApplication().getResources().getString(R.string.drawer_menu_book_collection_browser_latest_read);
            } else if (BookCollectionBrowserViewModel.Mode.MODE_REMOTE_READ.equals(mMode)) {
                title = getApplication().getResources().getString(R.string.drawer_menu_book_collection_browser_read);
            } else if (BookCollectionBrowserViewModel.Mode.MODE_REMOTE_READING.equals(mMode)) {
                title = getApplication().getResources().getString(R.string.drawer_menu_book_collection_browser_reading);
            } else if (BookCollectionBrowserViewModel.Mode.MODE_REMOTE_UNREAD.equals(mMode)) {
                title = getApplication().getResources().getString(R.string.drawer_menu_book_collection_browser_unread);
            }
        }

        Integer bookCollectionListSize = mBookCollectionListSizeObservable.getValue();

        if(bookCollectionListSize != null) {
            title = title + " (" + bookCollectionListSize + ")";
        }

        mTitleObservable.setValue(title);
    }

    @Override
    public void load() {
        setTitle();

        mSearchTypeObservable.setValue("NAME");
        mSearchObservable.setValue("");

        BookCollectionDto bookCollection = new BookCollectionDto();
        bookCollection.setName("");
        mBookCollectionObservable.setValue(bookCollection);

        loadBookCollectionList();
    }

    private String getFilterType() {
        String filterType;
        if(Mode.MODE_REMOTE_ALL.equals(mMode)) {
            filterType = "ALL";
        } else if(Mode.MODE_REMOTE_NEW.equals(mMode)) {
            filterType = "NEW";
        } else if(Mode.MODE_REMOTE_TO_READ.equals(mMode)) {
            filterType = "TO_READ";
        } else if(Mode.MODE_REMOTE_LATEST_READ.equals(mMode)) {
            filterType = "LATEST_READ";
        } else if(Mode.MODE_REMOTE_READ.equals(mMode)) {
            filterType = "READ";
        } else if(Mode.MODE_REMOTE_READING.equals(mMode)) {
            filterType = "READING";
        } else if(Mode.MODE_REMOTE_UNREAD.equals(mMode)) {
            filterType = "UNREAD";
        } else {
            filterType = "ALL";
        }
        return filterType;
    }

    @Override
    public void loadBookCollectionList() {
        mIsLoadingObservable.setValue(true);

        String filterType = getFilterType();

        Single<PageableListDto<BookCollectionDto>> single =  mApplicationService.getBookCollections(mSearchTypeObservable.getValue(), mSearchObservable.getValue(), filterType, mPage, mPageSize, "(bookCollectionMark)");
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

                Integer bookCollectionListSize = bookCollectionPageableListDto.getNumberOfElements().intValue();
                mBookCollectionListSizeObservable.setValue(bookCollectionListSize);

                setTitle();

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

            String filterType = getFilterType();

            Single<PageableListDto<BookCollectionDto>> single = mApplicationService.getBookCollections(mSearchTypeObservable.getValue(), mSearchObservable.getValue(), filterType, mNextPage, mNextPageSize, "(bookCollectionMark)");
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

                    Integer bookCollectionListSize = bookCollectionPageableListDto.getNumberOfElements().intValue();
                    mBookCollectionListSizeObservable.setValue(bookCollectionListSize);

                    setTitle();

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
    public void addBookMark(BookCollectionMarkDto bookCollectionMarkDto) {
        BookCollectionDto selectedBookCollectionDto = mSelectedBookCollectionObservable.getValue();

        Single<BookCollectionMarkDto> single = mApplicationService.createOrUpdateBookMarksByBookCollection(selectedBookCollectionDto.getId(), bookCollectionMarkDto);
        single = single.observeOn(AndroidSchedulers.mainThread());
        single = single.subscribeOn(Schedulers.io());
        single.subscribe(new SingleObserver<BookCollectionMarkDto>() {
            @Override
            public void onSubscribe(Disposable d) {

            }

            @Override
            public void onSuccess(BookCollectionMarkDto bookCollectionMarkDto) {
                selectedBookCollectionDto.setBookCollectionMark(bookCollectionMarkDto);

                List<BookCollectionDto> updatedBookCollectionListDto = new ArrayList<BookCollectionDto>();
                updatedBookCollectionListDto.add(selectedBookCollectionDto);

                updateBookCollectionList(updatedBookCollectionListDto);
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

        Completable completable = mApplicationService.deleteBookMarksByBookCollection(selectedBookCollectionDto.getId());
        completable = completable.observeOn(AndroidSchedulers.mainThread());
        completable = completable.subscribeOn(Schedulers.io());
        completable.subscribe(new CompletableObserver() {
            @Override
            public void onSubscribe(Disposable d) {

            }

            @Override
            public void onComplete() {
                selectedBookCollectionDto.setBookCollectionMark(null);

                List<BookCollectionDto> updatedBookCollectionListDto = new ArrayList<BookCollectionDto>();
                updatedBookCollectionListDto.add(selectedBookCollectionDto);

                updateBookCollectionList(updatedBookCollectionListDto);
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
