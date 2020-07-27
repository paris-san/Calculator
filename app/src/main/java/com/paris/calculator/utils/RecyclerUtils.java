package com.paris.calculator.utils;

import android.app.Activity;
import android.graphics.Point;
import android.view.Display;
import android.view.WindowManager;

public class RecyclerUtils {

    /**
     * Find the width of the screen (depending the orientation)
     * and calculate the appropriate column number for the GridLayout.
     */
    public static int getGridColumnsNumber(Activity activity) {

        WindowManager windowManager = activity.getWindowManager();
        if (windowManager == null) {
            return 2;
        }
        Display display = windowManager.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int gridColumns = size.x / 200;
        if (gridColumns < 2) {
            gridColumns = 2;
        }
        return gridColumns;
    }


}
