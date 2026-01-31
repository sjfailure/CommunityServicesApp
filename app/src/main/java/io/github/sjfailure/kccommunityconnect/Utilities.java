package io.github.sjfailure.kccommunityconnect;

import android.util.Log;

import com.applandeo.materialcalendarview.CalendarDay;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
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


}
