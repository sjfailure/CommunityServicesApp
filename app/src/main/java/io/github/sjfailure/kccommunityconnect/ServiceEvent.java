package io.github.sjfailure.kccommunityconnect;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.stream.Collectors;

public class ServiceEvent implements Parcelable {

    private final String TAG = "ServiceEvent";
    private final String id;
    private final String providerName;
    private final List<String> serviceCategory;
    private final List<String> serviceType;
    private String startTime;
    private String endTime;
    private String date;
    private final String isoLikeStartTime;
    private String monthDayYear;
    private final String address;
    private final String phone;
    private final String email;
    private final List<String> audience;
    private final String notes;


    @Deprecated
    public ServiceEvent(String id,
                        String providerName,
                        List<String> serviceCategory,
                        List<String> serviceType,
                        String startTime) {
        this.id = id;
        this.providerName = providerName;
        this.serviceCategory = serviceCategory;
        this.serviceType = serviceType;
        this.startTime = startTime;
        this.isoLikeStartTime = startTime;
        this.endTime = null;
        this.date = null;
        this.monthDayYear = null;
        this.address = null;
        this.phone = null;
        this.email = null;
        this.audience = null;
        this.notes = null;
    }

    public ServiceEvent(String id,
                        String providerName,
                        List<String> serviceCategory,
                        List<String> serviceType,
                        String startInformation,
                        String endInformation,
                        String address,
                        String phone,
                        String email,
                        List<String> audience,
                        String notes) {
        this.id = id;
        this.providerName = providerName;
        this.serviceCategory = serviceCategory;
        this.serviceType = serviceType;
        if (address == null) address = "";
        this.address = address;
        if (phone == null) phone = "";
        this.phone = phone;
        if (email == null) email = "";
        this.email = email;
        this.audience = audience;
        if (notes == null) notes = "";
        this.notes = notes;
        this.isoLikeStartTime = startInformation;

        // This parser understands the incoming string is in UTC
        SimpleDateFormat utcParser = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
        utcParser.setTimeZone(TimeZone.getTimeZone("UTC"));

        try {
            // These formatters will use the device's LOCAL timezone by default
            SimpleDateFormat timeFormatter = new SimpleDateFormat("h:mm a", Locale.US);
            SimpleDateFormat dateFormatter = new SimpleDateFormat("MM-dd-yyyy", Locale.US);
            SimpleDateFormat monthDayYearFormatter = new SimpleDateFormat("MMM d, yyyy", Locale.US);

            // Parse the UTC strings into universal Date objects
            java.util.Date startDate = utcParser.parse(startInformation);
            java.util.Date endDate = utcParser.parse(endInformation);

            // Format the universal Date objects into local time strings
            if (startDate != null) {
                this.startTime = timeFormatter.format(startDate);
                this.date = dateFormatter.format(startDate);
                this.monthDayYear = monthDayYearFormatter.format(startDate);
            }
            if (endDate != null) {
                this.endTime = timeFormatter.format(endDate);
            }

        } catch (ParseException e) {
            // Log.e(TAG, "Failed to parse date string", e);
            // Set to empty strings on failure to prevent crashes
            this.startTime = "";
            this.endTime = "";
            this.date = "";
            this.monthDayYear = "";
        }
    }

    protected ServiceEvent(Parcel in) {
        id = in.readString();
        providerName = in.readString();
        serviceCategory = in.createStringArrayList();
        serviceType = in.createStringArrayList();
        startTime = in.readString();
        endTime = in.readString();
        date = in.readString();
        isoLikeStartTime = in.readString();
        monthDayYear = in.readString();
        address = in.readString();
        phone = in.readString();
        email = in.readString();
        audience = in.createStringArrayList();
        notes = in.readString();
    }

    public static final Creator<ServiceEvent> CREATOR = new Creator<ServiceEvent>() {
        @Override
        public ServiceEvent createFromParcel(Parcel in) {
            return new ServiceEvent(in);
        }

        @Override
        public ServiceEvent[] newArray(int size) {
            return new ServiceEvent[size];
        }
    };

    public String getId() {
        return id;
    }

    public String getProviderName() {
        return providerName;
    }

    public List<String> getServiceCategory() {
        return serviceCategory;
    }

    public List<String> getServiceType() {
        return serviceType;
    }

    public String getStartTime() {
        return startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public String getDate() {
        return date;
    }

    public String getMonthDayYear() {
        return monthDayYear;
    }

    public String getAddress() {
        return address;
    }

    public String getPhone() {
        return phone;
    }

    public String getEmail() {
        return email;
    }

    public List<String> getAudience() {
        return audience;
    }

    public String getNotes() {
        return notes;
    }

    public String getIsoLikeStartTime() {
        return isoLikeStartTime;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(providerName);
        dest.writeStringList(serviceCategory);
        dest.writeStringList(serviceType);
        dest.writeString(startTime);
        dest.writeString(endTime);
        dest.writeString(date);
        dest.writeString(isoLikeStartTime);
        dest.writeString(monthDayYear);
        dest.writeString(address);
        dest.writeString(phone);
        dest.writeString(email);
        dest.writeStringList(audience);
        dest.writeString(notes);
    }

    /**
     * Groups service categories and types into a formatted string using a provided hierarchy.
     * Example: "Food: Breakfast, Lunch\nHousing: Shelter"
     *
     * @param hierarchy A JSONObject representing the category/type hierarchy (e.g., from category_type_and_audience_data).
     *                  Expected format: {"Category1": {"TypeA": true, "TypeB": true}, "Category2": {"TypeC": true}}
     * @return A formatted string of categories and their associated types for this service event.
     */
    public String groupCategoriesAndTypes(JSONObject hierarchy) {
        if (hierarchy == null) {
            Log.e(TAG, "groupCategoriesAndTypes: hierarchy is null.");
            throw new NullPointerException();
        }
        Log.d(TAG, "groupCategoriesAndTypes: start of method, hierarchy intact?: " + hierarchy.getClass().toString());
        StringBuilder sb = new StringBuilder();

        if (serviceCategory == null || serviceCategory.isEmpty() || hierarchy == null) {
            Log.d(TAG, "groupCategoriesAndTypes: serviceCategory or hierarchy is null.");
            return "";
        }

        Log.d(TAG, "groupCategoriesAndTypes: attempting to build string");
        try {
            for (String category : serviceCategory) {
                // Check if this event's category exists in the hierarchy
                Log.d(TAG, "groupCategoriesAndTypes: checking category: " + category);
                if (hierarchy.getJSONObject("categories/types").has(category)) {
                    Log.d(TAG, "groupCategoriesAndTypes: category is present: " + category);
                    JSONArray typesInHierarchyForCategory = hierarchy.getJSONObject("categories/types").getJSONArray(category);
                    List<String> typesInHierarchyForCategoryList = new ArrayList<>();
                    for (int i = 0; i < typesInHierarchyForCategory.length(); i++) {
                        typesInHierarchyForCategoryList.add(typesInHierarchyForCategory.getString(i));
                    }
                    Log.d(TAG, "groupCategoriesAndTypes: typesInHierarchyForCategory: " + typesInHierarchyForCategory);
                    List<String> matchingTypes = new ArrayList<>();

                    // Find types for *this event* that also exist under *this category* in
                    // the hierarchy
                    for (String type : serviceType) {
                        Log.d(TAG, "groupCategoriesAndTypes: checking type: " + type);
                        if (typesInHierarchyForCategoryList.contains(type)) {
                            Log.d(TAG, "groupCategoriesAndTypes: type is present: " + type);
                            matchingTypes.add(type);
                        }
                    }

                    // Append to StringBuilder if matches are found
                    if (!matchingTypes.isEmpty()) {
                        sb.append(category).append(": ")
                                .append(String.join(", ", matchingTypes))
                                .append("\n");
                    }
                }
            }
        } catch (JSONException e) {
            Log.e(TAG, "Error processing category/type hierarchy: ", e);
            return "Error grouping services.";
        }

        // Remove the last newline character if present
        if (sb.length() > 0 && sb.charAt(sb.length() - 1) == '\n') {
            sb.setLength(sb.length() - 1);
        }

        String result = sb.toString();
        return result.isEmpty() ? "" : result;
    }

    public String getAudienceAsString() {
        return audience == null ? "" : String.join(", ", audience);
    }
}
