package io.github.sjfailure.kccommunityconnect;

import com.applandeo.materialcalendarview.CalendarDay;

import java.util.Calendar;
import java.util.Date;

public class DateToCalendarConverter {

    public CalendarDay convert(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
//        int year = calendar.get(Calendar.YEAR);
//        int month = calendar.get(Calendar.MONTH);
//        int day = calendar.get(Calendar.DAY_OF_MONTH);
        CalendarDay calendarDay = new CalendarDay(calendar);
        return calendarDay;
    }

}
