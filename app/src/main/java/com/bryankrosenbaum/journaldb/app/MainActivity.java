package com.bryankrosenbaum.journaldb.app;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bryankrosenbaum.journaldb.app.adapter.JournalAdapter;
import com.bryankrosenbaum.journaldb.app.models.Journal;
import com.bryankrosenbaum.journaldb.app.rest.JournalRequestInterceptor;
import com.bryankrosenbaum.journaldb.app.rest.JournalService;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;
import retrofit.converter.GsonConverter;


public class MainActivity extends Activity {

    private JournalService journalService;
    private int prefHoursBack = 0;
    private Calendar selectedDate = null;

    private TextView textviewDate;
    private ListView listviewPreviousEntries;
    private SharedPreferences sharedPref;
    private ProgressBar progressbarSpinner;

    private SimpleDateFormat dateFormat;
    private String hostUrl;
    private String authUsername;
    private String authPassword;

    private static final String TAG = MainActivity.class.getSimpleName();
    Context context = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        listviewPreviousEntries = (ListView) findViewById(R.id.listviewPreviousEntries);
        textviewDate = (TextView) findViewById(R.id.textviewDate);

        dateFormat = new SimpleDateFormat("yyyy-MM-dd");

        sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            startActivity(new Intent(MainActivity.this, SettingsActivity.class));
            return true;
        }
        else if (id == R.id.action_newentry) {
            Intent intent = new Intent(getBaseContext(), CreateEntryActivity.class);
            intent.putExtra("SELECTED_DATE", selectedDate.getTimeInMillis());
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onResume() {
        super.onResume();

        String hostUrlPref = sharedPref.getString(getString(R.string.pref_name_host_url), "");

        String authUsernamePref = sharedPref.getString(getString(R.string.pref_name_auth_username), "");
        String authPasswordPref = sharedPref.getString(getString(R.string.pref_name_auth_password), "");

        // if host url isn't set, navigate directly to the settings screen
        if (hostUrlPref.length() < 1 || authUsernamePref.length() < 1 || authPasswordPref.length() < 1) {
            if (hostUrlPref.length() < 1) {
                Toast.makeText(MainActivity.this, getString(R.string.warning_no_url_set), Toast.LENGTH_SHORT).show();
            }
            else if (authUsernamePref.length() < 1 || authPasswordPref.length() < 1) {
                Toast.makeText(MainActivity.this, getString(R.string.warning_no_auth_set), Toast.LENGTH_SHORT).show();
            }
            startActivity(new Intent(MainActivity.this, SettingsActivity.class));
            return;
        }
        else {
            if (hostUrlPref != hostUrl || authUsernamePref != authUsername || authPasswordPref != authPassword) {
                hostUrl = hostUrlPref;
                authUsername = authUsernamePref;
                authPassword = authPasswordPref;
                setupJournalService();
            }

            String hoursBackString = sharedPref.getString(getString(R.string.pref_name_hours_back), "");
            if (hoursBackString != "") {
                prefHoursBack = Integer.parseInt(hoursBackString);
            }
            else {
                prefHoursBack = 0;
            }
            Log.i(TAG, "updating prefHoursBack to: " + prefHoursBack);

            setJournalDate();
            refreshEntries();
        }
    }

    private void setupJournalService() {
        Gson gsonConv = new GsonBuilder()
                .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
                .create();

        JournalRequestInterceptor journalRequestInterceptor = new JournalRequestInterceptor();
        journalRequestInterceptor.setEncodedCredentials(authUsername, authPassword);

        RestAdapter restAdapter = new RestAdapter.Builder()
                .setEndpoint(hostUrl)
                .setRequestInterceptor(journalRequestInterceptor)
                .setConverter(new GsonConverter(gsonConv))
                .build();

        journalService = restAdapter.create(JournalService.class);
    }

    public void setJournalDate() {

        Calendar calendar = Calendar.getInstance();

        if (prefHoursBack > calendar.get(Calendar.HOUR_OF_DAY)) {
            Log.d(TAG, "hours back (" + prefHoursBack + ") is less than current hour (" + calendar.get(Calendar.HOUR_OF_DAY) + "), so default to previous day");
            calendar.set(Calendar.DATE, calendar.get(Calendar.DATE) - 1);
        }

        Log.i(TAG, "orig calendar date: " + ((Date) (calendar.getTime())).toString());

        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        selectedDate = calendar;
        Log.i(TAG, "setting selectedDate to: " + selectedDate.getTime().toString());

        setHeaderDate();
    }

    public void refreshEntries() {

        Log.i(TAG, "refreshing entries for month=" + selectedDate.get(Calendar.MONTH) + ", dayOfMonth=" + selectedDate.get(Calendar.DAY_OF_MONTH));

        journalService.getEntriesForDay(selectedDate.get(Calendar.MONTH), selectedDate.get(Calendar.DAY_OF_MONTH), new Callback<List<Journal>>() {
            @Override
            public void success(List<Journal> journalEntries, Response response) {
                Log.i(TAG, "successfully retrieved records");
                ArrayList<Journal> entriesArrayList = new ArrayList<Journal>(journalEntries);
                JournalAdapter journalAdapter = new JournalAdapter(context, R.layout.listitem_entry, entriesArrayList);
                listviewPreviousEntries.setAdapter(journalAdapter);
            }

            @Override
            public void failure(RetrofitError retrofitError) {
                try {
                    Log.e(TAG, retrofitError.toString());
                }
                catch (Exception e) {
                    Log.e(TAG, "exception caught in error: " + e.getMessage());
                }
                Toast.makeText(MainActivity.this, "Error retrieving entries - check connection and host url in settings", Toast.LENGTH_SHORT).show();
            }
        });

    }

    public void changeDateClick(View view) {
        if (view instanceof ImageView) {
            ImageView buttonClicked = (ImageView) view;
            if (buttonClicked.getId() == R.id.imageBtnPreviousDate) {
                selectedDate.add(Calendar.DATE, -1);
                setHeaderDate();
                refreshEntries();
            }
            else if (buttonClicked.getId() == R.id.imageBtnNextDate) {
                selectedDate.add(Calendar.DATE, 1);
                setHeaderDate();
                refreshEntries();
            }
            else {
                Log.w(TAG, "onclick for view that is not previous or next button");
            }
        }
        else {
            Log.w(TAG, "onclick for view that is not the ImageView");
        }
    }

    private void setHeaderDate() {
        Log.d(TAG, "opening CreateEntryActivity for selectedDate: " + selectedDate.getTime().toString());

        // calculate todayTime and yesterdayTime
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
            textviewDate.setTextColor(Color.rgb(64, 64, 216));
            textviewDate.setText(dateFormat.format(selectedDate.getTime()));
        }
        else if (selectedDateTime == yesterdayTime) {
            textviewDate.setTextColor(Color.rgb(121, 52, 213));
            textviewDate.setText(dateFormat.format(selectedDate.getTime()));
        }
        else {
            textviewDate.setTextColor(Color.BLACK);
            textviewDate.setText(dateFormat.format(selectedDate.getTime()));
        }
    }

    DatePickerDialog.OnDateSetListener onDateSetListener = new DatePickerDialog.OnDateSetListener() {

        @Override
        public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {

            Calendar calendar = Calendar.getInstance();

            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.MONTH, month);
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);

            selectedDate = calendar;

            setHeaderDate();
            refreshEntries();
        }

    };

    public void dateLabelClick(View view) {
        new DatePickerDialog(this, onDateSetListener, selectedDate.get(Calendar.YEAR)
                , selectedDate.get(Calendar.MONTH), selectedDate.get(Calendar.DAY_OF_MONTH)).show();
    }
}
