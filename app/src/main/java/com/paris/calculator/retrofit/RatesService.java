package com.paris.calculator.retrofit;

import com.paris.calculator.models.FixerResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface RatesService {

    @GET("latest")
    Call<FixerResponse> getAllRates(@Query("access_key") String accessKey);

}
