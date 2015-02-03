package com.bryankrosenbaum.journaldb.app.models;

public class RequestWrapper {
    public EntryDate entryDay;
    public String entryText;

    public RequestWrapper(EntryDate _entryDate, String _entryText) {
        this.entryDay = _entryDate;
        this.entryText = _entryText;
    }
}
