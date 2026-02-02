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

        Bundle eventBundle = getIntent().getExtras();
        if (eventBundle == null) {
            Log.e(TAG, "No event bundle provided. Finishing activity.");
            finish();
            return;
        }
        ServiceEvent event = eventBundle.getParcelable("SERVICE_EVENT");
        if (event == null) {
            Log.e(TAG, "No ServiceEvent provided in the bundle. Finishing activity.");
            finish();
            return;
        }

        setTextOrHide.test(providerName, event.getProviderName());
        setTextOrHide.test(address, event.getAddress());
        setTextOrHide.test(phone, event.getPhone());
        setTextOrHide.test(email, event.getEmail());
        setTextOrHide.test(serviceType, event.getServiceType() + " - " + event.getServiceCategory());

        setTextOrHide.test(dateTime, event.getMonthDayYear() + " - " + event.getStartTime() + " - " + event.getEndTime());
        setTextOrHide.test(audience, event.getAudience());
        setTextOrHide.test(notes, event.getNotes());


    }
}
