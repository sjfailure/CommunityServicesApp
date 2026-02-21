package io.github.sjfailure.kccommunityconnect;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import android.os.Parcel;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;

/**
 * Instrumented test, which will execute on an Android device.
 * This is the correct place to test Android-specific functionality like Parcelable.
 */
@RunWith(AndroidJUnit4.class)
public class ServiceEventInstrumentedTest {

    private List<String> healthCategory;
    private List<String> clinicType;
    private List<String> seniorsAudience;

    @Before
    public void setUp() {
        healthCategory = new ArrayList<String>();
        healthCategory.add("Health");
        clinicType = new ArrayList<String>();
        clinicType.add("Clinic");
        seniorsAudience = new ArrayList<String>();
        seniorsAudience.add("Seniors");
    }

    @Test
    public void parcelable_writeAndRead_restoresObject() {
        // 1. Create an original object
        ServiceEvent originalEvent = new ServiceEvent(
                "789", "Parcel Provider", healthCategory, clinicType,
                "2025-01-01 12:00:00", "2025-01-01 13:00:00",
                "789 Pine Ln", "555-9012", "parcel@example.com",
                seniorsAudience, "Parcel notes"
        );

        // 2. Write the object to a Parcel
        Parcel parcel = Parcel.obtain();
        originalEvent.writeToParcel(parcel, originalEvent.describeContents());

        // 3. Reset the parcel for reading
        parcel.setDataPosition(0);

        // 4. Create a new object from the Parcel
        ServiceEvent createdFromParcel = ServiceEvent.CREATOR.createFromParcel(parcel);

        // 5. Assert that the new object has the same data as the original
        assertNotNull(createdFromParcel);
        assertEquals(originalEvent.getId(), createdFromParcel.getId());
        assertEquals(originalEvent.getProviderName(), createdFromParcel.getProviderName());
        assertEquals(originalEvent.getStartTime(), createdFromParcel.getStartTime());
        assertEquals(originalEvent.getAddress(), createdFromParcel.getAddress());
        assertEquals(originalEvent.getNotes(), createdFromParcel.getNotes());

        // Release the parcel
        parcel.recycle();
    }
}