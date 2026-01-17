package io.github.sjfailure.kccommunityconnect;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class EventAdapter extends RecyclerView.Adapter<EventAdapter.EventViewHolder> {

    // TODO implement intent to open detail view activity
    private List<ServiceEvent> eventList;

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
        holder.serviceCategory.setText("Category: " + currentEvent.getServiceCategory());
        holder.serviceType.setText("Service: " + currentEvent.getServiceType());
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

    static class EventViewHolder extends RecyclerView.ViewHolder {
        TextView providerName, serviceCategory, serviceType, startTime;

        public EventViewHolder(@NonNull View itemView) {
            super(itemView);
            providerName = itemView.findViewById(R.id.providerName);
            serviceCategory = itemView.findViewById(R.id.serviceCategory);
            serviceType = itemView.findViewById(R.id.serviceType);
            startTime = itemView.findViewById(R.id.startTime);
        }
    }
}
