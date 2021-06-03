package com.gitlab.jeeto.oboco.manager;

import android.net.Uri;
import android.os.Bundle;

import com.gitlab.jeeto.oboco.api.BookDto;
import com.gitlab.jeeto.oboco.api.BookMarkDto;
import com.gitlab.jeeto.oboco.common.NaturalOrderComparator;
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

import okio.Okio;

public class LocalBookReaderManager extends BookReaderManager {
    public static final String PARAM_BOOK_PATH = "PARAM_BOOK_PATH";
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

        String bookPath = bundle.getString(LocalBookReaderManager.PARAM_BOOK_PATH);

        mBookFile = new File(bookPath);

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
        BookDto book = null;
        List<BookDto> bookList = new ArrayList<BookDto>();

        File[] files = mBookFile.getParentFile().listFiles();
        List<File> bookFileList = new ArrayList<File>();
        for(File file: files) {
            if(file.isFile() && file.getName().endsWith(".cbz")) {
                bookFileList.add(file);
            }
        }

        Collections.sort(bookFileList, new NaturalOrderComparator() {
            @Override
            public String stringValue(Object o) {
                return ((File) o).getName();
            }
        });

        Integer index = 0;
        while(index < bookFileList.size()) {
            File bookFile = bookFileList.get(index);

            if(bookFile.getName().equals(mBookFile.getName())) {
                if(index - 1 >= 0) {
                    File previousBookFile = bookFileList.get(index - 1);

                    BookDto previousBook = new BookDto();
                    previousBook.setId(new Long(index - 1));
                    previousBook.setName(previousBookFile.getName());
                    previousBook.setNumberOfPages(0);
                    previousBook.setPath(previousBookFile.getAbsolutePath());

                    bookList.add(previousBook);
                }
                book = new BookDto();
                book.setId(new Long(index));
                book.setName(bookFile.getName());
                book.setNumberOfPages(mBookReader.getNumberOfPages());
                book.setPath(bookFile.getAbsolutePath());

                bookList.add(book);
                if(index + 1 < bookFileList.size()) {
                    File nextBookFile = bookFileList.get(index + 1);

                    BookDto nextBook = new BookDto();
                    nextBook.setId(new Long(index + 1));
                    nextBook.setName(nextBookFile.getName());
                    nextBook.setNumberOfPages(0);
                    nextBook.setPath(nextBookFile.getAbsolutePath());

                    bookList.add(nextBook);
                }
                break;
            }

            index = index + 1;
        }

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
