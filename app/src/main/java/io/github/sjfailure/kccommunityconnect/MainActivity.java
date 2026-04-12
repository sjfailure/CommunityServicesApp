package io.github.sjfailure.kccommunityconnect;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.Group;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.applandeo.materialcalendarview.CalendarDay;
import com.applandeo.materialcalendarview.CalendarView;
import com.applandeo.materialcalendarview.exceptions.OutOfDateRangeException;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.navigation.NavigationView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    // Data Sets
    private ArrayList<ServiceEvent> allEvents = new ArrayList<>(); 
    private ArrayList<ServiceEvent> filteredEvents = new ArrayList<>(); 
    
    // Filter State
    private String selectedCategory = null; 
    private String selectedType = null;
    private String selectedAudience = null;
    private CalendarDay selectedCalendarDay = null;

    private JSONObject category_type_and_audience_data;
    private String announcementMessage = "";

    private CalendarView calendarView;
    private RecyclerView eventRecyclerView;
    private EventAdapter eventAdapter;

    // UI State Views
    private ProgressBar loadingProgressBar;
    private Group contentGroup;
    private View errorLayout;
    private Button retryButton;
    private Button serviceSearchButton;
    private Button audienceSearchButton;
    private TextView serviceText;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private MaterialToolbar toolbar;
    private String feedbackKey;

    private final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize views
        calendarView = findViewById(R.id.calendarView);
        eventRecyclerView = findViewById(R.id.eventRecyclerView);
        serviceSearchButton = findViewById(R.id.service_menu_button);
        audienceSearchButton = findViewById(R.id.audience_menu_button);
        loadingProgressBar = findViewById(R.id.loadingProgressBar);
        contentGroup = findViewById(R.id.contentGroup);
        errorLayout = findViewById(R.id.errorLayout);
        retryButton = findViewById(R.id.retryButton);
        serviceText = findViewById(R.id.serviceText);
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        toolbar = findViewById(R.id.toolbar);

        setupRecyclerView();
        toolbar.inflateMenu(R.menu.toolbar_menu);

        // --- Setup Listeners ---
        retryButton.setOnClickListener(v -> startDataFetch());
        calendarView.setOnCalendarDayClickListener(this::handleCalendarDayClick);
        
        audienceSearchButton.setOnClickListener(v -> showAudienceMenu());
        serviceSearchButton.setOnClickListener(v -> showServiceMenu());

        toolbar.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.action_drawer) {
                drawerLayout.openDrawer(GravityCompat.END);
                return true;
            }
            return false;
        });
        
        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_announcements) {
                if (announcementMessage != null && !announcementMessage.isEmpty()) {
                    showAnnouncementDialog();
                } else {
                    Toast.makeText(this, "No current announcements.", Toast.LENGTH_SHORT).show();
                }
            } else if (id == R.id.nav_providers) {
                startActivity(new Intent(this, ProviderContactList.class));
                drawerLayout.closeDrawer(GravityCompat.END);
            } else if (id == R.id.nav_report_bug) {
                showFeedbackDialog();
                drawerLayout.closeDrawer(GravityCompat.END);
            }
            return true;
        });

        startDataFetch();
    }

    private void showFeedbackDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_feedback, null);
        EditText feedbackEditText = dialogView.findViewById(R.id.feedbackEditText);
        CheckBox includeDeviceInfoCheckbox = dialogView.findViewById(R.id.includeDeviceInfoCheckbox);
        Button submitButton = dialogView.findViewById(R.id.submitFeedbackButton);
        CheckBox showSubmissionInfoCheckbox = dialogView.findViewById(R.id.showSubmissionInfoCheckbox);
        TextView textView = dialogView.findViewById(R.id.submissionInfoTextView);

        textView.setVisibility(View.GONE);

        AlertDialog dialog = new MaterialAlertDialogBuilder(this)
                .setView(dialogView)
                .create();

        Runnable updateSubmissionInfo = () -> {
            if (showSubmissionInfoCheckbox.isChecked()) {
                String message = feedbackEditText.getText().toString().trim();
                Map<String, String> payload = new HashMap<>();
                payload.put("message", message);
                payload.put("user_key", feedbackKey != null ? feedbackKey : "");
                if (includeDeviceInfoCheckbox.isChecked()) {
                    payload.put("os_data", "Android " + Build.VERSION.RELEASE + " (API " + Build.VERSION.SDK_INT + ")");
                    payload.put("device_data", Build.MANUFACTURER + " " + Build.MODEL);
                }
                StringBuilder sb = new StringBuilder();
                sb.append("Message: ").append(payload.get("message")).append("\n");
                sb.append("Device Info: ").append(payload.get("device_data") != null ? payload.get("device_data") : "").append("\n");
                sb.append("OS Data: ").append(payload.get("os_data") != null ? payload.get("os_data") : "").append("\n");
                textView.setText(sb.toString());
                textView.setVisibility(View.VISIBLE);
            } else {
                textView.setVisibility(View.GONE);
            }
        };

        showSubmissionInfoCheckbox.setOnCheckedChangeListener((buttonView, isChecked) -> updateSubmissionInfo.run());
        includeDeviceInfoCheckbox.setOnCheckedChangeListener((buttonView, isChecked) -> updateSubmissionInfo.run());
        feedbackEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                updateSubmissionInfo.run();
            }
        });
        submitButton.setOnClickListener(v -> {
            String message = feedbackEditText.getText().toString().trim();
            if (message.isEmpty()) {
                Toast.makeText(this, "Please enter your feedback.", Toast.LENGTH_SHORT).show();
                return;
            }

            Map<String, String> payload = new HashMap<>();
            payload.put("message", message);
            payload.put("user_key", feedbackKey != null ? feedbackKey : "");

            if (includeDeviceInfoCheckbox.isChecked()) {
                payload.put("os_data", "Android " + Build.VERSION.RELEASE + " (API " + Build.VERSION.SDK_INT + ")");
                payload.put("device_data", Build.MANUFACTURER + " " + Build.MODEL);
            } else {
                payload.put("os_data", "");
                payload.put("device_data", "");
            }



            submitButton.setEnabled(false);
            submitButton.setText("Sending...");

            new FetchData().sendFeedback(payload, new FetchData.OnFeedbackSentCallback() {
                @Override
                public void onSuccess() {
                    runOnUiThread(() -> {
                        Toast.makeText(MainActivity.this, "Feedback sent! Thank you.", Toast.LENGTH_LONG).show();
                        dialog.dismiss();
                    });
                }

                @Override
                public void onFailure(Exception e) {
                    runOnUiThread(() -> {
                        Toast.makeText(MainActivity.this, "Failed to send feedback. Please try again later.", Toast.LENGTH_LONG).show();
                        submitButton.setEnabled(true);
                        submitButton.setText("Submit Feedback");
                    });
                }
            });
        });

        dialog.show();
    }

    private void showAudienceMenu() {
        if (category_type_and_audience_data == null) return;
        try {
            JSONArray audiences = category_type_and_audience_data.getJSONArray("audiences");
            String[] items = new String[audiences.length() + 1];
            items[0] = "All Audiences";
            for (int i = 0; i < audiences.length(); i++) {
                items[i + 1] = audiences.getString(i);
            }

            int checkedItem = 0;
            if (selectedAudience != null) {
                for (int i = 0; i < items.length; i++) {
                    if (items[i].equals(selectedAudience)) {
                        checkedItem = i;
                        break;
                    }
                }
            }

            new MaterialAlertDialogBuilder(this)
                    .setTitle("Select Target Audience")
                    .setSingleChoiceItems(items, checkedItem, (dialog, which) -> {
                        selectedAudience = (which == 0) ? null : items[which];
                        applyFilters();
                        dialog.dismiss();
                    })
                    .show();
        } catch (JSONException e) {
            Log.e(TAG, "Error showing audience menu", e);
        }
    }

    private void showServiceMenu() {
        if (category_type_and_audience_data == null) return;
        try {
            JSONObject categories = category_type_and_audience_data.getJSONObject("categories/types");
            List<String> itemList = new ArrayList<>();
            itemList.add("All Services");
            
            Iterator<String> categoryKeys = categories.keys();
            while (categoryKeys.hasNext()) {
                String category = categoryKeys.next();
                itemList.add("CATEGORY: " + category);
                JSONArray types = categories.getJSONArray(category);
                for (int i = 0; i < types.length(); i++) {
                    itemList.add("  - " + types.getString(i));
                }
            }

            String[] items = itemList.toArray(new String[0]);
            
            new MaterialAlertDialogBuilder(this)
                    .setTitle("Select Service Type")
                    .setItems(items, (dialog, which) -> {
                        String selected = items[which];
                        if (which == 0) {
                            selectedCategory = null;
                            selectedType = null;
                        } else if (selected.startsWith("CATEGORY: ")) {
                            selectedCategory = selected.replace("CATEGORY: ", "");
                            selectedType = null;
                        } else {
                            // Find the parent category for this type
                            selectedType = selected.replace("  - ", "");
                            selectedCategory = null; // Typing takes priority
                        }
                        applyFilters();
                    })
                    .show();
        } catch (JSONException e) {
            Log.e(TAG, "Error showing service menu", e);
        }
    }

    private void applyFilters() {
        filteredEvents = new ArrayList<>();

        for (ServiceEvent event : allEvents) {
            boolean matchesCategory = (selectedCategory == null) || event.getServiceCategory().contains(selectedCategory);
            boolean matchesType = (selectedType == null) || event.getServiceType().contains(selectedType);
            boolean matchesAudience = (selectedAudience == null) || event.getAudience().contains(selectedAudience);

            if (matchesCategory && matchesType && matchesAudience) {
                filteredEvents.add(event);
            }
        }

        populateCalendarDecorators();
        if (selectedCalendarDay != null) {
            populateRecyclerView(selectedCalendarDay);
        }
        
        // Update button text to show active filters
        serviceSearchButton.setText(selectedType != null ? selectedType : (selectedCategory != null ? selectedCategory : "Services"));
        if (selectedAudience != null && selectedAudience.length() > "Target Audience".length() + 3) {
            String truncatedAudience = selectedAudience.substring(0, "Target Audience".length()) + "...";
            audienceSearchButton.setText(truncatedAudience);
        } else {
            audienceSearchButton.setText(selectedAudience != null ? selectedAudience : "Target Audience");
        }
    }

    private void startDataFetch() {
        showLoading();
        FetchData fetchData = new FetchData();
        fetchData.fetch(new FetchData.OnDataReadyCallback() {
            @Override
            public void onDataReady(JSONObject data) {
                allEvents = parseAllEvents(data.optJSONObject("services"));
                category_type_and_audience_data = prepareCategoryTypeAndAudienceData(data);
                
                announcementMessage = data.optString("announcement", "");
                if (announcementMessage.equalsIgnoreCase("null")) announcementMessage = "";
                updateAnnouncementUI();

                if (eventAdapter != null) {
                    eventAdapter.setCategoryTypeAndAudienceData(category_type_and_audience_data);
                }

                feedbackKey = data.optString("feedback_key", "");

                setupCalendar();
                applyFilters();
                showContent();
            }

            @Override
            public void onFailure(Exception e) {
                Log.e(TAG, "Failed to fetch data", e);
                showError();
            }
        });
    }

    private void populateCalendarDecorators() {
        ArrayList<CalendarDay> eventDays = new ArrayList<>();
        for (ServiceEvent event : filteredEvents) {
            CalendarDay day = Utilities.convertDateStringtoCalendarDay(event.getIsoLikeStartTime());
            if (day != null && !eventDays.contains(day)) {
                day.setImageResource(R.mipmap.ic_dot);
                eventDays.add(day);
            }
        }
        calendarView.setCalendarDays(eventDays);
    }

    private void handleCalendarDayClick(CalendarDay calendarDay) {
        Calendar today = Calendar.getInstance();
        today.set(Calendar.HOUR_OF_DAY, 0);
        today.set(Calendar.MINUTE, 0);
        today.set(Calendar.SECOND, 0);
        today.set(Calendar.MILLISECOND, 0);

        if (!calendarDay.getCalendar().before(today)) {
            this.selectedCalendarDay = calendarDay;
            populateRecyclerView(calendarDay);
            
            Date date = calendarDay.getCalendar().getTime();
            SimpleDateFormat monthDayFormat = new SimpleDateFormat("MMMM d", Locale.getDefault());
            serviceText.setText("Showing events for " + monthDayFormat.format(date));
        }
    }

    private void populateRecyclerView(CalendarDay date) {
        List<ServiceEvent> eventsForDay = new ArrayList<>();
        SimpleDateFormat searchSdf = new SimpleDateFormat("MM-dd-yyyy", Locale.US);
        String searchDate = searchSdf.format(date.getCalendar().getTime());

        for (ServiceEvent event : filteredEvents) {
            if (event.getDate().equals(searchDate)) {
                eventsForDay.add(event);
            }
        }

        eventsForDay.sort(Comparator.comparing(ServiceEvent::getIsoLikeStartTime)
                .thenComparing(ServiceEvent::getProviderName));

        eventAdapter.updateEvents(eventsForDay);
        if(eventsForDay.isEmpty()) {
            Toast.makeText(this, "No events scheduled for this day.", Toast.LENGTH_SHORT).show();
        }
    }

    private ArrayList<ServiceEvent> parseAllEvents(JSONObject dataFromApi) {
        ArrayList<ServiceEvent> events = new ArrayList<>();
        if (dataFromApi == null) return events;

        Iterator<String> keys = dataFromApi.keys();
        while (keys.hasNext()) {
            String key = keys.next();
            try {
                JSONObject serviceEventData = dataFromApi.getJSONObject(key);
                events.add(new ServiceEvent(key,
                        serviceEventData.getString("provider_name"),
                        Utilities.convertJSONArraytoStringList(serviceEventData.getJSONArray("service_category")),
                        Utilities.convertJSONArraytoStringList(serviceEventData.getJSONArray("service_type")),
                        serviceEventData.getString("start_time"),
                        serviceEventData.getString("end_time"),
                        serviceEventData.getString("address"),
                        serviceEventData.getString("phone"),
                        serviceEventData.getString("email"),
                        Utilities.convertJSONArraytoStringList(serviceEventData.getJSONArray("audience")),
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
        eventRecyclerView.addItemDecoration(new DividerItemDecoration(this, layoutManager.getOrientation()));
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

    private void showAnnouncementDialog() {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Service Announcement")
                .setMessage(announcementMessage)
                .setPositiveButton("Dismiss", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void updateAnnouncementUI() {
        if (navigationView != null) {
            Menu menu = navigationView.getMenu();
            MenuItem mainItem = menu.findItem(R.id.nav_announcements);
            if (mainItem != null) mainItem.setVisible(!announcementMessage.isEmpty());
        }
    }

    private JSONObject prepareCategoryTypeAndAudienceData(JSONObject api_data) {
        JSONObject data = new JSONObject();
        try {
            if (api_data.has("audiences")) data.put("audiences", api_data.getJSONArray("audiences"));
            if (api_data.has("categories/types")) data.put("categories/types", api_data.getJSONObject("categories/types"));
        } catch (JSONException e) {
            Log.e(TAG, "Error preparing lookup data", e);
        }
        return data;
    }

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
