package com.gitlab.jeeto.oboco.manager;

import android.os.Bundle;
import android.os.Environment;

import androidx.room.Room;

import com.gitlab.jeeto.oboco.client.BookCollectionDto;
import com.gitlab.jeeto.oboco.client.BookDto;
import com.gitlab.jeeto.oboco.client.BookMarkDto;
import com.gitlab.jeeto.oboco.common.NaturalOrderComparator;
import com.gitlab.jeeto.oboco.database.AppDatabase;
import com.gitlab.jeeto.oboco.database.Book;
import com.gitlab.jeeto.oboco.fragment.DownloadBrowserFragment;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.reactivex.Completable;
import io.reactivex.CompletableObserver;
import io.reactivex.Single;
import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class DownloadBrowserManager extends BrowserManager {
    public final static String PARAM_BOOK_COLLECTION_PATH = "PARAM_BOOK_COLLECTION_PATH";
    public final static String STATE_CURRENT_BOOK_COLLECTION_PATH = "STATE_CURRENT_BOOK_COLLECTION_PATH";
    private DownloadBrowserFragment mDownloadBrowserFragment;

    private File mRootBookCollectionFile;
    private File mCurrentBookCollectionFile;

    private AppDatabase mAppDatabase;

    public DownloadBrowserManager(DownloadBrowserFragment fragment) {
        super();
        mDownloadBrowserFragment = fragment;
    }

    @Override
    public void create(Bundle savedInstanceState) {
        mRootBookCollectionFile = mDownloadBrowserFragment.getContext().getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);

        if (savedInstanceState != null) {
            String bookCollectionPath = savedInstanceState.getString(STATE_CURRENT_BOOK_COLLECTION_PATH);

            mCurrentBookCollectionFile = new File(bookCollectionPath);
        }
        else {
            if(mDownloadBrowserFragment.getArguments() != null) {
                String bookCollectionPath = mDownloadBrowserFragment.getArguments().getString(PARAM_BOOK_COLLECTION_PATH);

                mCurrentBookCollectionFile = new File(bookCollectionPath);
            } else {
                mCurrentBookCollectionFile = mDownloadBrowserFragment.getContext().getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);
            }
        }

        mAppDatabase = Room.databaseBuilder(mDownloadBrowserFragment.getActivity().getApplicationContext(), AppDatabase.class, "database").build();
    }

    @Override
    public void destroy() {
        if(mAppDatabase != null) {
            if(mAppDatabase.isOpen()) {
                mAppDatabase.close();
            }
            mAppDatabase = null;
        }
    }

    @Override
    public void saveInstanceState(Bundle outState) {
        outState.putString(STATE_CURRENT_BOOK_COLLECTION_PATH, mCurrentBookCollectionFile.getAbsolutePath());
    }

    public void load(BookCollectionDto bookCollectionDto) {
        mCurrentBookCollectionFile = new File(bookCollectionDto.getPath());

        load();
    }

    @Override
    public void load() {
        BookCollectionDto currentBookCollectionDto = new BookCollectionDto();
        currentBookCollectionDto.setName(mCurrentBookCollectionFile.getName());
        currentBookCollectionDto.setPath(mCurrentBookCollectionFile.getAbsolutePath());

        if(!mCurrentBookCollectionFile.getAbsolutePath().equals(mRootBookCollectionFile.getAbsolutePath())) {
            File parentFile = mCurrentBookCollectionFile.getParentFile();

            BookCollectionDto parentBookCollectionDto = new BookCollectionDto();
            parentBookCollectionDto.setName(parentFile.getName());
            parentBookCollectionDto.setPath(parentFile.getAbsolutePath());

            currentBookCollectionDto.setParentBookCollection(parentBookCollectionDto);
        }

        List<BookCollectionDto> bookCollectionListDto = new ArrayList<BookCollectionDto>();
        List<BookDto> bookListDto = new ArrayList<BookDto>();

        File[] files = mCurrentBookCollectionFile.listFiles();
        if(files != null) {
            int index = 0;

            while(index < files.length) {
                File file = files[index];

                if (file.isDirectory()) {
                    BookCollectionDto bookCollectionDto = new BookCollectionDto();
                    bookCollectionDto.setName(file.getName());
                    bookCollectionDto.setPath(file.getAbsolutePath());

                    bookCollectionListDto.add(bookCollectionDto);
                } else if(file.isFile()) {
                    BookDto bookDto = new BookDto();
                    bookDto.setName(file.getName());
                    bookDto.setPath(file.getAbsolutePath());

                    bookListDto.add(bookDto);
                }

                index = index + 1;
            }
        }

        Collections.sort(bookCollectionListDto, new NaturalOrderComparator<BookCollectionDto>() {
            @Override
            public String toString(BookCollectionDto o) {
                return o.getName();
            }
        });

        Collections.sort(bookListDto, new NaturalOrderComparator<BookDto>() {
            @Override
            public String toString(BookDto o) {
                return o.getName();
            }
        });

        currentBookCollectionDto.setBookCollections(bookCollectionListDto);
        currentBookCollectionDto.setBooks(bookListDto);

        Single<List<Book>> single = mAppDatabase.bookDao().findByBookCollectionPath(mCurrentBookCollectionFile.getAbsolutePath());
        single = single.observeOn(AndroidSchedulers.mainThread());
        single = single.subscribeOn(Schedulers.io());
        single.subscribe(new SingleObserver<List<Book>>() {
            @Override
            public void onSubscribe(Disposable d) {

            }

            @Override
            public void onSuccess(List<Book> bookList) {
                for(BookDto bookDto: currentBookCollectionDto.getBooks()) {
                    for(Book book: bookList) {
                        if(bookDto.getPath().equals(book.path)) {
                            bookDto.setNumberOfPages(book.numberOfPages);

                            BookMarkDto bookMarkDto = new BookMarkDto();
                            bookMarkDto.setPage(book.page);

                            bookDto.setBookMark(bookMarkDto);

                            break;
                        }
                    }
                }

                mDownloadBrowserFragment.onLoad(currentBookCollectionDto);
            }

            @Override
            public void onError(Throwable e) {
                mDownloadBrowserFragment.onError(e);
            }
        });
    }

    public void deleteBook(BookDto bookDto) {
        File bookFile = new File(bookDto.getPath());
        bookFile.delete();

        Single<List<Book>> single = mAppDatabase.bookDao().findByPath(bookDto.getPath());
        single = single.observeOn(AndroidSchedulers.mainThread());
        single = single.subscribeOn(Schedulers.io());
        single.subscribe(new SingleObserver<List<Book>>() {
            @Override
            public void onSubscribe(Disposable disposable) {

            }

            @Override
            public void onSuccess(List<Book> bookList) {
                Completable complatable = mAppDatabase.bookDao().delete(bookList);
                complatable = complatable.observeOn(AndroidSchedulers.mainThread());
                complatable = complatable.subscribeOn(Schedulers.io());
                complatable.subscribe(new CompletableObserver() {
                    @Override
                    public void onSubscribe(Disposable disposable) {

                    }

                    @Override
                    public void onComplete() {
                        mDownloadBrowserFragment.onDeleteBook(bookDto);
                    }

                    @Override
                    public void onError(Throwable e) {
                        mDownloadBrowserFragment.onError(e);
                    }
                });
            }

            @Override
            public void onError(Throwable e) {
                mDownloadBrowserFragment.onError(e);
            }
        });
    }

    public void deleteBookCollection(BookCollectionDto bookCollectionDto) {
        File bookCollectionFile = new File(bookCollectionDto.getPath());
        for(File file: bookCollectionFile.listFiles()) {
            file.delete();
        }
        bookCollectionFile.delete();

        Single<List<Book>> single = mAppDatabase.bookDao().findByBookCollectionPath(bookCollectionDto.getPath());
        single = single.observeOn(AndroidSchedulers.mainThread());
        single = single.subscribeOn(Schedulers.io());
        single.subscribe(new SingleObserver<List<Book>>() {
            @Override
            public void onSubscribe(Disposable disposable) {

            }

            @Override
            public void onSuccess(List<Book> bookList) {
                Completable complatable = mAppDatabase.bookDao().delete(bookList);
                complatable = complatable.observeOn(AndroidSchedulers.mainThread());
                complatable = complatable.subscribeOn(Schedulers.io());
                complatable.subscribe(new CompletableObserver() {
                    @Override
                    public void onSubscribe(Disposable disposable) {

                    }

                    @Override
                    public void onComplete() {
                        mDownloadBrowserFragment.onDeleteBookCollection(bookCollectionDto);
                    }

                    @Override
                    public void onError(Throwable e) {
                        mDownloadBrowserFragment.onError(e);
                    }
                });
            }

            @Override
            public void onError(Throwable e) {
                mDownloadBrowserFragment.onError(e);
            }
        });
    }
}
