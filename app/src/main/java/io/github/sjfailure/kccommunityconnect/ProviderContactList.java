package io.github.sjfailure.kccommunityconnect;

import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ProviderContactList extends AppCompatActivity {
    private RecyclerView providerRecyclerView;
    private ProviderAdapter providerAdapter;
    private List<Provider> providerList;
    private JSONObject providerData;
    private final String TAG = "ProviderContactList";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.provider_contact_list);

        providerList = new ArrayList<>();
        setupRecyclerView();

        // Fetch data from the API
        FetchProviderData fetchData = new FetchProviderData();
        fetchData.fetch(new FetchProviderData.OnDataReadyCallback() {
            @Override
            public void onDataReady(JSONObject data) {
                providerData = data;
                populateRecyclerView();
            }

            @Override
            public void onFailure(Exception e) {
                Log.e(TAG, "Failed to fetch provider data", e);
            }
        });
    }

    private void setupRecyclerView() {
        providerRecyclerView = findViewById(R.id.providerRecyclerView);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        providerRecyclerView.setLayoutManager(layoutManager);
        
        providerAdapter = new ProviderAdapter(providerList);
        providerRecyclerView.setAdapter(providerAdapter);
        
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(
                providerRecyclerView.getContext(),
                layoutManager.getOrientation()
        );
        providerRecyclerView.addItemDecoration(dividerItemDecoration);
    }

    private void populateRecyclerView() {
        if (providerData == null) return;

        List<Provider> newList = new ArrayList<>();
        Iterator<String> keys = providerData.keys();
        while (keys.hasNext()) {
            String key = keys.next();

            try {
                JSONObject p = providerData.getJSONObject(key);
                
                String phone = p.optString("phone", "");
                String email = p.optString("email", "");

                boolean hasPhone = !phone.isEmpty() && !phone.equalsIgnoreCase("null");
                boolean hasEmail = !email.isEmpty() && !email.equalsIgnoreCase("null");

                if (hasPhone || hasEmail) {
                    newList.add(new Provider(
                            p.getString("name"),
                            p.optString("address", ""),
                            hasPhone ? phone : "",
                            hasEmail ? email : ""
                    ));
                }
            } catch (JSONException e) {
                Log.e(TAG, "Error parsing individual provider", e);
            }
        }
        
        providerAdapter.updateProviders(newList);
    }
}
