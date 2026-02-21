package io.github.sjfailure.kccommunityconnect;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class ServiceEventTest {

    private List<String> foodCategory;
    private List<String> dinnerType;
    private List<String> everyoneAudience;
    private List<String> familyAudience;
    private List<String> shelterCategory;
    private List<String> overnightType;
    
    @Before
    public void setUp() {
        List<String> foodCategory = new ArrayList<String>();
        foodCategory.add("Food");
        List<String> dinnerType = new ArrayList<String>();
        dinnerType.add("Dinner");
        List<String> everyoneAudience = new ArrayList<String>();
        everyoneAudience.add("Everyone");
        List<String> familyAudience = new ArrayList<String>();
        familyAudience.add("Families");
        List<String> shelterCategory = new ArrayList<String>();
        shelterCategory.add("Shelter");
        List<String> overnightType = new ArrayList<String>();
        overnightType.add("Overnight");
    }
    // Test the "happy path" - does the constructor work with valid data?
    @Test
    public void constructor_withValidData_parsesCorrectly() {
        

        ServiceEvent event = new ServiceEvent(
                "123",
                "Test Provider",
                foodCategory,
                dinnerType,
                "2024-05-15 22:30:00", // 10:30 PM UTC
                "2024-05-16 01:00:00", // 1:00 AM UTC
                "123 Main St",
                "555-1234",
                "test@example.com",
                everyoneAudience,
                "Some notes"
        );

        assertNotNull(event);
        assertEquals("Test Provider", event.getProviderName());
        // Note: This test assumes the test environment's timezone can convert from UTC.
        // The exact output might vary, but we can check that it's not empty.
        assertNotNull(event.getStartTime());
        assertNotNull(event.getEndTime());
        assertEquals("05-15-2024", event.getDate());
        assertEquals("May 15, 2024", event.getMonthDayYear());
    }

    // Happy path 2: Does it work with null note information?
    @Test
    public void constructor_withNullNotes_parsesCorrectly() {
        ServiceEvent event = new ServiceEvent(
                "4",
                "Test Provider",
                foodCategory,
                dinnerType,
                "2024-05-15 22:30:00",
                "2024-05-16 01:00:00",
                "123 Main St",
                "555-1234",
                "test@example.com",
                everyoneAudience,
                null
        );

        assertNotNull(event);
        assertEquals("Test Provider", event.getProviderName());
        assertNotNull(event.getStartTime());
        assertNotNull(event.getEndTime());
        assertEquals("05-15-2024", event.getDate());
        assertEquals("May 15, 2024", event.getMonthDayYear());
        assertEquals("", event.getNotes());
    }

    // Happy path 3: Does it work with null phone number?
    @Test
    public void constructor_withNullPhone_parsesCorrectly() {
        ServiceEvent event = new ServiceEvent(
                "5",
                "Test Provider",
                foodCategory,
                dinnerType,
                "2024-05-15 22:30:00",
                "2024-05-16 01:00:00",
                "123 Main St",
                null,
                "test@example.com",
                everyoneAudience,
                "Some notes"
        );

        assertNotNull(event);
        assertEquals("Test Provider", event.getProviderName());
        assertNotNull(event.getStartTime());
        assertNotNull(event.getEndTime());
        assertEquals("05-15-2024", event.getDate());
        assertEquals("May 15, 2024", event.getMonthDayYear());
        assertEquals("", event.getPhone());
    }

    // Happy path 4: Does it work with null address?
    @Test
    public void constructor_withNullAddress_parsesCorrectly() {
        ServiceEvent event = new ServiceEvent(
                "4953",
                "Test Provider",
                foodCategory,
                dinnerType,
                "2026-03-21 12:30:00",
                "2026-03-21 13:30:00",
                null,
                "555-1010",
                "test@example.com",
                everyoneAudience,
                "Some notes"
        );

        assertNotNull(event);
        assertEquals("Test Provider", event.getProviderName());
        assertNotNull(event.getStartTime());
        assertNotNull(event.getEndTime());
        assertEquals("03-21-2026", event.getDate());
        assertEquals("Mar 21, 2026", event.getMonthDayYear());
        assertEquals("", event.getAddress());
    }

    // Test the "sad path" - does the constructor handle bad data without crashing?
    @Test
    public void constructor_withInvalidDate_handlesGracefully() {
        ServiceEvent event = new ServiceEvent(
                "456",
                "Another Provider",
                shelterCategory,
                overnightType,
                "not-a-real-date", // Invalid data
                "also-not-a-date",
                "456 Oak Ave",
                "555-5678",
                "another@example.com",
                familyAudience,
                "More notes"
        );

        // The most important thing is that the constructor didn't crash.
        assertNotNull(event);
        // Check that the fields that relied on the invalid date are now empty.
        assertEquals("", event.getStartTime());
        assertEquals("", event.getEndTime());
        assertEquals("", event.getDate());
        assertEquals("", event.getMonthDayYear());
        // Check that other fields are still populated correctly.
        assertEquals("456", event.getId());
    }
}
