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

            //debugging
            String str = "";

            int maxPosition = view.getCount() - 1;
            Fluff lastFluff = parent.adapter.getItem(maxPosition);
            Fluff firstFluff = parent.adapter.getItem(0);

            str += String.format("maxposition: %d, ", maxPosition);
            str += String.format("first fluff index: %d, ", firstFluff.index);
            str += String.format("last fluff index: %d, ", lastFluff.index);
            str += String.format("first visible position: %d, ", view.getFirstVisiblePosition());
            str += String.format("last visible position: %d, ", view.getLastVisiblePosition());

            // approaching top of list
            if (view.getFirstVisiblePosition() <= threshold) {
                // load more items
                String state = parent.getCurrentState().toLowerCase();
                if (!state.equals("browse")) return;

                int index = firstFluff.index;

                //TODO -- note, the above will not work for "most liked" or indices that
                // allow degeneracy

                parent.getFluffsTask = new LoadFluffs(parent, "more_" + state + "_up", true, index);
                parent.getFluffsTask.execute();

            } else if (view.getLastVisiblePosition() >= maxPosition - threshold) {
                // end of list
                // load more items
                String state = parent.getCurrentState().toLowerCase();
                int index = lastFluff.index + 1;

                //TODO -- note, the +1 above will not work for "most liked" or indices that
                // allow degeneracy

                if (state.equals("inbox")) {
                    index = parent.inbox.size();
                    parent.getInboxTask = new LoadInbox(parent, "more_inbox", index);
                    parent.getInboxTask.execute();
                } else {
                    parent.getFluffsTask = new LoadFluffs(parent, "more_" + state, true, index);
                    parent.getFluffsTask.execute();
                }

            }

            Log.d("fluffscrollListener",str);
        }
    }

}
