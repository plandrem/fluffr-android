package com.fluffr.app.fluffr;

import android.util.Log;
import android.widget.AbsListView;

/**
 * Created by Patrick on 11/24/14.
 */
public class FluffScrollListener implements AbsListView.OnScrollListener {
    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        Log.d("FluffScrollListener","onScroll");
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        Log.d("FluffScrollListener","onScrollStateChanged");
    }

}
