package io.github.sjfailure.kccommunityconnect;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.Group;
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
import java.util.TimeZone;

public class MainActivity extends AppCompatActivity {

    private JSONObject service_data;
    private CalendarView calendarView;
    private RecyclerView eventRecyclerView;
    private EventAdapter eventAdapter;
    
    // UI State Views
    private ProgressBar loadingProgressBar;
    private Group contentGroup;
    private View errorLayout;
    private Button retryButton;

    private final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize all views
        calendarView = findViewById(R.id.calendarView);
        eventRecyclerView = findViewById(R.id.eventRecyclerView);
        loadingProgressBar = findViewById(R.id.loadingProgressBar);
        contentGroup = findViewById(R.id.contentGroup);
        errorLayout = findViewById(R.id.errorLayout);
        retryButton = findViewById(R.id.retryButton);

        // --- Setup RecyclerView ---
        setupRecyclerView();

        // --- Setup Listeners ---
        retryButton.setOnClickListener(v -> startDataFetch());
        calendarView.setOnCalendarDayClickListener(this::handleCalendarDayClick);
        
        // Initial data fetch
        startDataFetch();
    }

    private void startDataFetch() {
        showLoading();
        FetchData fetchData = new FetchData();
        fetchData.fetch(new FetchData.OnDataReadyCallback() {
            @Override
            public void onDataReady(JSONObject data) {
                service_data = data;
                setupCalendar();
                populateCalendarDecorators();
                showContent();
            }

            @Override
            public void onFailure(Exception e) {
                Log.e(TAG, "Failed to fetch data", e);
                showError();
            }
        });
    }
    
    private void setupRecyclerView() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        eventRecyclerView.setLayoutManager(layoutManager);
        eventAdapter = new EventAdapter(new ArrayList<>());
        eventRecyclerView.setAdapter(eventAdapter);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(eventRecyclerView.getContext(),
                layoutManager.getOrientation());
        eventRecyclerView.addItemDecoration(dividerItemDecoration);
    }
    
    private void setupCalendar() {
        Calendar today = Calendar.getInstance();
        calendarView.setMinimumDate(today);
        try {
            calendarView.setDate(today);
        } catch (OutOfDateRangeException e) {
            Log.e(TAG, "Error setting calendar date", e);
        }
    }

    private void populateCalendarDecorators() {
        if (service_data == null) {
            Log.e(TAG, "populateCalendar called but service_data is null.");
            return;
        }
        try {
            ArrayList<CalendarDay> eventDays = createEventDaysFromJSON(service_data);
            calendarView.setCalendarDays(eventDays);
        } catch (JSONException e) {
            Log.e(TAG, "Failed to parse JSON and create event days.", e);
        }
    }

    private ArrayList<CalendarDay> createEventDaysFromJSON(JSONObject serviceData) throws JSONException {
        ArrayList<CalendarDay> eventDays = new ArrayList<>();
        JSONObject services = serviceData.getJSONObject("services");
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());

        Iterator<String> keys = services.keys();
        while (keys.hasNext()) {
            String key = keys.next();
            try {
                JSONObject service = services.getJSONObject(key);
                String startTimeStr = service.getString("start_time");
                Date eventDate = sdf.parse(startTimeStr);
                Calendar eventCalendar = Calendar.getInstance();
                eventCalendar.setTime(eventDate);
                CalendarDay calendarDay = new CalendarDay(eventCalendar);
                calendarDay.setImageResource(R.mipmap.ic_dot);
                eventDays.add(calendarDay);
            } catch (ParseException | JSONException e) {
                Log.w(TAG, "Skipping service with malformed data. Key: " + key, e);
            }
        }
        return eventDays;
    }
    
    private void handleCalendarDayClick(CalendarDay calendarDay) {
        Calendar today = Calendar.getInstance();
        today.set(Calendar.HOUR_OF_DAY, 0);
        today.set(Calendar.MINUTE, 0);
        today.set(Calendar.SECOND, 0);
        today.set(Calendar.MILLISECOND, 0);

        if (!calendarDay.getCalendar().before(today)) {
            populateRecyclerView(calendarDay);
        }
    }

    private void populateRecyclerView(CalendarDay date) {
        if (service_data == null) return;
        List<ServiceEvent> eventsForDay = new ArrayList<>();
        Calendar searchCal = date.getCalendar();
        SimpleDateFormat sdf = new SimpleDateFormat(
                "yyyy-MM-dd HH:mm:ss",
                Locale.getDefault());

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
                    eventsForDay.add(
                            new ServiceEvent(
                                    serviceKey,
                                    provider,
                                    service_category,
                                    service_type,
                                    start_time
                            )
                    );
                }
            }
        } catch (JSONException | ParseException e) {
            Log.e(TAG, "Error parsing services for display", e);
            Toast.makeText(
                    this,
                    "Error loading event details.",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        eventAdapter.updateEvents(eventsForDay);
        if(eventsForDay.isEmpty()) {
            Toast.makeText(
                    this,
                    "No events scheduled for this day.",
                    Toast.LENGTH_SHORT).show();
        }
    }

    private String convert24To12(String time24) {
        try {
            SimpleDateFormat sdf24 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
            sdf24.setTimeZone(TimeZone.getTimeZone("UTC"));
            SimpleDateFormat sdf12 = new SimpleDateFormat("hh:mm a", Locale.US);
            Date date = sdf24.parse(time24);
            if (date == null) {
                return "";
            }
            return sdf12.format(date);
        } catch (ParseException e) {
            return time24.substring(11, 16);
        }
    }
    
    // --- UI State Management Methods ---
    private void showLoading() {
        contentGroup.setVisibility(View.GONE);
        errorLayout.setVisibility(View.GONE);
        loadingProgressBar.setVisibility(View.VISIBLE);
    }

    private void showContent() {
        loadingProgressBar.setVisibility(View.GONE);
        errorLayout.setVisibility(View.GONE);
        contentGroup.setVisibility(View.VISIBLE);
    }

    private void showError() {
        loadingProgressBar.setVisibility(View.GONE);
        contentGroup.setVisibility(View.GONE);
        errorLayout.setVisibility(View.VISIBLE);
    }
}
