package io.github.sjfailure.kccommunityconnect;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.function.BiPredicate;

public class DetailViewActivity extends AppCompatActivity {

    private final String TAG = "DetailViewActivity";
    private JSONObject event_detail_data;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate called");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.detail_view);

        // Find all the TextViews from the layout
        TextView providerName = findViewById(R.id.providerNameTextView);
        TextView address = findViewById(R.id.addressTextView);
        TextView phone = findViewById(R.id.phoneTextView);
        TextView serviceType = findViewById(R.id.serviceTypeTextView);
        TextView email = findViewById(R.id.emailTextView);
        TextView dateTime = findViewById(R.id.dateTimeTextView);
        TextView audience = findViewById(R.id.audienceTextView);
        TextView notes = findViewById(R.id.notesTextView);

        // --- Data Population Logic ---

        BiPredicate<TextView, String> setTextOrHide = (textView, text) -> {
            if (text != null && !text.isEmpty() && !text.equalsIgnoreCase("null")) {
                textView.setText(text);
                textView.setVisibility(View.VISIBLE);
                return true;
            } else {
                textView.setVisibility(View.GONE);
                return false;
            }
        };

        String eventId = getIntent().getStringExtra("EVENT_ID");
        if (eventId == null || eventId.isEmpty()) {
            Log.e(TAG, "No Event ID provided. Finishing activity.");
            finish();
            return;
        }

        FetchData fetchData = new FetchData();
        fetchData.detailFetch(new FetchData.OnDataReadyCallback() {
            @Override
            public void onDataReady(JSONObject data) {
                Log.d(TAG, "onDataReady called with data: " + data);
                if (data == null) {
                    Log.e(TAG, "Received null data from detailFetch.");
                    Toast.makeText(DetailViewActivity.this, "Error: Could not load event details.", Toast.LENGTH_LONG).show();
                    return;
                }
                try {
                    event_detail_data = data.getJSONObject("event_data");
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
                try {
                    setTextOrHide.test(providerName, event_detail_data.getString("provider"));
                    setTextOrHide.test(address, event_detail_data.getString("address"));
                    setTextOrHide.test(phone, event_detail_data.getString("phone"));
                    setTextOrHide.test(email, event_detail_data.getString("email_address"));
                    setTextOrHide.test(serviceType, event_detail_data.getString("type") + " - " + event_detail_data.getString("category"));
                    Utilities utilities = new Utilities();
                    setTextOrHide.test(dateTime, utilities.formatDateTimeRange(event_detail_data.getString("start"), event_detail_data.getString("end")));
                    setTextOrHide.test(audience, event_detail_data.getString("audience"));
                    setTextOrHide.test(notes, event_detail_data.getString("note"));
                } catch (JSONException e) {
                    Log.e(TAG, "Error parsing event details from JSON", e);
                    Toast.makeText(DetailViewActivity.this, "Error loading event details.", Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    // This is the crucial part. It will catch any other exceptions, like NullPointerException.
                    Log.e(TAG, "An unexpected error occurred while populating UI", e);
                    Toast.makeText(DetailViewActivity.this, "An unexpected error occurred.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Exception e) {
                Log.e(TAG, "Failed to fetch event details", e);
            }
        }, eventId);
    }
}
