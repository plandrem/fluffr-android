package com.fluffr.app.fluffr;

import android.app.Application;
import android.graphics.drawable.Drawable;

import com.parse.ParseFile;

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

}