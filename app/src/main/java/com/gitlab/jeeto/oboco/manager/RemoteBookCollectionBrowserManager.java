package com.gitlab.jeeto.oboco.manager;

import android.content.Context;
import android.content.SharedPreferences;

import com.gitlab.jeeto.oboco.api.ApplicationService;
import com.gitlab.jeeto.oboco.api.AuthenticationManager;
import com.gitlab.jeeto.oboco.api.BookCollectionDto;
import com.gitlab.jeeto.oboco.api.PageableListDto;
import com.gitlab.jeeto.oboco.fragment.LibraryFragment;

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

public class RemoteBookCollectionBrowserManager extends BookCollectionBrowserManager {
    private LibraryFragment mLibraryFragment;
    private Long mBookCollectionId;

    private String mBaseUrl;

    private AuthenticationManager mAuthenticationManager;
    private Disposable mAuthenticationManagerDisposable;
    private ApplicationService mApplicationService;

    public RemoteBookCollectionBrowserManager(Long bookCollectionId) {
        super();
        mBookCollectionId = bookCollectionId;
    }

    public void create(LibraryFragment libraryFragment) {
        mLibraryFragment = libraryFragment;

        SharedPreferences sp = mLibraryFragment.getContext().getSharedPreferences("application", Context.MODE_PRIVATE);
        mBaseUrl = sp.getString("baseUrl", "");

        mAuthenticationManager = new AuthenticationManager(mLibraryFragment.getContext());
        Observable<Throwable> observable = mAuthenticationManager.getErrors();
        observable = observable.observeOn(AndroidSchedulers.mainThread());
        observable = observable.subscribeOn(Schedulers.io());
        mAuthenticationManagerDisposable = observable.subscribe(new Consumer<Throwable>() {
            @Override
            public void accept(Throwable e) throws Exception {
                mLibraryFragment.onError(e);
            }
        });

        mApplicationService = new ApplicationService(mLibraryFragment.getContext(), mBaseUrl, mAuthenticationManager);

    }

    public void destroy() {
        mAuthenticationManagerDisposable.dispose();
    }

    public void load(String bookCollectionName, int page, int pageSize) {
        mLibraryFragment.setRefreshing(true);

        Single<BookCollectionDto> single = new Single<BookCollectionDto>() {
            @Override
            protected void subscribeActual(SingleObserver<? super BookCollectionDto> observer) {
                try {
                    if(mBookCollectionId == null) {
                        BookCollectionDto bookCollection = mApplicationService.getRootBookCollection("(parentBookCollection)").blockingGet();

                        observer.onSuccess(bookCollection);
                    } else {
                        BookCollectionDto bookCollection = mApplicationService.getBookCollection(mBookCollectionId, "(parentBookCollection)").blockingGet();

                        observer.onSuccess(bookCollection);
                    }
                } catch(Exception e) {
                    observer.onError(e);
                }
            }
        };
        single = single.observeOn(AndroidSchedulers.mainThread());
        single = single.subscribeOn(Schedulers.io());
        single.subscribe(new SingleObserver<BookCollectionDto>() {
            @Override
            public void onSubscribe(Disposable d) {

            }

            @Override
            public void onSuccess(BookCollectionDto bookCollection) {
                if(bookCollection != null) {
                    Single<PageableListDto<BookCollectionDto>> single = new Single<PageableListDto<BookCollectionDto>>() {
                        @Override
                        protected void subscribeActual(SingleObserver<? super PageableListDto<BookCollectionDto>> observer) {
                            try {
                                PageableListDto<BookCollectionDto> bookCollectionPageableList = mApplicationService.getBookCollections(bookCollection.getId(), bookCollectionName, page, pageSize, "(bookCollections,books)").blockingGet();

                                observer.onSuccess(bookCollectionPageableList);
                            } catch(Exception e) {
                                observer.onError(e);
                            }
                        }
                    };
                    single = single.observeOn(AndroidSchedulers.mainThread());
                    single = single.subscribeOn(Schedulers.io());
                    single.subscribe(new SingleObserver<PageableListDto<BookCollectionDto>>() {
                        @Override
                        public void onSubscribe(Disposable d) {

                        }

                        @Override
                        public void onSuccess(PageableListDto<BookCollectionDto> bookCollectionPageableList) {
                            mLibraryFragment.onLoad(bookCollection, bookCollectionPageableList);
                            mLibraryFragment.setRefreshing(false);
                        }

                        @Override
                        public void onError(Throwable e) {
                            mLibraryFragment.onError(e);
                            mLibraryFragment.setRefreshing(false);
                        }
                    });
                }
            }

            @Override
            public void onError(Throwable e) {
                mLibraryFragment.onError(e);
                mLibraryFragment.setRefreshing(false);
            }
        });
    }

    public void loadBookCollectionPageableList(String bookCollectionName, int page, int pageSize) {
        mLibraryFragment.setRefreshing(true);

        Single<PageableListDto<BookCollectionDto>> single = new Single<PageableListDto<BookCollectionDto>>() {
            @Override
            protected void subscribeActual(SingleObserver<? super PageableListDto<BookCollectionDto>> observer) {
                try {
                    PageableListDto<BookCollectionDto> bookCollectionPageableList = mApplicationService.getBookCollections(mBookCollectionId, bookCollectionName, page, pageSize, "(bookCollections,books)").blockingGet();

                    observer.onSuccess(bookCollectionPageableList);
                } catch(Exception e) {
                    observer.onError(e);
                }
            }
        };
        single = single.observeOn(AndroidSchedulers.mainThread());
        single = single.subscribeOn(Schedulers.io());
        single.subscribe(new SingleObserver<PageableListDto<BookCollectionDto>>() {
            @Override
            public void onSubscribe(Disposable d) {

            }

            @Override
            public void onSuccess(PageableListDto<BookCollectionDto> bookCollectionPageableList) {
                mLibraryFragment.onLoadBookCollectionPageableList(bookCollectionPageableList);
                mLibraryFragment.setRefreshing(false);
            }

            @Override
            public void onError(Throwable e) {
                mLibraryFragment.onError(e);
                mLibraryFragment.setRefreshing(false);
            }
        });
    }

    public void addBookMark(BookCollectionDto bookCollection) {
        Completable completable = new Completable() {
            @Override
            protected void subscribeActual(CompletableObserver observer) {
                try {
                    mApplicationService.createOrUpdateBookMarks(bookCollection.getId()).blockingGet();

                    observer.onComplete();
                } catch(Exception e) {
                    observer.onError(e);
                }
            }
        };
        completable = completable.observeOn(AndroidSchedulers.mainThread());
        completable = completable.subscribeOn(Schedulers.io());
        completable.subscribe(new CompletableObserver() {
            @Override
            public void onSubscribe(Disposable d) {

            }

            @Override
            public void onComplete() {
                mLibraryFragment.onAddBookMark(bookCollection);
            }

            @Override
            public void onError(Throwable e) {
                mLibraryFragment.onError(e);
            }
        });
    }

    public void removeBookMark(BookCollectionDto bookCollection) {
        Completable completable = new Completable() {
            @Override
            protected void subscribeActual(CompletableObserver observer) {
                try {
                    mApplicationService.deleteBookMarks(bookCollection.getId()).blockingGet();

                    observer.onComplete();
                } catch(Exception e) {
                    observer.onError(e);
                }
            }
        };
        completable = completable.observeOn(AndroidSchedulers.mainThread());
        completable = completable.subscribeOn(Schedulers.io());
        completable.subscribe(new CompletableObserver() {
            @Override
            public void onSubscribe(Disposable d) {

            }

            @Override
            public void onComplete() {
                mLibraryFragment.onRemoveBookMark(bookCollection);
            }

            @Override
            public void onError(Throwable e) {
                mLibraryFragment.onError(e);
            }
        });
    }

    public InputStream getBookCollectionPage(BookCollectionDto bookCollection, String scaleType, int scaleWidth, int scaleHeight) throws IOException {
        ResponseBody responseBody = mApplicationService.downloadBookCollectionPage(bookCollection.getId(), scaleType, scaleWidth, scaleHeight).blockingGet();

        InputStream inputStream = responseBody.byteStream();

        return inputStream;
    }
}
