package com.udacity.stockhawk.data;

/**
 * Created by krypt on 11/04/2017.
 */

public class EntryGraph {

    private String mDate;
    private float mPrice;

    public EntryGraph (String date, float price){

        this.mDate = date;

        this.mPrice = price;

    }

    public String getmDate() {
        return mDate;
    }

    public float getmPrice() {
        return mPrice;
    }
}
