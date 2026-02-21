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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

// TODO Add Room Database for persistence
// TODO Add Service Category Selector and logic
// TODO add Audience Selector and logic
// TODO Add Alerts button and functionality

public class MainActivity extends AppCompatActivity {

    private ArrayList<ServiceEvent> serviceData;
    private JSONObject category_type_and_audience_data;

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
                serviceData = parseAllEvents(data.optJSONObject("services"));
                category_type_and_audience_data = prepareCategoryTypeAndAudienceData(data);
                // Pass the global data to the adapter after it's fetched
                if (eventAdapter != null) {
                    eventAdapter.setCategoryTypeAndAudienceData(category_type_and_audience_data);
                }
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

    private ArrayList<ServiceEvent> parseAllEvents(JSONObject dataFromApi) {
        ArrayList<ServiceEvent> events = new ArrayList<>();
        if (dataFromApi == null) return events;

        Iterator<String> keys = dataFromApi.keys();
        Log.d(TAG, "parseAllEvents: dataFromApi: " + dataFromApi);
        while (keys.hasNext()) {
            String key = keys.next();
            try {
                JSONObject serviceEventData = dataFromApi.getJSONObject(key);
                events.add(new ServiceEvent(key,
                        serviceEventData.getString("provider_name"),
                        Utilities.convertJSONArraytoStringList(
                                serviceEventData.getJSONArray("service_category")),
                        Utilities.convertJSONArraytoStringList(
                                serviceEventData.getJSONArray("service_type")),
                        serviceEventData.getString("start_time"),
                        serviceEventData.getString("end_time"),
                        serviceEventData.getString("address"),
                        serviceEventData.getString("phone"),
                        serviceEventData.getString("email"),
                        Utilities.convertJSONArraytoStringList(
                                serviceEventData.getJSONArray("audience")),
                        serviceEventData.getString("notes")));
            } catch (JSONException e) {
                Log.e(TAG, "Failed to parse JSON for key: " + key, e);
            }
        }
        return events;
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
        if (serviceData == null) {
            Log.e(TAG, "populateCalendar called but service_data is null.");
            return;
        }
        ArrayList<CalendarDay> eventDays = createEventDaysFromServiceEventList(serviceData);
        calendarView.setCalendarDays(eventDays);
    }

    private ArrayList<CalendarDay> createEventDaysFromServiceEventList(List<ServiceEvent> serviceEvents) {
        ArrayList<CalendarDay> eventDays = new ArrayList<>();
        for (ServiceEvent event : serviceEvents) {
            CalendarDay day = Utilities.convertDateStringtoCalendarDay(event.getIsoLikeStartTime());
            if (day != null && !eventDays.contains(day)) {
                day.setImageResource(R.mipmap.ic_dot);
                eventDays.add(day);
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
        if (serviceData == null) return;
        
        List<ServiceEvent> eventsForDay = new ArrayList<>();
        SimpleDateFormat searchSdf = new SimpleDateFormat("MM-dd-yyyy", Locale.US);
        String searchDate = searchSdf.format(date.getCalendar().getTime());

        for (ServiceEvent event : serviceData) {
            if (event.getDate().equals(searchDate)) {
                eventsForDay.add(event);
            }
        }

        // Sort the list: primarily by time, secondarily by provider name
        eventsForDay.sort(Comparator.comparing(ServiceEvent::getIsoLikeStartTime)
                .thenComparing(ServiceEvent::getProviderName));

        eventAdapter.updateEvents(eventsForDay);
        if(eventsForDay.isEmpty()) {
            Toast.makeText(
                    this,
                    "No events scheduled for this day.",
                    Toast.LENGTH_SHORT).show();
        }
    }

    private JSONObject prepareCategoryTypeAndAudienceData(JSONObject api_data) {
        JSONObject data = new JSONObject();
        try {
            // Assuming the API returns these keys at the top level, or nested within 'data'
            // Adjust keys like 'audiences' and 'categories/types' based on your actual API response structure
            if (api_data.has("audiences")) {
                data.put("audiences", api_data.getJSONArray("audiences"));
            }
            if (api_data.has("categories/types")) {
                data.put("categories/types", api_data.getJSONObject("categories/types"));
            }
        } catch (JSONException e) {
            Log.e(TAG, "Error preparing category/type/audience data", e);
            // Return an empty object or handle more gracefully if this data is critical
        }
        return data;
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
