package com.fluffr.app.fluffr;

import android.app.Application;
import android.graphics.drawable.Drawable;
import android.util.Log;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.Comparator;

/**
 * Created by Patrick on 10/20/14.
 */
public class Fluff {

    public String id;
    public Drawable image;
    public String title;
    public String subtitle;
    public ParseFile parseFile;
    public boolean favorited = false;
    public String sender;
    public Long sendDate;
    public int position;
    public Integer index;

    public Fluff() {

    }

    public Fluff(ParseObject object) {

        this.title = (String) object.get("title");
        this.subtitle = "subtitle";
        this.id = object.getObjectId();
        this.parseFile = object.getParseFile("image");
        this.index = object.getInt("index");

        if (this.parseFile != null) {
            ImageLoader.getInstance().loadImageSync(this.parseFile.getUrl());
        } else {
            Log.e("Fluff",String.format("Error: no ParseFile found for item with objectId %s", this.id));
        }


    }

    public static Fluff fromString(String fluffId) {

        ParseQuery query = ParseQuery.getQuery("fluff");
        ParseObject object = null;
        try {
            object = query.get(fluffId);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return new Fluff(object);

    }

    public static class DateComparitor  implements Comparator<Fluff> {

        @Override
        public int compare(Fluff lhs, Fluff rhs) {
            return 0;
        }

    }

}
