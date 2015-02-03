package com.bryankrosenbaum.journaldb.app.rest;

import com.bryankrosenbaum.journaldb.app.models.EntryDate;
import com.bryankrosenbaum.journaldb.app.models.Journal;
import com.bryankrosenbaum.journaldb.app.models.RequestWrapper;

import java.util.ArrayList;
import java.util.List;

import retrofit.Callback;
import retrofit.http.Body;
import retrofit.http.Field;
import retrofit.http.FormUrlEncoded;
import retrofit.http.GET;
import retrofit.http.Multipart;
import retrofit.http.POST;
import retrofit.http.PUT;
import retrofit.http.Part;
import retrofit.http.Path;
import retrofit.http.Query;

public interface JournalService {

    /**
     * Creates new journal entry
     *
     * @param requestWrapper
     * @param cb
     */
    @POST("/journal")
    void createEntry(@Body RequestWrapper requestWrapper, Callback<Journal> cb);

    /**
     * Updates journal entry
     *
     * @param requestWrapper
     * @param cb
     */
    @PUT("/journal")
    void updateEntry(@Body RequestWrapper requestWrapper, Callback<Journal> cb);

    /**
     * Get journal entries for day
     *
     * @param month
     * @param dayOfMonth
     * @param cb
     */
    @GET("/journal/month/{month}/dayOfMonth/{dayOfMonth}")
    void getEntriesForDay(@Path("month") int month, @Path("dayOfMonth") int dayOfMonth, Callback<List<Journal>> cb);

    /**
     * Get journal entries for specified date
     *
     * @param year
     * @param month
     * @param dayOfMonth
     * @param cb
     */
    @GET("/journal/year/{year}/month/{month}/dayOfMonth/{dayOfMonth}")
    void getEntriesForDate(@Path("year") int year, @Path("month") int month, @Path("dayOfMonth") int dayOfMonth, Callback<Journal> cb);

    /**
     * Get test
     *
     * @param cb
     */
    @GET("/test")
    void getTest(Callback<String> cb);

}
