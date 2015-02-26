package com.bryankrosenbaum.journaldb.app.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.bryankrosenbaum.journaldb.app.R;
import com.bryankrosenbaum.journaldb.app.models.Journal;

import java.util.ArrayList;
import java.util.List;

public class JournalAdapter extends ArrayAdapter<Journal> {

    private ArrayList<Journal> entries;

    private static final String TAG = JournalAdapter.class.getSimpleName();

    public JournalAdapter(Context context, int resource, ArrayList<Journal> entries) {
        super(context, resource, entries);
        this.entries = entries;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        // take data from cursor and put it in the view

        if (view == null) {
            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.listitem_entry, null);
        }

        Journal entry = entries.get(position);

        TextView textviewYear = (TextView) view.findViewById(R.id.textviewYear);
        TextView textviewEntryText = (TextView) view.findViewById(R.id.textviewEntryText);

        textviewYear.setText(entry.getEntryDate().getYear());
        textviewEntryText.setText(entry.getEntryText());

        return view;
    }

    @Override
    public boolean isEnabled(int position) {
        return false;
    }
}
