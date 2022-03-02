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
import com.gitlab.jeeto.oboco.client.ProblemDto;
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

public class RemoteBookCollectionBrowserViewModel extends BookCollectionBrowserViewModel {
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

    public RemoteBookCollectionBrowserViewModel(Application application, Bundle arguments) {
        super(application, arguments);

        SharedPreferences sp = getApplication().getSharedPreferences("application", Context.MODE_PRIVATE);
        mBaseUrl = sp.getString("baseUrl", "");

        mAuthenticationManager = new AuthenticationManager(getApplication().getApplicationContext());
        Observable<Throwable> observable = mAuthenticationManager.getErrors();
        observable = observable.observeOn(AndroidSchedulers.mainThread());
        observable = observable.subscribeOn(Schedulers.io());
        mAuthenticationManagerDisposable = observable.subscribe(new Consumer<Throwable>() {
            @Override
            public void accept(Throwable e) throws Exception {
                String message = null;

                ProblemDto p = getProblem(e);
                if(p != null) {
                    if(400 == p.getStatusCode()) {
                        if("PROBLEM_USER_TOKEN_INVALID".equals(p.getCode())) {
                            message = getMessage(R.string.action_user_log_in_token_error);
                        }
                    }
                }

                if(message == null) {
                    message = getMessage(e);
                }

                mMessageObservable.setValue(message);
                mShowMessageObservable.setValue(true);
            }
        });

        mApplicationService = new ApplicationService(getApplication().getApplicationContext(), mBaseUrl, mAuthenticationManager);

        mRequestHandler = new RemoteBookCollectionBrowserRequestHandler(mApplicationService);

        mPicasso = new Picasso.Builder(getApplication())
                .addRequestHandler(mRequestHandler)
                .listener(new Picasso.Listener() {
                    @Override
                    public void onImageLoadFailed(Picasso picasso, Uri uri, Exception e) {
                        String message = null;

                        ProblemDto p = getProblem(e);
                        if(p != null) {
                            if(404 == p.getStatusCode()) {
                                if("PROBLEM_BOOK_NOT_FOUND".equals(p.getCode()) || "PROBLEM_BOOK_PAGE_NOT_FOUND".equals(p.getCode())) {
                                    message = getMessage(R.string.action_book_page_get_error);
                                }
                            }
                        }

                        if(message == null) {
                            message = getMessage(e);
                        }

                        mMessageObservable.setValue(message);
                        mShowMessageObservable.setValue(true);
                    }
                })
                //.loggingEnabled(true)
                //.indicatorsEnabled(true)
                .build();

        mBookCollectionIdObservable.setValue(getArguments().getLong(BookCollectionBrowserViewModel.PARAM_BOOK_COLLECTION_ID));

        mFilterTypeObservable.setValue(getArguments().getString(BookCollectionBrowserViewModel.PARAM_FILTER_TYPE));

        mSearchTypeObservable.setValue("");

        mSearchObservable.setValue("");

        mSearchDialogSearchTypeObservable.setValue(mSearchTypeObservable.getValue());

        mSearchDialogSearchObservable.setValue(mSearchObservable.getValue());

        load();
    }

    @Override
    protected void onCleared() {
        mPicasso.shutdown();

        mAuthenticationManagerDisposable.dispose();
    }

    private void setTitle() {
        String title = getApplication().getResources().getString(R.string.drawer_menu_book_collection_browser);

        BookCollectionDto bookCollection = mBookCollectionObservable.getValue();

        if(bookCollection != null) {
            Integer bookCollectionListSize = mBookCollectionListSizeObservable.getValue();

            if(bookCollectionListSize != null) {
                String filterType = mFilterTypeObservable.getValue();

                if("ROOT".equals(filterType)) {
                    title = title + ": " + getApplication().getResources().getString(R.string.book_collection_browser_menu_filter_type_root);
                } else if("ALL".equals(filterType)) {
                    title = title + ": " + getApplication().getResources().getString(R.string.book_collection_browser_menu_filter_type_all);
                } else if("NEW".equals(filterType)) {
                    title = title + ": " + getApplication().getResources().getString(R.string.book_collection_browser_menu_filter_type_new);
                } else if("TO_READ".equals(filterType)) {
                    title = title + ": " + getApplication().getResources().getString(R.string.book_collection_browser_menu_filter_type_to_read);
                } else if("LATEST_READ".equals(filterType)) {
                    title = title + ": " + getApplication().getResources().getString(R.string.book_collection_browser_menu_filter_type_latest_read);
                } else if("READ".equals(filterType)) {
                    title = title + ": " + getApplication().getResources().getString(R.string.book_collection_browser_menu_filter_type_read);
                } else if("READING".equals(filterType)) {
                    title = title + ": " + getApplication().getResources().getString(R.string.book_collection_browser_menu_filter_type_reading);
                } else if("UNREAD".equals(filterType)) {
                    title = title + ": " + getApplication().getResources().getString(R.string.book_collection_browser_menu_filter_type_unread);
                }

                String searchType = mSearchTypeObservable.getValue();

                if("".equals(searchType)) {
                    title = title + " (" + bookCollectionListSize + ")";
                } else {
                    title = title + " (" + bookCollectionListSize + "*)";
                }

                if("ROOT".equals(filterType)) {
                    if(bookCollection.getParentBookCollection() != null) {
                        title = title + " - " + bookCollection.getName();
                    }
                }
            }
        }

        mTitleObservable.setValue(title);
    }

    @Override
    public void load() {
        setTitle();

        mIsLoadingObservable.setValue(true);

        if(mFilterTypeObservable.getValue().equals("ROOT")) {
            Single<BookCollectionDto> single;
            if (mBookCollectionIdObservable.getValue() == -1L) {
                single = mApplicationService.getRootBookCollection("(parentBookCollection)");
            } else {
                single = mApplicationService.getBookCollection(mBookCollectionIdObservable.getValue(), "(parentBookCollection)");
            }
            single = single.observeOn(AndroidSchedulers.mainThread());
            single = single.subscribeOn(Schedulers.io());
            single.subscribe(new SingleObserver<BookCollectionDto>() {
                @Override
                public void onSubscribe(Disposable d) {

                }

                @Override
                public void onSuccess(BookCollectionDto bookCollectionDto) {
                    mBookCollectionIdObservable.setValue(bookCollectionDto.getId());
                    mBookCollectionObservable.setValue(bookCollectionDto);

                    setTitle();

                    loadBookCollectionList();
                }

                @Override
                public void onError(Throwable e) {
                    String message = null;

                    ProblemDto p = getProblem(e);
                    if(p != null) {
                        if(400 == p.getStatusCode()) {
                            if("PROBLEM_GRAPH_INVALID".equals(p.getCode())) {
                                message = getMessage(R.string.action_book_collection_get_error);
                            }
                        } else if(400 == p.getStatusCode()) {
                            if("PROBLEM_BOOK_COLLECTION_NOT_FOUND".equals(p.getCode())) {
                                message = getMessage(R.string.action_book_collection_get_error);
                            }
                        }
                    }

                    if(message == null) {
                        message = getMessage(e);
                    }

                    mMessageObservable.setValue(message);
                    mShowMessageObservable.setValue(true);

                    mIsLoadingObservable.setValue(false);
                }
            });
        } else {
            BookCollectionDto bookCollection = new BookCollectionDto();
            bookCollection.setName("");
            mBookCollectionObservable.setValue(bookCollection);

            setTitle();

            loadBookCollectionList();
        }
    }

    @Override
    public void loadBookCollectionList() {
        mPage = 1;
        mPageSize = 100;
        mNextPage = null;
        mNextPageSize = 100;

        mIsLoadingObservable.setValue(true);

        Single<PageableListDto<BookCollectionDto>> single;
        if(mFilterTypeObservable.getValue().equals("ROOT")) {
            if(mSearchTypeObservable.getValue().equals("")) {
                single = mApplicationService.getBookCollectionsByBookCollection(mBookCollectionIdObservable.getValue(), mPage, mPageSize, "(bookCollectionMark)");
            } else {
                single = mApplicationService.getBookCollectionsByBookCollection(mBookCollectionIdObservable.getValue(), mSearchTypeObservable.getValue(), mSearchObservable.getValue(), mPage, mPageSize, "(bookCollectionMark)");
            }
        } else {
            if(mSearchTypeObservable.getValue().equals("")) {
                single = mApplicationService.getBookCollections(mFilterTypeObservable.getValue(), mPage, mPageSize, "(bookCollectionMark)");
            } else {
                single = mApplicationService.getBookCollections(mSearchTypeObservable.getValue(), mSearchObservable.getValue(), mFilterTypeObservable.getValue(), mPage, mPageSize, "(bookCollectionMark)");
            }
        }
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
                String message = null;

                ProblemDto p = getProblem(e);
                if(p != null) {
                    if(400 == p.getStatusCode()) {
                        if("PROBLEM_PAGE_INVALID".equals(p.getCode()) || "PROBLEM_PAGE_SIZE_INVALID".equals(p.getCode()) || "PROBLEM_GRAPH_INVALID".equals(p.getCode())) {
                            message = getMessage(R.string.action_book_collections_get_error);
                        }
                    }
                }

                if(message == null) {
                    message = getMessage(e);
                }

                mMessageObservable.setValue(message);
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

            Single<PageableListDto<BookCollectionDto>> single;
            if(mFilterTypeObservable.getValue().equals("ROOT")) {
                if(mSearchTypeObservable.getValue().equals("")) {
                    single = mApplicationService.getBookCollectionsByBookCollection(mBookCollectionIdObservable.getValue(), mNextPage, mNextPageSize, "(bookCollectionMark)");
                } else {
                    single = mApplicationService.getBookCollectionsByBookCollection(mBookCollectionIdObservable.getValue(), mSearchTypeObservable.getValue(), mSearchObservable.getValue(), mNextPage, mNextPageSize, "(bookCollectionMark)");
                }
            } else {
                if(mSearchObservable.getValue().equals("")) {
                    single = mApplicationService.getBookCollections(mFilterTypeObservable.getValue(), mNextPage, mNextPageSize, "(bookCollectionMark)");
                } else {
                    single = mApplicationService.getBookCollections(mSearchTypeObservable.getValue(), mSearchObservable.getValue(), mFilterTypeObservable.getValue(), mNextPage, mNextPageSize, "(bookCollectionMark)");
                }
            }
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
                    String message = null;

                    ProblemDto p = getProblem(e);
                    if(p != null) {
                        if(400 == p.getStatusCode()) {
                            if("PROBLEM_PAGE_INVALID".equals(p.getCode()) || "PROBLEM_PAGE_SIZE_INVALID".equals(p.getCode()) || "PROBLEM_GRAPH_INVALID".equals(p.getCode())) {
                                message = getMessage(R.string.action_book_collections_get_error);
                            }
                        }
                    }

                    if(message == null) {
                        message = getMessage(e);
                    }

                    mMessageObservable.setValue(message);
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
                String message = null;

                ProblemDto p = getProblem(e);
                if(p != null) {
                    if(400 == p.getStatusCode()) {
                        if("PROBLEM_BOOK_COLLECTION_MARK_BOOK_PAGE_INVALID".equals(p.getCode())) {
                            message = getMessage(R.string.action_book_marks_create_update_error);
                        }
                    } else if(404 == p.getStatusCode()) {
                        if("PROBLEM_BOOK_COLLECTION_NOT_FOUND".equals(p.getCode()) || "PROBLEM_BOOK_COLLECTION_BOOKS_NOT_FOUND".equals(p.getCode())) {
                            message = getMessage(R.string.action_book_marks_create_update_error);
                        }
                    }
                }

                if(message == null) {
                    message = getMessage(e);
                }

                mMessageObservable.setValue(message);
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
                String message = null;

                ProblemDto p = getProblem(e);
                if(p != null) {
                    if(404 == p.getStatusCode()) {
                        if("PROBLEM_BOOK_COLLECTION_NOT_FOUND".equals(p.getCode()) || "PROBLEM_BOOK_COLLECTION_BOOKS_NOT_FOUND".equals(p.getCode())) {
                            message = getMessage(R.string.action_book_marks_delete_error);
                        }
                    }
                }

                if(message == null) {
                    message = getMessage(e);
                }

                mMessageObservable.setValue(message);
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
