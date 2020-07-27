package com.paris.calculator.ui;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.ViewPager;

public class ViewPagerTransformer implements ViewPager.PageTransformer {
    private static final float MIN_SCALE = 0.90f;

    public void transformPage(@NonNull View view, float position) {

        int pageWidth = view.getWidth();
        view.setLayerType(View.LAYER_TYPE_NONE, null);

        if (position < -1) {                                                                        // [-Infinity,-1)
            view.setAlpha(0);                                                                       // This page is way off-screen to the left.

        } else if (position <= 0) {                                                                 // [-1,0]
            view.setAlpha(1);                                                                       // Use the default slide transition when moving to the left page
            view.setTranslationX(0);
            view.setScaleX(1);
            view.setScaleY(1);

        } else if (position <= 1) {                                                                 // (0,1]
            view.setAlpha(1 - position);                                                            // Fade the page out.
            view.setTranslationX(pageWidth * -position);                                            // Counteract the default slide transition

            float scaleFactor = MIN_SCALE
                    + (1 - MIN_SCALE) * (1 - Math.abs(position));                                   // Scale the page down (between MIN_SCALE and 1)
            view.setScaleX(scaleFactor);
            view.setScaleY(scaleFactor);

        } else {                                                                                    // (1,+Infinity]
            view.setAlpha(0);                                                                       // This page is way off-screen to the right.
        }
    }

}
