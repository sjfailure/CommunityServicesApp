package io.github.sjfailure.kccommunityconnect;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;

import com.applandeo.materialcalendarview.CalendarDay;

import org.junit.Test;

import java.text.ParseException;

public class UtilitiesTest {

    // .convertDateStringtoCalendarDay tests
    // Happy Path 1: Valid date string
    @Test
    public void convertDateStringtoCalendarDay_validDateString_returnsCalendarDay() {
        String validDateString = "2023-08-15 12:00:00";
        CalendarDay result = Utilities.convertDateStringtoCalendarDay(validDateString);
        assertNotNull(result);
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
    public void convertDateStringtoCalendarDay_invalidDateString_throwsParseException() {
        String invalidDateString = "invalid-date-string";
        assertNull(Utilities.convertDateStringtoCalendarDay(invalidDateString));
    }

}
