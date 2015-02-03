package com.bryankrosenbaum.journaldb.app;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.bryankrosenbaum.journaldb.app.adapter.JournalAdapter;
import com.bryankrosenbaum.journaldb.app.models.EntryDate;
import com.bryankrosenbaum.journaldb.app.models.Journal;
import com.bryankrosenbaum.journaldb.app.models.RequestWrapper;
import com.bryankrosenbaum.journaldb.app.rest.JournalRequestInterceptor;
import com.bryankrosenbaum.journaldb.app.rest.JournalService;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;
import retrofit.converter.GsonConverter;


public class CreateEntryActivity extends Activity {

    private JournalService journalService;

    private Calendar selectedDate;
    private SharedPreferences sharedPref;

    private TextView textviewEntryDate;
    private EditText edittextEntry;
    private Button btnSubmitNewEntry;

    private SimpleDateFormat dateFormat;
    private String hostUrl;

    private boolean isUpdate;

    private static final String TAG = CreateEntryActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_entry);

        dateFormat = new SimpleDateFormat("yyyy-MM-dd");

        textviewEntryDate = (TextView) findViewById(R.id.textviewEntryDate);
        edittextEntry = (EditText) findViewById(R.id.edittextEntry);
        btnSubmitNewEntry = (Button) findViewById(R.id.btnSubmitNewEntry);

        selectedDate = Calendar.getInstance();
        selectedDate.setTimeInMillis(getIntent().getLongExtra("SELECTED_DATE", 0));

        sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        hostUrl = sharedPref.getString(getString(R.string.pref_name_host_url), "");

        setupJournalService();

        setHeaderDate();
        getEntryForDay();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_create_entry, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void setupJournalService() {
        Gson gsonConv = new GsonBuilder()
                .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
                .create();

        RestAdapter restAdapter = new RestAdapter.Builder()
                .setEndpoint(hostUrl)
                .setRequestInterceptor(new JournalRequestInterceptor())
                .setConverter(new GsonConverter(gsonConv))
                .build();

        journalService = restAdapter.create(JournalService.class);
    }

    private void setHeaderDate() {
        Log.d(TAG, "opening CreateEntryActivity for selectedDate: " + selectedDate.getTime().toString());

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        long todayTime = calendar.getTimeInMillis();
        calendar.add(Calendar.DATE, -1);
        long yesterdayTime = calendar.getTimeInMillis();

        long selectedDateTime = selectedDate.getTimeInMillis();
        if (selectedDateTime == todayTime) {
            textviewEntryDate.setText(dateFormat.format(selectedDate.getTime()) + getString(R.string.date_suffix_today));
        }
        else if (selectedDateTime == yesterdayTime) {
            textviewEntryDate.setText(dateFormat.format(selectedDate.getTime()) + getString(R.string.date_suffix_yesterday));
        }
        else {
            textviewEntryDate.setText(dateFormat.format(selectedDate.getTime()));
        }
    }

    private void getEntryForDay() {
        journalService.getEntriesForDate(selectedDate.get(Calendar.YEAR), selectedDate.get(Calendar.MONTH), selectedDate.get(Calendar.DAY_OF_MONTH), new Callback<Journal>() {
            @Override
            public void success(Journal journalEntry, Response response) {
                Log.i(TAG, "successfully retrieved entry for day");
                if (journalEntry != null && journalEntry.getEntryText().length() > 0) {
                    edittextEntry.setText(journalEntry.getEntryText());
                    edittextEntry.setSelection(edittextEntry.getText().length());
                    isUpdate = true;
                    btnSubmitNewEntry.setText(R.string.btn_update_entry);
                }
            }

            @Override
            public void failure(RetrofitError retrofitError) {
                Log.e(TAG, retrofitError.getCause().toString());
                Toast.makeText(CreateEntryActivity.this, "Error retrieving entry for day", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void createNewEntryClick(View view) {
        if (edittextEntry.getText().length() > 0) {
            EntryDate entryDate = new EntryDate(selectedDate.get(Calendar.YEAR), selectedDate.get(Calendar.MONTH), selectedDate.get(Calendar.DAY_OF_MONTH));
            String entryText = edittextEntry.getText().toString();

            if (isUpdate) {
                RequestWrapper rw = new RequestWrapper(entryDate, entryText);
                journalService.updateEntry(rw, new Callback<Journal>() {
                    @Override
                    public void success(Journal journal, Response response) {
                        finish();
                        Toast.makeText(CreateEntryActivity.this, "Successfully updated entry", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        Log.e(TAG, "error updating entry: " + error.getMessage());
                        Toast.makeText(CreateEntryActivity.this, "Error updating entry", Toast.LENGTH_SHORT).show();
                    }
                });
            }
            else {
                RequestWrapper rw = new RequestWrapper(entryDate, entryText);
                journalService.createEntry(rw, new Callback<Journal>() {
                    @Override
                    public void success(Journal journal, Response response) {
                        finish();
                        Toast.makeText(CreateEntryActivity.this, "Successfully created entry", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        Log.e(TAG, "error creating entry: " + error.getMessage());
                        Toast.makeText(CreateEntryActivity.this, "Error creating entry", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }
        else {
            Toast.makeText(CreateEntryActivity.this, "Textbox is empty", Toast.LENGTH_SHORT).show();
        }
    }
}
