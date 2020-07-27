package com.paris.calculator.ui;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;
import com.paris.calculator.R;
import com.paris.calculator.databinding.ActivityMainBinding;

import static com.paris.calculator.utils.KeyboardUtils.hideSoftKeyboard;

public class MainActivity extends AppCompatActivity {

    private int visibleFragment;
    public static final int CALCULATOR_FRAGMENT = 0;
    public static final int CONVERTER_FRAGMENT = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityMainBinding activityMainBinding = DataBindingUtil.setContentView(this, R.layout.activity_main);

        ViewPager viewPager = findViewById(R.id.view_pager);
        TabLayout tabs = findViewById(R.id.tabs);

        MainPagerAdapter mainPagerAdapter = new MainPagerAdapter(getSupportFragmentManager(), this);
        viewPager.setAdapter(mainPagerAdapter);
        viewPager.setPageTransformer(true, new ViewPagerTransformer());
        tabs.setupWithViewPager(viewPager);
        viewPager.addOnPageChangeListener(setupPageChangeListener());
    }


    /**
     * Add a page listener on ViewPager, to close the soft keyboard when we change page.
     **/
    private ViewPager.OnPageChangeListener setupPageChangeListener() {
        return new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {                                              //When the current page of the ViewPager changes
                visibleFragment = position == 0 ? CALCULATOR_FRAGMENT : CONVERTER_FRAGMENT;
                hideSoftKeyboard(MainActivity.this);
            }

            @Override
            public void onPageScrollStateChanged(int arg0) {
            }

            @Override
            public void onPageScrolled(int arg0, float arg1, int arg2) {
            }
        };
    }

    public int getVisibleFragment() {
        return visibleFragment;
    }
}