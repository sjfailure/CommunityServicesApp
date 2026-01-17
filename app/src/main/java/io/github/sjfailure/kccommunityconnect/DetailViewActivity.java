package io.github.sjfailure.kccommunityconnect;

import android.os.Bundle;
import android.widget.TextView;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import java.util.function.BiPredicate;

public class DetailViewActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {super.onCreate(savedInstanceState);
        setContentView(R.layout.detail_view);

        // Assume you have a 'Service' object passed via Intent
        // Service myService = (Service) getIntent().getSerializableExtra("SERVICE_DATA");
        // For this example, I'll use placeholder data.

        // Find all the TextViews from the layout
        TextView providerName = findViewById(R.id.providerNameTextView);
        TextView address = findViewById(R.id.addressTextView);
        TextView phone = findViewById(R.id.phoneTextView);
        TextView serviceType = findViewById(R.id.serviceTypeTextView);
        TextView dateTime = findViewById(R.id.dateTimeTextView);
        TextView audience = findViewById(R.id.audienceTextView);
        TextView notes = findViewById(R.id.notesTextView);

        // --- Data Population Logic ---

        // This is a helper function to check for empty/null strings
        // You can also use TextUtils.isEmpty()
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

        // Populate each view
        // In a real app, you would get these values from your Service object
        setTextOrHide.test(providerName, "Bishop Sullivan: One City Cafe");
        setTextOrHide.test(address, "3936 Troost Ave.");
        setTextOrHide.test(phone, "816-561-8515");
        setTextOrHide.test(serviceType, "Food - Dinner");
        setTextOrHide.test(dateTime, "Oct 27, 2025, 9:30 PM - 6:00 PM");
        setTextOrHide.test(audience, "Everyone");

        // Special handling for notes, as it has a different style
        String notesText = ""; // Example: Get this from your object
        if (setTextOrHide.test(notes, notesText)) {
            // The note is visible and has text
        }
    }
}
