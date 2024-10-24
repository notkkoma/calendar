package com.example.calendar;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface HolidayAPI {
    @GET("getRestDeInfo")
    Call<HolidayResponse> getHolidays(
            @Query("serviceKey") String serviceKey,
            @Query("solYear") String solYear,
            @Query("solMonth") String solMonth
    );
}