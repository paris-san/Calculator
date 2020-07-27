package com.paris.calculator.retrofit;

import android.content.Context;

import androidx.annotation.NonNull;

import com.google.gson.JsonObject;
import com.paris.calculator.R;
import com.paris.calculator.models.FixerResponse;

import java.util.LinkedHashMap;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.paris.calculator.utils.ConnectionUtils.getInternetConnected;

public class RatesCall {

    private static final String accessKey = "dc704427c2518afcb0f19e8f5c9d74e5";
    private Context context;
    private GetRatesListener mListener;
    private RatesService service;
    private Call<FixerResponse> call;

    public RatesCall(Context context, GetRatesListener mListener) throws NoInternetException {
        this.context = context;
        this.mListener = mListener;
        checkInternetConnection(context);
        service = FixerBuilder.getRetrofitInstance().create(RatesService.class);
        enqueueCall();
    }

    private void enqueueCall() {
        call = service.getAllRates(accessKey);
        call.enqueue(new Callback<FixerResponse>() {
            @Override
            public void onResponse(@NonNull Call<FixerResponse> call,
                                   @NonNull Response<FixerResponse> response) {
                if (response.isSuccessful() && response.body() != null
                        && response.body().getSuccess()
                        && response.body().getRates() != null) {
                    mListener.onRatesReceived(createListOfRates(response.body().getRates()));
                } else {
                    if (response.body() == null || !response.body().getSuccess()) {
                        mListener.onRatesFailed("");
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<FixerResponse> call, @NonNull Throwable t) {
                mListener.onRatesFailed(t.getMessage());
            }
        });
    }


    /**
     * Parse the list of currencies and create a HashMap of currency -> rate.
     */
    private LinkedHashMap<String, Double> createListOfRates(JsonObject rates) {
        LinkedHashMap<String, Double> ratesMap = new LinkedHashMap<>();
        for (String currency : rates.keySet()) {
            ratesMap.put(currency, rates.get(currency).getAsDouble());
        }
        return ratesMap;
    }


    private void checkInternetConnection(Context context) throws NoInternetException {
        if (!getInternetConnected(context)) {
            throw new NoInternetException(
                    context.getResources().getString(R.string.no_internet_connection));
        }
    }


    public interface GetRatesListener {
        void onRatesReceived(LinkedHashMap<String, Double> fixerResponse);

        void onRatesFailed(String error);
    }

}
