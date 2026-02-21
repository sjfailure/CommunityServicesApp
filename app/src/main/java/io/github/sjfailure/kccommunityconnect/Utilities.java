package io.github.sjfailure.kccommunityconnect;

import android.util.Log;

import com.applandeo.materialcalendarview.CalendarDay;

import org.json.JSONArray;
import org.json.JSONException;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class Utilities {

    private static final String TAG = "Utilities";

    /**
     * Converts an ISO-8601-like ("yyyy-MM-dd HH:mm:ss") date string to a CalendarDay object.
     *
     * @param dateString String in "yyyy-MM-dd HH:mm:ss" format
     * @return CalendarDay object or null on failure
     */
    public static CalendarDay convertDateStringtoCalendarDay(String dateString) {
        if (dateString == null || dateString.isEmpty()) {
            return null;
        }
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
        sdf.setTimeZone(java.util.TimeZone.getTimeZone("UTC"));
        try {
            Date date = sdf.parse(dateString);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            return new CalendarDay(calendar);
        } catch (ParseException e) {
//            Log.e(TAG, "Could not parse ISO date: " + dateString, e);
            System.out.println(TAG + " Could not parse ISO date: " + dateString + " - " + e);
            return null; // Return null on parsing failure
        }
    }

    public static List<String> convertJSONArraytoStringList(JSONArray jsonArray) {
        List<String> stringList = new ArrayList<>();
        for (int i = 0; i < jsonArray.length(); i++) {
            try {
                stringList.add(jsonArray.getString(i));
            } catch (JSONException e) {
                Log.e(TAG, "Error converting JSON array to string list", e);
            }
        }
        return stringList;
    }

    public static String convertListToStringForDisplay(List<String> list) {
        if (list == null || list.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (String item : list) {
            sb.append(item).append(", ");
        }
        return sb.substring(0, sb.length() - 2); // Remove the trailing comma and space
    }
}
