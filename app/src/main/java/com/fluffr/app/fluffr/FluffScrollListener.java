package com.fluffr.app.fluffr;

import android.util.Log;
import android.widget.AbsListView;

/**
 * Created by Patrick on 11/24/14.
 */
public class FluffScrollListener implements AbsListView.OnScrollListener {

    public static int threshold = 10;
    private BrowserActivity parent;

    public FluffScrollListener(BrowserActivity browserActivity) {
        this.parent = browserActivity;
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
//        Log.d("FluffScrollListener","onScroll");
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
//        Log.d("FluffScrollListener","onScrollStateChanged");
        if (scrollState == SCROLL_STATE_IDLE && parent.downloadsInProgress == 0) {

            //TODO - don't fetch data if all data already pulled

            if (view.getLastVisiblePosition() >= view.getCount() - 1 - threshold) {
                // load more items
                String state = parent.getCurrentState().toLowerCase();
                int index = parent.getCurrentBrowseIndex();

                new LoadFluffs(parent, "more_" + state, index).execute();

            }

        }
    }

}
