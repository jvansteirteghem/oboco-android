package com.gitlab.jeeto.oboco.manager;

import android.net.Uri;
import android.os.Bundle;

import androidx.room.Room;

import com.gitlab.jeeto.oboco.api.BookDto;
import com.gitlab.jeeto.oboco.api.BookMarkDto;
import com.gitlab.jeeto.oboco.common.NaturalOrderComparator;
import com.gitlab.jeeto.oboco.database.AppDatabase;
import com.gitlab.jeeto.oboco.database.Book;
import com.gitlab.jeeto.oboco.fragment.BookReaderFragment;
import com.gitlab.jeeto.oboco.reader.BookReader;
import com.gitlab.jeeto.oboco.reader.ZipBookReader;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Request;
import com.squareup.picasso.RequestHandler;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
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
import okio.Okio;

public class LocalBookReaderManager extends BookReaderManager {
    public static final String PARAM_BOOK_PATH = "PARAM_BOOK_PATH";
    private BookReaderFragment mBookReaderFragment;
    private BookReader mBookReader;
    private File mBookFile;
    private AppDatabase mAppDatabase;

    public LocalBookReaderManager(BookReaderFragment bookReaderFragment) {
        super();
        mBookReaderFragment = bookReaderFragment;
    }

    @Override
    public void create(Bundle savedInstanceState) {
        Bundle bundle = mBookReaderFragment.getArguments();

        String bookPath = bundle.getString(LocalBookReaderManager.PARAM_BOOK_PATH);

        mBookFile = new File(bookPath);

        try {
            mBookReader = new ZipBookReader(mBookFile);
            mBookReader.create();
        } catch(Exception e) {
            mBookReaderFragment.onError(e);
        }

        mAppDatabase = Room.databaseBuilder(mBookReaderFragment.getActivity().getApplicationContext(), AppDatabase.class, "database").build();
    }

    @Override
    public void destroy() {
        try {
            mBookReader.destroy();
        } catch(Exception e) {
            mBookReaderFragment.onError(e);
        }
    }

    @Override
    public void saveInstanceState(Bundle outState) {

    }

    @Override
    public void load() {
        BookDto bookDto = new BookDto();;
        List<BookDto> bookListDto = new ArrayList<BookDto>();

        File[] files = mBookFile.getParentFile().listFiles();
        List<File> bookFileList = new ArrayList<File>();
        for(File file: files) {
            if(file.isFile() && file.getName().endsWith(".cbz")) {
                bookFileList.add(file);
            }
        }

        Collections.sort(bookFileList, new NaturalOrderComparator<File>() {
            @Override
            public String toString(File o) {
                return o.getName();
            }
        });

        Integer index = 0;
        while(index < bookFileList.size()) {
            File bookFile = bookFileList.get(index);

            if(bookFile.getName().equals(mBookFile.getName())) {
                if(index - 1 >= 0) {
                    File previousBookFile = bookFileList.get(index - 1);

                    BookDto previousBookDto = new BookDto();
                    previousBookDto.setName(previousBookFile.getName());
                    previousBookDto.setNumberOfPages(0);
                    previousBookDto.setPath(previousBookFile.getAbsolutePath());

                    bookListDto.add(previousBookDto);
                }
                bookDto.setName(bookFile.getName());
                bookDto.setNumberOfPages(mBookReader.getNumberOfPages());
                bookDto.setPath(bookFile.getAbsolutePath());

                bookListDto.add(bookDto);
                if(index + 1 < bookFileList.size()) {
                    File nextBookFile = bookFileList.get(index + 1);

                    BookDto nextBookDto = new BookDto();
                    nextBookDto.setName(nextBookFile.getName());
                    nextBookDto.setNumberOfPages(0);
                    nextBookDto.setPath(nextBookFile.getAbsolutePath());

                    bookListDto.add(nextBookDto);
                }
                break;
            }

            index = index + 1;
        }

        Single<List<Book>> single = mAppDatabase.bookDao().findByPath(mBookFile.getAbsolutePath());
        single = single.observeOn(AndroidSchedulers.mainThread());
        single = single.subscribeOn(Schedulers.io());
        single.subscribe(new SingleObserver<List<Book>>() {
            @Override
            public void onSubscribe(Disposable disposable) {

            }

            @Override
            public void onSuccess(List<Book> bookList) {
                if(bookList.size() == 1) {
                    Book book = bookList.get(0);

                    BookMarkDto bookMarkDto = new BookMarkDto();
                    bookMarkDto.setPage(book.page);

                    bookDto.setBookMark(bookMarkDto);
                }

                mBookReaderFragment.onLoad(bookDto, bookListDto);
            }

            @Override
            public void onError(Throwable e) {
                mBookReaderFragment.onError(e);
            }
        });
    }

    @Override
    public void addBookMark(int bookPage) {
        Single<List<Book>> single = mAppDatabase.bookDao().findByPath(mBookFile.getAbsolutePath());
        single = single.observeOn(AndroidSchedulers.mainThread());
        single = single.subscribeOn(Schedulers.io());
        single.subscribe(new SingleObserver<List<Book>>() {
            @Override
            public void onSubscribe(Disposable disposable) {

            }

            @Override
            public void onSuccess(List<Book> bookList) {
                if(bookList.size() == 1) {
                    Book book = bookList.get(0);
                    book.page = bookPage;

                    Completable completable = mAppDatabase.bookDao().update(book);
                    completable = completable.observeOn(AndroidSchedulers.mainThread());
                    completable = completable.subscribeOn(Schedulers.io());
                    completable.subscribe(new CompletableObserver() {
                        @Override
                        public void onSubscribe(Disposable disposable) {

                        }

                        @Override
                        public void onComplete() {
                            BookMarkDto bookMarkDto = new BookMarkDto();
                            bookMarkDto.setPage(bookPage);

                            mBookReaderFragment.onAddBookMark(bookMarkDto);
                        }

                        @Override
                        public void onError(Throwable e) {
                            mBookReaderFragment.onError(e);
                        }
                    });
                } else {
                    Book book = new Book();
                    book.path = mBookFile.getAbsolutePath();
                    book.bookCollectionPath = mBookFile.getParentFile().getAbsolutePath();
                    book.page = bookPage;
                    book.numberOfPages = mBookReader.getNumberOfPages();

                    Completable completable = mAppDatabase.bookDao().create(book);
                    completable = completable.observeOn(AndroidSchedulers.mainThread());
                    completable = completable.subscribeOn(Schedulers.io());
                    completable.subscribe(new CompletableObserver() {
                        @Override
                        public void onSubscribe(Disposable disposable) {

                        }

                        @Override
                        public void onComplete() {
                            BookMarkDto bookMarkDto = new BookMarkDto();
                            bookMarkDto.setPage(bookPage);

                            mBookReaderFragment.onAddBookMark(bookMarkDto);
                        }

                        @Override
                        public void onError(Throwable e) {
                            mBookReaderFragment.onError(e);
                        }
                    });
                }
            }

            @Override
            public void onError(Throwable e) {
                mBookReaderFragment.onError(e);
            }
        });
    }

    public Uri getBookPageUri(int bookPage) {
        return new Uri.Builder()
                .scheme("bookReaderManager")
                .authority("")
                .path("/bookPage")
                .appendQueryParameter("bookPage", Integer.toString(bookPage))
                .build();
    }

    @Override
    public boolean canHandleRequest(Request request) {
        return request.uri.getScheme().equals("bookReaderManager");
    }

    @Override
    public Result load(Request request, int networkPolicy) throws IOException {
        if(request.uri.getPath().equals("/bookPage")) {
            Integer bookPage = Integer.parseInt(request.uri.getQueryParameter("bookPage"));

            InputStream inputStream = mBookReader.getPage(bookPage);

            return new RequestHandler.Result(Okio.source(inputStream), Picasso.LoadedFrom.DISK);
        } else {
            throw new IOException("uri is invalid");
        }
    }
}
