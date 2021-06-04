package com.gitlab.jeeto.oboco.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Single;

@Dao
public interface BookDao {
    @Query("SELECT * FROM book WHERE path = :path")
    Single<Book> findByPath(String path);

    @Query("SELECT * FROM book WHERE book_collection_path = :bookCollectionPath")
    Single<List<Book>> findByBookCollectionPath(String bookCollectionPath);

    @Insert
    Completable create(Book book);

    @Update
    Completable update(Book book);

    @Delete
    Completable delete(Book book);
}
