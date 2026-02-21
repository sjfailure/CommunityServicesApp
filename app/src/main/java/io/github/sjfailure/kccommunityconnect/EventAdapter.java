package io.github.sjfailure.kccommunityconnect;

import android.content.Intent;
import android.os.Parcelable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONObject;

import java.util.List;

public class EventAdapter extends RecyclerView.Adapter<EventAdapter.EventViewHolder> {

    private List<ServiceEvent> eventList;
    private JSONObject category_type_and_audience_data;

    private final String TAG = "EventAdapter";


    public EventAdapter(List<ServiceEvent> eventList) {
        this.eventList = eventList;
    }

    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.event_list_item, parent, false);
        return new EventViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        ServiceEvent currentEvent = eventList.get(position);
        holder.providerName.setText(currentEvent.getProviderName());
        holder.serviceCategory.setText("Category: " +
                Utilities.convertListToStringForDisplay(currentEvent.getServiceCategory()));
        holder.serviceType.setText("Service: " +
                Utilities.convertListToStringForDisplay(currentEvent.getServiceType()));
        holder.startTime.setText("Time: " + currentEvent.getStartTime());
    }

    @Override
    public int getItemCount() {
        return eventList.size();
    }

    public void updateEvents(List<ServiceEvent> newEvents) {
        this.eventList = newEvents;
        notifyDataSetChanged(); // This tells the adapter to refresh the list
    }

    class EventViewHolder extends RecyclerView.ViewHolder {
        TextView providerName, serviceCategory, serviceType, startTime;

        public EventViewHolder(@NonNull View itemView) {
            super(itemView);
            providerName = itemView.findViewById(R.id.providerName);
            serviceCategory = itemView.findViewById(R.id.serviceCategory);
            serviceType = itemView.findViewById(R.id.serviceType);
            startTime = itemView.findViewById(R.id.startTime);
            itemView.setOnClickListener(v -> {

                int position = getAdapterPosition();

                // Make sure the position is valid
                if (position != RecyclerView.NO_POSITION) {
                    ServiceEvent clickedEvent = eventList.get(position);
                    Log.d(TAG, "Item clicked: " + clickedEvent.getProviderName());

                    // Create the intent to start DetailViewActivity
                    Intent intent = new Intent(v.getContext(), DetailViewActivity.class);
                    // Pass the full ServiceEvent object to the new activity
                    intent.putExtra("SERVICE_EVENT", clickedEvent);
                    intent.putExtra(
                            "CATEGORY_TYPE_AND_AUDIENCE_DATA",
                            category_type_and_audience_data.toString()
                    );
                    // Start the new activity
                    v.getContext().startActivity(intent);
                }
            });
        }
    }

    public void setCategoryTypeAndAudienceData(JSONObject data) {
        this.category_type_and_audience_data = data;
    }
}
