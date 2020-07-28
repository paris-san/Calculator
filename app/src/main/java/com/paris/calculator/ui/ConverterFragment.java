package com.paris.calculator.ui;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SearchView;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.paris.calculator.R;
import com.paris.calculator.databinding.FragmentConverterBinding;
import com.paris.calculator.viewmodels.ConverterViewModel;

import java.util.LinkedHashMap;

import static com.paris.calculator.utils.RecyclerUtils.getGridColumnsNumber;


public class ConverterFragment extends Fragment implements CurrencyRecyclerAdapter.ItemClickListener {

    private Context mContext;
    private ConverterViewModel converterViewModel;
    private CurrencyRecyclerAdapter currencyRecyclerAdapter;
    private RecyclerView currencyRecycler;
    private SearchView searchView;
    private EditText inputEditText;


    public static ConverterFragment newInstance() {
        return new ConverterFragment();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mContext = context;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        converterViewModel = new ViewModelProvider(this).get(ConverterViewModel.class);
        converterViewModel.getRatesMap().observe(this, this::setupCurrencyRecycler);
        converterViewModel.getErrorMessage().observe(this, this::showError);
        converterViewModel.getInputValue().observe(this, this::editTextChanged);
        createFragmentResultListener();

    }


    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        FragmentConverterBinding fragmentConverterBinding = DataBindingUtil
                .inflate(LayoutInflater.from(container.getContext()),
                        R.layout.fragment_converter, container, false);
        fragmentConverterBinding.setConverterViewModel(converterViewModel);
        fragmentConverterBinding.setLifecycleOwner(this);
        View rootView = fragmentConverterBinding.getRoot();
        currencyRecycler = rootView.findViewById(R.id.recycler_view);
        searchView = rootView.findViewById(R.id.search_view);
        inputEditText = rootView.findViewById(R.id.input_value);
        setupSearchListener();
        return rootView;
    }


    private void showError(String error) {
        if (((MainActivity) mContext).getVisibleFragment() == MainActivity.CONVERTER_FRAGMENT) {     //Show error only when the Converter is visible
            Toast.makeText(mContext, error, Toast.LENGTH_LONG).show();
        }
    }


    /**
     * Due to formatting the input number with commas, the cursor moved to the start.
     * We added this to set the cursor at the end and not disrupt the input process by the user.
     */
    private void editTextChanged(String text) {
        inputEditText.setSelection(inputEditText.getText().length());
    }


    private void setupCurrencyRecycler(LinkedHashMap<String, Double> ratesMap) {
        int gridColumns = getGridColumnsNumber((Activity) mContext);
        currencyRecycler.setLayoutManager(new GridLayoutManager(mContext, gridColumns));
        if (currencyRecyclerAdapter == null) {
            currencyRecyclerAdapter = new CurrencyRecyclerAdapter(mContext, ratesMap, this);
            currencyRecycler.setAdapter(currencyRecyclerAdapter);
        } else {
            currencyRecyclerAdapter.update(ratesMap);
        }
    }


    private void setupSearchListener() {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                currencyRecyclerAdapter.filter(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                currencyRecyclerAdapter.filter(newText);
                return true;
            }
        });
    }

    /**
     * Since androidx.fragment:fragment:1.3.0-alpha04 we can use FragmentManager
     * to send data directly to another Fragment.
     */
    private void createFragmentResultListener() {
        getParentFragmentManager().setFragmentResultListener(
                "key", this, (key, bundle) -> {
                    String result = bundle.getString("resultNumber");
                    if (result != null) {
                        double resultDouble = Double.parseDouble(result.replace(",", ""));       //Remove all commas
                        converterViewModel.setInputValue(resultDouble >= 0 ? result : "0");
                    } else {
                        converterViewModel.setInputValue("0");
                    }
                });
    }


    @Override
    public void onItemClick(View view, String currencyName) {
        converterViewModel.currencySelected(currencyName);
    }
}
