package com.gitlab.jeeto.oboco.manager;

import android.net.Uri;
import android.os.Bundle;

import com.gitlab.jeeto.oboco.api.BookDto;
import com.gitlab.jeeto.oboco.api.BookMarkDto;
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
import java.util.List;

import okio.Okio;

public class LocalBookReaderManager extends BookReaderManager {
    public static final String PARAM_BOOK_FILE = "PARAM_BOOK_FILE";
    private BookReaderFragment mBookReaderFragment;
    private BookReader mBookReader;
    private File mBookFile;

    public LocalBookReaderManager(BookReaderFragment bookReaderFragment) {
        super();
        mBookReaderFragment = bookReaderFragment;
    }

    @Override
    public void create(Bundle savedInstanceState) {
        Bundle bundle = mBookReaderFragment.getArguments();

        mBookFile = (File) bundle.getSerializable(LocalBookReaderManager.PARAM_BOOK_FILE);

        try {
            mBookReader = new ZipBookReader(mBookFile);
            mBookReader.create();
        } catch(Exception e) {
            mBookReaderFragment.onError(e);
        }
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
        BookDto book = new BookDto();
        book.setId(0L);
        book.setName(mBookFile.getName());
        book.setNumberOfPages(mBookReader.getNumberOfPages());

        List<BookDto> bookList = new ArrayList<BookDto>();
        bookList.add(book);

        mBookReaderFragment.onLoad(book, bookList);
    }

    @Override
    public void addBookMark(int bookPage) {
        BookMarkDto bookMark = new BookMarkDto();
        bookMark.setPage(bookPage);

        mBookReaderFragment.onAddBookMark(bookMark);
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
