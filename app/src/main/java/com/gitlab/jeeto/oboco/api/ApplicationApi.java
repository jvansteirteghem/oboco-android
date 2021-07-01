package com.gitlab.jeeto.oboco.api;

import io.reactivex.Completable;
import io.reactivex.Single;
import okhttp3.ResponseBody;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.PATCH;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;
import retrofit2.http.Streaming;

public interface ApplicationApi {
    @Headers("Content-Type: application/json")
    @GET("/api/v1/users/ME")
    public Single<UserDto> getAuthenticatedUser();

    @Headers("Content-Type: application/json")
    @PATCH("/api/v1/users/ME/password")
    public Single<UserDto> updateAuthenticatedUserPassword(@Body UserPasswordDto userPassword);

    @Headers("Content-Type: application/json")
    @GET("/api/v1/bookCollections")
    public Single<PageableListDto<BookCollectionDto>> getBookCollections(@Query("parentBookCollectionId") Long parentBookCollectionId, @Query("name") String name, @Query("page") Integer page, @Query("pageSize") Integer pageSize, @Query("graph") String graph);

    @Headers("Content-Type: application/json")
    @GET("/api/v1/bookCollections/ROOT")
    public Single<BookCollectionDto> getRootBookCollection(@Query("graph") String graph);

    @Headers("Content-Type: application/json")
    @GET("/api/v1/bookCollections/{id}")
    public Single<BookCollectionDto> getBookCollection(@Path("id") Long id, @Query("graph") String graph);

    @Headers("Content-Type: application/json")
    @GET("/api/v1/bookCollections/{bookCollectionId}/books/{bookId}/books")
    public Single<LinkableDto<BookDto>> getBooks(@Path("bookCollectionId") Long bookCollectionId, @Path("bookId") Long id, @Query("graph") String graph);

    @Headers("Content-Type: application/json")
    @GET("/api/v1/bookCollections/{bookCollectionId}/books")
    public Single<PageableListDto<BookDto>> getBooks(@Path("bookCollectionId") Long bookCollectionId, @Query("bookMarkStatus") String bookMarkStatus, @Query("page") Integer page, @Query("pageSize") Integer pageSize, @Query("graph") String graph);

    @Headers("Content-Type: application/json")
    @GET("/api/v1/books/{id}")
    public Single<BookDto> getBook(@Path("id") Long id, @Query("graph") String graph);

    @Headers("Content-Type: application/json")
    @GET("/api/v1/bookMarks")
    public Single<PageableListDto<BookMarkDto>> getBookMarks(@Query("page") Integer page, @Query("pageSize") Integer pageSize, @Query("graph") String graph);

    @Headers("Content-Type: application/json")
    @GET("/api/v1/books/{bookId}/bookMark")
    public Single<BookMarkDto> getBookMark(@Path("bookId") Long bookId, @Query("graph") String graph);

    @Headers("Content-Type: application/json")
    @PUT("/api/v1/books/{bookId}/bookMark")
    public Single<BookMarkDto> createOrUpdateBookMark(@Path("bookId") Long bookId, @Body BookMarkDto bookMark);

    @Headers("Content-Type: application/json")
    @DELETE("/api/v1/books/{bookId}/bookMark")
    public Completable deleteBookMark(@Path("bookId") Long bookId);

    @Headers("Content-Type: application/json")
    @PUT("/api/v1/bookCollections/{bookCollectionId}/bookMarks")
    public Completable createOrUpdateBookMarks(@Path("bookCollectionId") Long bookCollectionId);

    @Headers("Content-Type: application/json")
    @DELETE("/api/v1/bookCollections/{bookCollectionId}/bookMarks")
    public Completable deleteBookMarks(@Path("bookCollectionId") Long bookCollectionId);

    @Streaming
    @Headers("Content-Type: image/jpeg")
    @GET("/api/v1/bookCollections/{bookCollectionId}/books/FIRST/pages/1.jpg")
    public Single<ResponseBody> downloadBookCollectionPage(@Path("bookCollectionId") Long bookCollectionId, @Query("scaleType") String scaleType, @Query("scaleWidth") Integer scaleWidth, @Query("scaleHeight") Integer scaleHeight);

    @Streaming
    @Headers("Content-Type: image/jpeg")
    @GET("/api/v1/books/{bookId}/pages/{page}.jpg")
    public Single<ResponseBody> downloadBookPage(@Path("bookId") Long bookId, @Path("page") Integer page, @Query("scaleType") String scaleType, @Query("scaleWidth") Integer scaleWidth, @Query("scaleHeight") Integer scaleHeight);

    @Streaming
    @Headers("Content-Type: application/zip")
    @GET("/api/v1/books/{bookId}.cbz")
    public Single<ResponseBody> downloadBook(@Path("bookId") Long bookId);

    @Headers("Content-Type: application/json")
    @GET("/api/v1/bookCollections/LATEST/bookCollections")
    public Single<PageableListDto<BookCollectionDto>> getLatestBookCollections(@Query("name") String name, @Query("page") Integer page, @Query("pageSize") Integer pageSize, @Query("graph") String graph);

}
