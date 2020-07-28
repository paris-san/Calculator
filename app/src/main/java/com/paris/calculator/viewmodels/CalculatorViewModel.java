package com.paris.calculator.viewmodels;

import android.app.Application;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.paris.calculator.R;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.Locale;

public class CalculatorViewModel extends AndroidViewModel {

    private Application application;
    private MutableLiveData<String> screenResult = new MutableLiveData<>();
    private MutableLiveData<String> screenPrevious = new MutableLiveData<>();
    private BigDecimal lastResult = BigDecimal.valueOf(0);
    private String stringPrevious = "";
    private String currentNumberInput = "";
    private String currentOperation = "+";                                                          //Set addition for the first input, so lastResult gets a value


    public CalculatorViewModel(@NonNull Application application) {
        super(application);
        this.application = application;
        screenPrevious.setValue("");
        screenResult.setValue("0");
    }


    /**
     * When the user clicks on operation symbol or C/CE.
     */
    public void onOperationClicked(View view) {
        int id = view.getId();
        switch (id) {
            case R.id.operation_c:
                clearAll();
                break;
            case R.id.operation_ce:
                clearCurrent();
                break;
            case R.id.operation_percent:
                calculatePercentage();
                break;
            case R.id.operation_divide:
                setNewOperation("÷");
                break;
            case R.id.operation_multiply:
                setNewOperation("*");
                break;
            case R.id.operation_subtract:
                setNewOperation("-");
                break;
            case R.id.operation_add:
                setNewOperation("+");
                break;
            case R.id.operation_equals:
                setNewOperation("=");
                break;
        }
        screenPrevious.setValue(stringPrevious);
    }


    /**
     * When the user clicks on a number or "."
     */
    public void onNumberClicked(View view, char number) {
        if (number == '.' && currentNumberInput.contains(".")) {                                    //If we are already in decimals, ignore '.' characters
            return;
        }
        if ((currentNumberInput.equals("") || currentNumberInput.equals("0"))) {                    //If there is not current input
            currentNumberInput = number != '.' ? String.valueOf(number) : "0" + number;             //If the user clicks on "." add a "0" before
        } else {
            currentNumberInput = currentNumberInput + number;
        }
        screenResult.setValue(formatWithCommas(currentNumberInput));
    }


    /**
     * When the user clicks on [+ - * ÷ =]
     */
    private void setNewOperation(String operation) {
        if (changeOperationWithoutNumber()) {                                                       //If the current input is zero, just change the current operation
            stringPrevious = stringPrevious.substring(0, stringPrevious.length() - 2) + operation + " ";
            screenPrevious.setValue(stringPrevious);
            currentOperation = operation;
        } else {
            if (divideByZero()) {                                                                   //If user tries to divide by zero show message
                currentNumberInput = "";
                showToast(application.getString(R.string.divide_by_zero));
            } else if (currentNumberInput.length() > 0) {
                if (currentOperation.equals("=")) {                                                 //If last operation is "="
                    stringPrevious = currentNumberInput + " " + operation + " ";
                    lastResult = new BigDecimal(currentNumberInput);                                //Save the current input as starting sum
                } else {                                                                            //If last operation is "+ - * ÷"
                    stringPrevious = stringPrevious + currentNumberInput + " " + operation + " ";
                    calculateResult();
                    formatAndShowResult();
                    screenPrevious.setValue(stringPrevious);
                }
                currentNumberInput = "";
                currentOperation = operation;                                                       //Set operation for next step
            }
        }
    }


    /**
     * Show the result in full format or in E(+/-)x power depending on its length
     */
    private void formatAndShowResult() {
        if (Math.abs(lastResult.scale()) < 10) {                                                    //If scale is > 10 or < -10
            screenResult.setValue(formatWithCommas(lastResult.toPlainString()));                    //Show whole number
        } else {
            screenResult.setValue(String.valueOf(lastResult));                                      //Else use E(x) format
        }
    }


    /**
     * If the user tried to divide by zero, return true.
     */
    private boolean divideByZero() {
        return currentNumberInput.length() > 0
                && new BigDecimal(currentNumberInput).compareTo(BigDecimal.ZERO) == 0
                && currentOperation.equals("÷");
    }


    /**
     * If a valid operation was requested, perform operation with BigDecimal format.
     */
    private void calculateResult() {
        switch (currentOperation) {
            case "+":
                lastResult = lastResult.add(new BigDecimal(currentNumberInput));
                break;
            case "-":
                lastResult = lastResult.subtract(new BigDecimal(currentNumberInput));
                break;
            case "*":
                lastResult = lastResult.multiply(new BigDecimal(currentNumberInput));
                break;
            case "÷":
                lastResult = lastResult.divide(
                        new BigDecimal(currentNumberInput), 10, RoundingMode.DOWN);
                break;
        }
        lastResult = lastResult.stripTrailingZeros();                                               //remove zeros and return in E(+/-)x format
    }


    /**
     * When the user clicks on %, calculate the given percentage of the lastResult.
     */
    private void calculatePercentage() {
        if (currentNumberInput.length() > 0 && new BigDecimal(currentNumberInput).doubleValue() != 0) {
            BigDecimal percent = lastResult
                    .multiply(new BigDecimal(currentNumberInput))
                    .divide(new BigDecimal(100), 9, RoundingMode.DOWN);
            percent = percent.compareTo(BigDecimal.ZERO) == 0 ?
                    BigDecimal.ZERO : percent.stripTrailingZeros();
            if (Math.abs(percent.scale()) < 10) {                                                   //If scale is > 10 or < -10
                currentNumberInput = percent.toPlainString();                                       //Show whole number
            } else {
                currentNumberInput = String.valueOf(percent);                                       //Else use E(x) format
            }
            screenResult.setValue(formatWithCommas(currentNumberInput));
        }
    }


    /**
     * Use DecimalFormat to add comma separators between thousands, millions etc.
     */
    private String formatWithCommas(String number) {
        DecimalFormat formatter = (DecimalFormat) NumberFormat.getInstance(Locale.getDefault());
        DecimalFormatSymbols symbols = formatter.getDecimalFormatSymbols();
        symbols.setGroupingSeparator(',');
        formatter.setDecimalFormatSymbols(symbols);
        formatter.setMaximumFractionDigits(50);
        formatter.setMaximumIntegerDigits(100);
        if (number.contains(".")) {                                                                 //DecimalFormat hides decimal points if the amount to zero
            return formatter.format(new BigDecimal(number.substring(0, number.indexOf("."))))
                    + number.substring(number.indexOf("."));                                        //so we handle the decimal part separately
        } else {
            return formatter.format(new BigDecimal(number));
        }
    }


    /**
     * When the user clicks on operation symbol, without a number input, we simply change
     * the currentOperation and the end of the stringPrevious/screenPrevious.
     */
    private boolean changeOperationWithoutNumber() {
        return currentNumberInput.equals("")
                && stringPrevious.length() > 2
                && stringPrevious.substring(stringPrevious.length() - 2).matches("[-+*÷=]\\s");
    }


    private void showToast(String message) {
        Toast.makeText(application, message, Toast.LENGTH_LONG).show();
    }


    /**
     * When the user clicks on C, remove all previous operations.
     */
    private void clearAll() {
        currentOperation = "+";
        currentNumberInput = "";
        stringPrevious = "";
        screenResult.setValue("0");
        lastResult = BigDecimal.valueOf(0);
    }

    /**
     * When the user clicks on CE, remove current input and show 0 on result.
     */
    private void clearCurrent() {
        currentNumberInput = "";
        screenResult.setValue("0");
    }


    public MutableLiveData<String> getScreenResult() {
        return screenResult;
    }

    public void setScreenResult(MutableLiveData<String> screenResult) {
        this.screenResult = screenResult;
    }

    public MutableLiveData<String> getScreenPrevious() {
        return screenPrevious;
    }

    public void setScreenPrevious(MutableLiveData<String> screenPrevious) {
        this.screenPrevious = screenPrevious;
    }

}
