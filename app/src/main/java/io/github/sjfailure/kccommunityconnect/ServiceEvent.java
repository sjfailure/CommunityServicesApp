package io.github.sjfailure.kccommunityconnect;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.TimeZone;

public class ServiceEvent implements Parcelable {

    private final String TAG = "ServiceEvent";
    private final String id;
    private final String providerName;
    private final String serviceCategory;
    private final String serviceType;
    private String startTime;
    private String endTime;
    private String date;
    private final String isoLikeStartTime;
    private String monthDayYear;
    private final String address;
    private final String phone;
    private final String email;
    private final String audience;
    private final String notes;

    @Deprecated
    public ServiceEvent(String id, String providerName, String serviceCategory, String serviceType, String startTime) {
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
                        String serviceCategory,
                        String serviceType,
                        String startInformation,
                        String endInformation,
                        String address,
                        String phone,
                        String email,
                        String audience,
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
//            Log.e(TAG, "Failed to parse date string", e);
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
        serviceCategory = in.readString();
        serviceType = in.readString();
        startTime = in.readString();
        endTime = in.readString();
        date = in.readString();
        isoLikeStartTime = in.readString();
        monthDayYear = in.readString();
        address = in.readString();
        phone = in.readString();
        email = in.readString();
        audience = in.readString();
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

    public String getServiceCategory() {
        return serviceCategory;
    }

    public String getServiceType() {
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

    public String getAudience() {
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
        dest.writeString(serviceCategory);
        dest.writeString(serviceType);
        dest.writeString(startTime);
        dest.writeString(endTime);
        dest.writeString(date);
        dest.writeString(isoLikeStartTime);
        dest.writeString(monthDayYear);
        dest.writeString(address);
        dest.writeString(phone);
        dest.writeString(email);
        dest.writeString(audience);
        dest.writeString(notes);
    }
}
