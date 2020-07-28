package com.paris.calculator.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.paris.calculator.R;
import com.paris.calculator.retrofit.NoInternetException;
import com.paris.calculator.retrofit.RatesCall;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.LinkedHashMap;
import java.util.Locale;

public class ConverterViewModel extends AndroidViewModel implements RatesCall.GetRatesListener {

    private Application application;
    public final static String GETTING_RATES = "GETTING_RATES";
    public final static String RATES_FETCHED = "RATES_FETCHED";
    public final static String RATES_FAILED = "RATES_FAILED";
    private MutableLiveData<String> progressState = new MutableLiveData<>();
    private MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private MutableLiveData<String> firstCurrency = new MutableLiveData<>();
    private MutableLiveData<String> secondCurrency = new MutableLiveData<>();
    private MutableLiveData<String> inputValue = new MutableLiveData<>();
    private MutableLiveData<String> resultValue = new MutableLiveData<>();
    private MutableLiveData<Boolean> selectingFirstCurrency = new MutableLiveData<>();
    private MutableLiveData<Boolean> selectingSecondCurrency = new MutableLiveData<>();
    private MutableLiveData<LinkedHashMap<String, Double>> ratesMap = new MutableLiveData<>();


    public ConverterViewModel(@NonNull Application application) {
        super(application);
        this.application = application;
        firstCurrency.setValue("EUR");
        secondCurrency.setValue("USD");
        inputValue.setValue(String.valueOf(1));
        getRates();
    }


    private void getRates() {
        try {
            new RatesCall(application, this);
            progressState.setValue(GETTING_RATES);
        } catch (NoInternetException e) {
            progressState.setValue(RATES_FAILED);
            errorMessage.setValue(application.getString(R.string.no_internet_connection));
        }
    }


    private void calculateRate() {
        String input = inputValue.getValue();
        String fCurrency = firstCurrency.getValue();
        String sCurrency = secondCurrency.getValue();
        if (input != null && !input.equals("") && fCurrency != null
                && sCurrency != null && ratesMap.getValue() != null) {                              //If all required values exist
            input = input.replace(",", "");                                         //Remove all commas
            BigDecimal inputBigDecimal = new BigDecimal(input);
            ratesMap.getValue().get(fCurrency);
            if (ratesMap.getValue().get(fCurrency) != null && ratesMap.getValue().get(sCurrency) != null) {
                BigDecimal firstCurrencyRate = BigDecimal.valueOf(ratesMap.getValue().get(fCurrency));
                BigDecimal secondCurrencyRate = BigDecimal.valueOf(ratesMap.getValue().get(sCurrency));
                BigDecimal finalRate = secondCurrencyRate
                        .divide(firstCurrencyRate, 5, RoundingMode.DOWN);                       //Divide currency rates
                BigDecimal result = finalRate.multiply(inputBigDecimal)
                        .setScale(5, RoundingMode.DOWN);                                    //Multiply with input number to get result
                result = result.compareTo(BigDecimal.ZERO) == 0 ?
                        BigDecimal.ZERO : result.stripTrailingZeros();                              //Remove trailing zeros
                resultValue.setValue(formatWithCommas(result));

            }
        } else {
            resultValue.setValue(String.valueOf(0));
        }
    }


    @Override
    public void onRatesReceived(LinkedHashMap<String, Double> ratesMap) {
        progressState.setValue(RATES_FETCHED);
        this.ratesMap.setValue(ratesMap);
        calculateRate();
    }


    @Override
    public void onRatesFailed(String error) {
        progressState.setValue(RATES_FAILED);
        errorMessage.setValue(application.getString(R.string.generic_error));
    }


    public void onGetRatesClicked() {
        getRates();
    }


    public void onReverseCLicked() {
        String fCurrency = firstCurrency.getValue();
        String sCurrency = secondCurrency.getValue();
        if (fCurrency != null && sCurrency != null) {
            firstCurrency.setValue(sCurrency);
            secondCurrency.setValue(fCurrency);
        }
        calculateRate();
    }

    public void onFirstCurrencyClicked() {
        selectingFirstCurrency.setValue(true);
        selectingSecondCurrency.setValue(false);
    }


    public void onSecondCurrencyClicked() {
        selectingFirstCurrency.setValue(false);
        selectingSecondCurrency.setValue(true);
    }


    public void onTextChanged(CharSequence s, int start, int before, int count) {
        if (s.length() > 0) {
            String input = String.valueOf(s).replace(",", "");                   //Remove all commas
            String formattedInput = formatWithCommas(new BigDecimal(input));
            if (s.charAt(s.length() - 1) == '.') {
                formattedInput += '.';
            }
            inputValue.setValue(formattedInput);
            calculateRate();
        } else {
            resultValue.setValue(String.valueOf(0));
        }
    }


    public void currencySelected(String currencyName) {
        if (selectingFirstCurrency.getValue() != null && selectingFirstCurrency.getValue()) {
            firstCurrency.setValue(currencyName);
        } else if (selectingSecondCurrency.getValue() != null && selectingSecondCurrency.getValue()) {
            secondCurrency.setValue(currencyName);
        }
        selectingFirstCurrency.setValue(false);
        selectingSecondCurrency.setValue(false);
        calculateRate();
    }


    /**
     * Use DecimalFormat to add comma separators between thousands, millions etc.
     */
    private String formatWithCommas(BigDecimal number) {
        DecimalFormat formatter = (DecimalFormat) NumberFormat.getInstance(Locale.getDefault());
        DecimalFormatSymbols symbols = formatter.getDecimalFormatSymbols();
        symbols.setGroupingSeparator(',');
        formatter.setDecimalFormatSymbols(symbols);
        formatter.setMaximumFractionDigits(5);
        formatter.setMaximumIntegerDigits(100);
        String inputString = number.toPlainString();
        if (inputString.contains(".")) {                                                            //DecimalFormat hides decimal points if the amount to zero
            return formatter.format(new BigDecimal(inputString.substring(0, inputString.indexOf("."))))
                    + inputString.substring(inputString.indexOf("."));                              //so we handle the decimal part separately
        } else {
            return formatter.format(number);
        }
    }


    public MutableLiveData<String> getFirstCurrency() {
        return firstCurrency;
    }

    public void setFirstCurrency(MutableLiveData<String> firstCurrency) {
        this.firstCurrency = firstCurrency;
    }

    public MutableLiveData<String> getSecondCurrency() {
        return secondCurrency;
    }

    public void setSecondCurrency(MutableLiveData<String> secondCurrency) {
        this.secondCurrency = secondCurrency;
    }

    public MutableLiveData<String> getInputValue() {
        return inputValue;
    }

    public void setInputValue(String inputValue) {
        this.inputValue.setValue(inputValue);
    }

    public MutableLiveData<String> getResultValue() {
        return resultValue;
    }

    public void setResultValue(MutableLiveData<String> resultValue) {
        this.resultValue = resultValue;
    }

    public MutableLiveData<LinkedHashMap<String, Double>> getRatesMap() {
        return ratesMap;
    }

    public void setRatesMap(MutableLiveData<LinkedHashMap<String, Double>> ratesMap) {
        this.ratesMap = ratesMap;
    }

    public MutableLiveData<String> getProgressState() {
        return progressState;
    }

    public MutableLiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public MutableLiveData<Boolean> getSelectingFirstCurrency() {
        return selectingFirstCurrency;
    }

    public void setSelectingFirstCurrency(MutableLiveData<Boolean> selectingFirstCurrency) {
        this.selectingFirstCurrency = selectingFirstCurrency;
    }

    public MutableLiveData<Boolean> getSelectingSecondCurrency() {
        return selectingSecondCurrency;
    }

    public void setSelectingSecondCurrency(MutableLiveData<Boolean> selectingSecondCurrency) {
        this.selectingSecondCurrency = selectingSecondCurrency;
    }

}