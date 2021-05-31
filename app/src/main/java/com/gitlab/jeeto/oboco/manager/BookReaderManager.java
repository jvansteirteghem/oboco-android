package com.gitlab.jeeto.oboco.manager;

import com.gitlab.jeeto.oboco.fragment.ReaderFragment;

import java.io.IOException;
import java.io.InputStream;

public abstract class BookReaderManager {
    public abstract void create(ReaderFragment fragment);
    public abstract void destroy();
    public abstract void load();
    public abstract void addBookMark(int bookPage);
    public abstract InputStream getBookPage(int bookPage) throws IOException;
}
