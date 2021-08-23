package com.gitlab.jeeto.oboco.manager;

import android.os.Bundle;

import com.gitlab.jeeto.oboco.client.BookCollectionDto;
import com.gitlab.jeeto.oboco.client.BookDto;

public abstract class BrowserManager {
    public abstract void create(Bundle savedInstanceState);
    public abstract void destroy();
    public abstract void saveInstanceState(Bundle outState);
    public abstract void load();
    public abstract void load(BookCollectionDto bookCollectionDto);
    public abstract void deleteBookCollection(BookCollectionDto bookCollectionDto);
    public abstract void deleteBook(BookDto bookDto);
}
