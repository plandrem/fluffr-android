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

        if (scrollState == SCROLL_STATE_IDLE && parent.downloadsInProgress == 0) {

            //TODO - don't fetch data if all data already pulled

            int maxPosition = view.getCount() - 1;
            Fluff lastFluff = parent.adapter.getItem(maxPosition);

            if (view.getLastVisiblePosition() >= maxPosition - 1 - threshold) {
                // load more items
                String state = parent.getCurrentState().toLowerCase();
                int index = lastFluff.index + 1;

                //TODO -- note, the +1 above will not work for "most liked" or indices that
                // allow degeneracy

                new LoadFluffs(parent, "more_" + state, true, index).execute();

            }

        }
    }

}
