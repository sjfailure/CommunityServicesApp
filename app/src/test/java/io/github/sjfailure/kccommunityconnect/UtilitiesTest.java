package io.github.sjfailure.kccommunityconnect;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import com.applandeo.materialcalendarview.CalendarDay;

import org.junit.Test;

import java.util.Calendar;

public class UtilitiesTest {

    // .convertDateStringtoCalendarDay tests
    // Happy Path 1: Valid date string
    @Test
    public void convertDateStringtoCalendarDay_validDateString_returnsCalendarDay() {
        String validDateString = "2023-08-15 12:00:00";
        CalendarDay result = Utilities.convertDateStringtoCalendarDay(validDateString);
        assertNotNull(result);
        assertEquals(2023, result.getCalendar().get(Calendar.YEAR));
        assertEquals(Calendar.AUGUST, result.getCalendar().get(Calendar.MONTH));
        assertEquals(15, result.getCalendar().get(Calendar.DAY_OF_MONTH));
    }

    // Happy Path 2: Null date string
    @Test
    public void convertDateStringtoCalendarDay_nullDateString_returnsNull() {
        String nullDateString = null;
        CalendarDay result = Utilities.convertDateStringtoCalendarDay(nullDateString);
        assertNull(result);
    }

    // Sad Path 1: Empty date string
    @Test
    public void convertDateStringtoCalendarDay_emptyDateString_returnsNull() {
        String emptyDateString = "";
        CalendarDay result = Utilities.convertDateStringtoCalendarDay(emptyDateString);
        assertNull(result);
    }

    // Sad Path 2: Invalid date string
    @Test
    public void convertDateStringtoCalendarDay_invalidDateString_returnsNull() {
        String invalidDateString = "invalid-date-string";
        assertNull(Utilities.convertDateStringtoCalendarDay(invalidDateString));
    }

    // Sad Path 3: Invalid date or formatting
    @Test
    public void convertDateStringtoCalendarDay_invalidFormatString_returnsNull() {
        String invalidFormatString = "03/02/2026 - 3:45AM";
        assertNull(Utilities.convertDateStringtoCalendarDay(invalidFormatString));
    }

    // Edge Case 1: Leading white space
    @Test
    public void convertDateStringtoCalendarDay_leadingWhiteSpace_returnsCalendarDay() {
        String leadingWhiteSpaceString = " 2023-08-15 12:00:00";
        CalendarDay result = Utilities.convertDateStringtoCalendarDay(leadingWhiteSpaceString);
        assertNotNull(result);
        assertEquals(2023, result.getCalendar().get(Calendar.YEAR));
        assertEquals(Calendar.AUGUST, result.getCalendar().get(Calendar.MONTH));
        assertEquals(15, result.getCalendar().get(Calendar.DAY_OF_MONTH));
    }

    // Edge Case 2: Trailing white space
    @Test
    public void convertDateStringtoCalendarDay_trailingWhiteSpace_returnsCalendarDay() {
        String trailingWhiteSpaceString = "2023-08-15 12:00:00 ";
        CalendarDay result = Utilities.convertDateStringtoCalendarDay(trailingWhiteSpaceString);
        assertNotNull(result);
        assertEquals(2023, result.getCalendar().get(Calendar.YEAR));
        assertEquals(Calendar.AUGUST, result.getCalendar().get(Calendar.MONTH));
        assertEquals(15, result.getCalendar().get(Calendar.DAY_OF_MONTH));
    }



}
