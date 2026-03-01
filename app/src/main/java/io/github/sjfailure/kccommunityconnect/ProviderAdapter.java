package io.github.sjfailure.kccommunityconnect;

import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Comparator;
import java.util.List;

public class ProviderAdapter extends RecyclerView.Adapter<ProviderAdapter.ProviderViewHolder> {
    private List<Provider> providerList;

    public ProviderAdapter(List<Provider> providerList) {
        sortList(providerList);
        this.providerList = providerList;
    }

    @NonNull
    @Override
    public ProviderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.provider_list_item, parent, false);
        return new ProviderViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ProviderViewHolder holder, int position) {
        Provider currentProvider = providerList.get(position);
        
        holder.name.setText(currentProvider.getName());
        
        if (currentProvider.getAddress() != null && !currentProvider.getAddress().isEmpty()) {
            holder.address.setText(currentProvider.getAddress());
            holder.address.setVisibility(View.VISIBLE);
        } else {
            holder.address.setVisibility(View.GONE);
        }

        if (currentProvider.getPhone() != null && !currentProvider.getPhone().isEmpty()) {
            holder.phone.setText(currentProvider.getPhone());
            holder.phone.setVisibility(View.VISIBLE);
            holder.phone.setOnClickListener(v -> {
                if (currentProvider.getPhone().length() != 5) {
                    Intent intent = new Intent(Intent.ACTION_DIAL);
                    intent.setData(Uri.parse("tel:" + currentProvider.getPhone()));
                    v.getContext().startActivity(intent);
                } else {
                    // Specific solution to handle Text-specific data lines, solution might
                    // require refinement.
                    Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:" + currentProvider.getPhone()));
                    v.getContext().startActivity(intent);
                }
            });
        } else {
            holder.phone.setVisibility(View.GONE);
        }

        if (currentProvider.getEmail() != null && !currentProvider.getEmail().isEmpty()) {
            holder.email.setText(currentProvider.getEmail());
            holder.email.setVisibility(View.VISIBLE);
            holder.email.setOnClickListener(v -> {
                Intent intent = new Intent(Intent.ACTION_SENDTO);
                intent.setData(Uri.parse("mailto:" + currentProvider.getEmail()));
                v.getContext().startActivity(intent);
            });
        } else {
            holder.email.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return providerList != null ? providerList.size() : 0;
    }

    public void updateProviders(List<Provider> newList) {
        sortList(newList);
        this.providerList = newList;
        notifyDataSetChanged();
    }

    /**
     * Helper method to sort the provider list alphabetically by name.
     */
    private void sortList(List<Provider> list) {
        if (list != null && !list.isEmpty()) {
            list.sort(Comparator.comparing(Provider::getName, String.CASE_INSENSITIVE_ORDER));
        }
    }

    static class ProviderViewHolder extends RecyclerView.ViewHolder {
        TextView name, address, phone, email;

        public ProviderViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.providerName);
            address = itemView.findViewById(R.id.providerAddress);
            phone = itemView.findViewById(R.id.providerPhone);
            email = itemView.findViewById(R.id.providerEmail);
        }
    }
}
