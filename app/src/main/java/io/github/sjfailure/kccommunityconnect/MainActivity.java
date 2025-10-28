package io.github.sjfailure.kccommunityconnect;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.applandeo.materialcalendarview.CalendarView;
import com.applandeo.materialcalendarview.CalendarDay;
//import com.applandeo.materialcalendarview.DayViewDecorator;
//import com.applandeo.materialcalendarview.DayViewFacade;
//import com.applandeo.materialcalendarview.MaterialCalendarView;
import com.applandeo.materialcalendarview.exceptions.OutOfDateRangeException;
//import com.applandeo.materialcalendarview.spans.DotSpan;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;

public class MainActivity extends AppCompatActivity {

    private JSONObject service_data;
    private CalendarView calendarView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        calendarView = findViewById(R.id.calendarView);
        Calendar calendar = Calendar.getInstance();
        try {
            calendarView.setDate(calendar);
        } catch (OutOfDateRangeException e) {
            throw new RuntimeException(e);
        }
        // Corrected code
        calendarView.setOnCalendarDayClickListener(new com.applandeo.materialcalendarview.listeners.OnCalendarDayClickListener() {
            @Override
            public void onClick(@NonNull CalendarDay calendarDay) {

            }

            public void onCalendarDayClick(com.applandeo.materialcalendarview.EventDay eventDay) {
                // The calendarDay object contains the date and any events associated with it.
                // For now, we'll just show the date in a Toast message.
                Calendar clickedDayCalendar = eventDay.getCalendar();

                // Format the date for better display in the Toast
                java.text.SimpleDateFormat toastDateFormat = new java.text.SimpleDateFormat("EEE, MMM dd, yyyy", java.util.Locale.getDefault());
                String formattedDate = toastDateFormat.format(clickedDayCalendar.getTime());

                Toast.makeText(MainActivity.this, formattedDate, Toast.LENGTH_SHORT).show();

                // --- This is where you will add logic to update the RecyclerView ---
                // For example: updateRecyclerViewForDate(clickedDayCalendar);
            }
        });


        RecyclerView recyclerView = findViewById(R.id.recyclerView);

        FetchData fetchData = new FetchData();
        fetchData.fetch(new FetchData.OnDataReadyCallback() {
            @Override
            public void onDataReady(JSONObject data) {
                service_data = data;
                populateCalendar();
            }

            @Override
            public void onFailure(Exception e) {
                // It's better to log the error than to crash the app
                Log.e("MainActivity", "Failed to fetch data", e);
                // Optionally, show a message to the user
                 Toast.makeText(MainActivity.this, "Failed to fetch data", Toast.LENGTH_SHORT).show();

            }
        });

    }

    private void populateCalendar() {
        if (service_data == null) {
            Log.e("MainActivity", "populateCalendar called but service_data is null.");
            return;
        }

        try {
            // Directly call the new, efficient method
            ArrayList<CalendarDay> eventDays = createEventDaysFromJSON(service_data);

            Log.d("MainActivity", "Populating calendar with " + eventDays.size() + " event days.");
            calendarView.setCalendarDays(eventDays);

        } catch (JSONException e) {
            Log.e("MainActivity", "Failed to parse JSON and create event days.", e);
        }
    }



    private ArrayList<CalendarDay> createEventDaysFromJSON(JSONObject serviceData) throws JSONException {
        ArrayList<CalendarDay> eventDays = new ArrayList<>();
        JSONObject services = serviceData.getJSONObject("services");

        // A single SimpleDateFormat instance is more efficient than manual string splitting
        // NOTE: Ensure your API always provides this exact format.
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault());

        Iterator<String> keys = services.keys();
        while (keys.hasNext()) {
            String key = keys.next();
            JSONObject service = services.getJSONObject(key);

            try {
                // Directly parse the date string from the JSON
                String startTimeStr = service.getString("start_time");

                // Parse the string into a Date object
                java.util.Date eventDate = sdf.parse(startTimeStr);

                // Create a Calendar instance from the Date
                Calendar eventCalendar = Calendar.getInstance();
                eventCalendar.setTime(eventDate);

                // Create the customized CalendarDay object
                CalendarDay calendarDay = new CalendarDay(eventCalendar);
                calendarDay.setImageResource(R.mipmap.ic_dot);
                // calendarDay.setLabelColor(Color.BLUE); // You can customize further

                // Add the final, styled object to the list
                eventDays.add(calendarDay);

            } catch (java.text.ParseException e) {
                // Log if a single date is malformed, but don't crash the whole app
                Log.w("MainActivity", "Skipping service with unparseable date. Key: " + key, e);
            } catch (JSONException e) {
                // Log if a service object is missing the "start_time" key
                Log.w("MainActivity", "Skipping service with missing 'start_time'. Key: " + key, e);
            }
        }

        Log.d("MainActivity", "Finished creating event days. Total: " + eventDays.size());
        return eventDays;
    }

    // The timeStringParser method is no longer needed.
    // private JSONObject timeStringParser(...) { ... }
}

