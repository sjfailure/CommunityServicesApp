package io.github.sjfailure.kccommunityconnect;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.applandeo.materialcalendarview.CalendarDay;
import com.applandeo.materialcalendarview.CalendarView;
import com.applandeo.materialcalendarview.exceptions.OutOfDateRangeException;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private JSONObject service_data;
    private CalendarView calendarView;
    private RecyclerView eventRecyclerView;
    private EventAdapter eventAdapter;

    private final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        calendarView = findViewById(R.id.calendarView);
        eventRecyclerView = findViewById(R.id.eventRecyclerView);

        // --- Setup RecyclerView ---
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        eventRecyclerView.setLayoutManager(layoutManager);
        eventAdapter = new EventAdapter(new ArrayList<>()); // Start with an empty list
        eventRecyclerView.setAdapter(eventAdapter);

        // Add dividers between RecyclerView items
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(eventRecyclerView.getContext(),
                layoutManager.getOrientation());
        eventRecyclerView.addItemDecoration(dividerItemDecoration);

        Calendar calendar = Calendar.getInstance();
        calendarView.setMinimumDate(calendar);

        try {
            calendarView.setDate(calendar);
        } catch (OutOfDateRangeException e) {
            throw new RuntimeException(e);
        }

        calendarView.setOnCalendarDayClickListener(calendarDay -> {
            // Normalize today's date to midnight for an accurate comparison
            Calendar today = Calendar.getInstance();
            today.set(Calendar.HOUR_OF_DAY, 0);
            today.set(Calendar.MINUTE, 0);
            today.set(Calendar.SECOND, 0);
            today.set(Calendar.MILLISECOND, 0);

            // Proceed only if the selected date is not in the past
            if (!calendarDay.getCalendar().before(today)) {
                populateRecyclerView(calendarDay);
            }
        });

        FetchData fetchData = new FetchData();
        fetchData.fetch(new FetchData.OnDataReadyCallback() {
            @Override
            public void onDataReady(JSONObject data) {
                service_data = data;
                populateCalendar();
            }

            @Override
            public void onFailure(Exception e) {
                Log.e("MainActivity", "Failed to fetch data", e);
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
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());

        Iterator<String> keys = services.keys();
        while (keys.hasNext()) {
            String key = keys.next();
            JSONObject service = services.getJSONObject(key);

            try {
                String startTimeStr = service.getString("start_time");
                Date eventDate = sdf.parse(startTimeStr);
                Calendar eventCalendar = Calendar.getInstance();
                eventCalendar.setTime(eventDate);

                CalendarDay calendarDay = new CalendarDay(eventCalendar);
                calendarDay.setImageResource(R.mipmap.ic_dot);
                eventDays.add(calendarDay);

            } catch (ParseException e) {
                Log.w("MainActivity", "Skipping service with unparseable date. Key: " + key, e);
            } catch (JSONException e) {
                Log.w("MainActivity", "Skipping service with missing 'start_time'. Key: " + key, e);
            }
        }

        Log.d("MainActivity", "Finished creating event days. Total: " + eventDays.size());
        return eventDays;
    }

    private void populateRecyclerView(CalendarDay date) {
        List<ServiceEvent> eventsForDay = new ArrayList<>();
        Log.d(TAG, "populateRecyclerView: starting to populate for date: " + date.getCalendar().getTime().toString());
        Calendar searchCal = date.getCalendar();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());

        try {
            JSONObject services = service_data.getJSONObject("services");
            Iterator<String> keys = services.keys();
            while (keys.hasNext()) {
                String serviceKey = keys.next();
                JSONObject service = services.getJSONObject(serviceKey);
                Date match_date = sdf.parse(service.getString("start_time"));

                Calendar matchCal = Calendar.getInstance();
                matchCal.setTime(match_date);

                if (searchCal.get(Calendar.YEAR) == matchCal.get(Calendar.YEAR) &&
                    searchCal.get(Calendar.MONTH) == matchCal.get(Calendar.MONTH) &&
                    searchCal.get(Calendar.DAY_OF_MONTH) == matchCal.get(Calendar.DAY_OF_MONTH)) {

                    String provider = service.getString("provider_name");
                    String service_category = service.getString("service_category");
                    String service_type = service.getString("service_type");
                    String start_time = convert24To12(service.getString("start_time"));

                    eventsForDay.add(new ServiceEvent(provider, service_category, service_type, start_time));
                }
            }
        } catch (JSONException | ParseException e) {
            Log.e(TAG, "Error parsing services for display", e);
            Toast.makeText(this, "Error loading event details.", Toast.LENGTH_SHORT).show();
            return;
        }

        eventAdapter.updateEvents(eventsForDay);
        Log.d(TAG, "populateRecyclerView: Adapter updated with " + eventsForDay.size() + " events.");

        // Optionally, show a message if no events are found
        if(eventsForDay.isEmpty()) {
            Toast.makeText(this, "No events scheduled for this day.", Toast.LENGTH_SHORT).show();
        }
    }

    private String convert24To12(String time24) {
        try {
            SimpleDateFormat sdf24 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            SimpleDateFormat sdf12 = new SimpleDateFormat("hh:mm a", Locale.getDefault());
            Date date = sdf24.parse(time24);
            return sdf12.format(date);
        } catch (ParseException e) {
            Log.e(TAG, "Error converting time format", e);
            // Return the original time part if parsing fails
            return time24.substring(11, 16);
        }
    }
}
