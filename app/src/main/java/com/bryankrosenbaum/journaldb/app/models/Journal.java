package com.bryankrosenbaum.journaldb.app.models;

import java.io.Serializable;
import java.util.Date;

public class Journal implements Serializable {

    private String _id;
    private String entryText;
    private EntryDate entryDate;
    private Date createdDate;
    private Date lastUpdateDate;

    public String get_id() { return this._id; }
    public void set_id(String _id) { this._id = _id; }

    public String getEntryText() { return this.entryText; }
    public void setEntryText(String _entryText) { this.entryText = _entryText; }

    public EntryDate getEntryDate() { return this.entryDate; }
    public void setEntryDate(EntryDate _entryDate) { this.entryDate = _entryDate; }

    public Date getCreatedDate() { return this.createdDate; }

    public Date getLastUpdateDate() { return this.lastUpdateDate; }
}