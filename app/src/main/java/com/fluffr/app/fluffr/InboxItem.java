package com.fluffr.app.fluffr;

import java.util.Comparator;
import java.util.HashMap;

/**
 * Created by Patrick on 12/11/14.
 */
public class InboxItem {

    public String from;
    public Long date;
    public String fluffId;

    public InboxItem(HashMap<String,Object> hm) {
        this.from = (String) hm.get("from");
        this.date = (Long) hm.get("date");
        this.fluffId = (String) hm.get("fluffId");
    }

    public static class DateComparator implements Comparator<InboxItem> {

        @Override
        public int compare(InboxItem lhs, InboxItem rhs) {
            return (int) (rhs.date - lhs.date);
        }
    }

}
