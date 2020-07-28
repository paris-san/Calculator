package com.paris.calculator.ui;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.widget.NestedScrollView;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.paris.calculator.R;
import com.paris.calculator.databinding.FragmentCalculatorBinding;
import com.paris.calculator.viewmodels.CalculatorViewModel;


public class CalculatorFragment extends Fragment {

    private Context mContext;
    private NestedScrollView scrollView;

    private CalculatorViewModel calculatorViewModel;

    public static CalculatorFragment newInstance() {
        return new CalculatorFragment();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mContext = context;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        calculatorViewModel = new ViewModelProvider(this).get(CalculatorViewModel.class);
        calculatorViewModel.getScreenResult().observe(this, this::sendResultToConverter);
    }


    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        FragmentCalculatorBinding fragmentCalculatorBinding = DataBindingUtil
                .inflate(LayoutInflater.from(container.getContext()), R.layout.fragment_calculator, container, false);
        fragmentCalculatorBinding.setCalculatorViewModel(calculatorViewModel);
        fragmentCalculatorBinding.setLifecycleOwner(this);
        calculatorViewModel.getScreenPrevious().observe(getViewLifecycleOwner(),
                __ -> scrollView.post(() -> scrollView.fullScroll(View.FOCUS_DOWN)));               //When the previous operations increase, scroll to bottom

        View rootView = fragmentCalculatorBinding.getRoot();
        scrollView = rootView.findViewById(R.id.scrollView);
        return rootView;
    }


    /**
     * Since androidx.fragment:fragment:1.3.0-alpha04 we can use FragmentManager
     * to send data directly to another Fragment.
     */
    private void sendResultToConverter(String resultNumber) {
        Bundle result = new Bundle();
        result.putString("resultNumber", resultNumber);
        getParentFragmentManager().setFragmentResult("key", result);
    }

}