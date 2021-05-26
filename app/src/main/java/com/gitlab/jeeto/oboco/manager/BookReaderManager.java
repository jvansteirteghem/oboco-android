package com.gitlab.jeeto.oboco.manager;

import com.gitlab.jeeto.oboco.fragment.ReaderFragment;

public abstract class BookReaderManager {
    public abstract void create(ReaderFragment fragment);
    public abstract void destroy();
    public abstract BookHandler getBookHandler();
    public abstract void loadBook();
    public abstract void saveBookMark(int bookPage);
}
