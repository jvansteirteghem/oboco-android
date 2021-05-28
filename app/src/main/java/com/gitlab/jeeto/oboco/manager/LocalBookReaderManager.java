package com.gitlab.jeeto.oboco.manager;

import com.gitlab.jeeto.oboco.api.BookDto;
import com.gitlab.jeeto.oboco.api.BookMarkDto;
import com.gitlab.jeeto.oboco.fragment.ReaderFragment;
import com.gitlab.jeeto.oboco.reader.BookReader;
import com.gitlab.jeeto.oboco.reader.ZipBookReader;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class LocalBookReaderManager extends BookReaderManager {
    private ReaderFragment mReaderFragment;
    private BookReader mBookReader;
    private File mBookFile;

    public LocalBookReaderManager(File bookFile) {
        super();
        mBookFile = bookFile;
    }

    @Override
    public void create(ReaderFragment readerFragment) {
        mReaderFragment = readerFragment;

        try {
            mBookReader = new ZipBookReader(mBookFile);
            mBookReader.create();
        } catch(Exception e) {
            mReaderFragment.onError(e);
        }
    }

    @Override
    public void destroy() {
        try {
            mBookReader.destroy();
        } catch(Exception e) {
            mReaderFragment.onError(e);
        }
    }

    @Override
    public void loadBook() {
        BookDto book = new BookDto();
        book.setId(0L);
        book.setName(mBookFile.getName());
        book.setNumberOfPages(mBookReader.getNumberOfPages());

        List<BookDto> bookList = new ArrayList<BookDto>();
        bookList.add(book);

        mReaderFragment.setBook(book);
        mReaderFragment.setBookList(bookList);
        mReaderFragment.loadBook();
    }

    @Override
    public void saveBookMark(int bookPage) {
        BookMarkDto bookMark = new BookMarkDto();
        bookMark.setPage(bookPage);

        BookDto book = mReaderFragment.getBook();
        book.setBookMark(bookMark);
    }

    public InputStream getBookPage(int bookPage) throws IOException {
        InputStream inputStream = mBookReader.getPage(bookPage);

        return inputStream;
    }
}
