package com.bryankrosenbaum.journaldb.app.models;

import java.io.Serializable;

public class EntryDate implements Serializable {

    private int year;
    private int month;
    private int dayOfMonth;

    public String getYear() { return this.year + ""; }
    public void setYear(int _year) { this.year = _year; }

    public int getMonth() { return this.month; }
    public void setMonth(int _month) { this.month = _month; }

    public int getDayOfMonth() { return this.dayOfMonth; }
    public void setDayOfMonth(int _dayOfMonth) { this.dayOfMonth = _dayOfMonth; }

    public EntryDate(int _year, int _month, int _dayOfMonth) {
        this.year = _year;
        this.month = _month;
        this.dayOfMonth = _dayOfMonth;
    }

    public void setEntryDate(int _year, int _month, int _dayOfMonth) {
        this.year = _year;
        this.month = _month;
        this.dayOfMonth = _dayOfMonth;
    }

    /*@Override public String toString() {
        return this.year + "-" + this.month + "-" + this.dayOfMonth;
    }*/
}
