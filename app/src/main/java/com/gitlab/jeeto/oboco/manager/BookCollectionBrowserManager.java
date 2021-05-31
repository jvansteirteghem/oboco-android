package com.gitlab.jeeto.oboco.manager;

import com.gitlab.jeeto.oboco.api.BookCollectionDto;
import com.gitlab.jeeto.oboco.api.BookDto;
import com.gitlab.jeeto.oboco.fragment.LibraryFragment;

import java.io.IOException;
import java.io.InputStream;

public abstract class BookCollectionBrowserManager {
    public abstract void create(LibraryFragment libraryFragment);
    public abstract void destroy();
    public abstract void load(String bookCollectionName, int page, int pageSize);
    public abstract void loadBookCollectionPageableList(String bookCollectionName, int page, int pageSize);
    public abstract void addBookMark(BookCollectionDto bookCollection);
    public abstract void removeBookMark(BookCollectionDto bookCollection);
    public abstract InputStream getBookCollectionPage(BookCollectionDto bookCollection, String scaleType, int scaleWidth, int scaleHeight) throws IOException;
}
