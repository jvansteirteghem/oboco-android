package com.gitlab.jeeto.oboco.database;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "book", indices = {@Index(value = {"path"}, unique = true), @Index(value = {"book_collection_path"}, unique = false)})
public class Book {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    @NonNull
    public long id;

    @ColumnInfo(name = "path")
    @NonNull
    public String path;

    @ColumnInfo(name = "book_collection_path")
    @NonNull
    public String bookCollectionPath;

    @ColumnInfo(name = "page")
    public int page;

    @ColumnInfo(name = "number_of_pages")
    public int numberOfPages;
}
