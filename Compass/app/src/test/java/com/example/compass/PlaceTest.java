package com.example.compass;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.*;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
@RunWith(MockitoJUnitRunner.class)
public class PlaceTest {

    @Spy
    Place place;

    @Before
    public void setUp() throws Exception {
        place = new Place(51, 17);
    }

    @Test
    public void meterDistanceBetweenPoints() {
        assertEquals(place.meterDistanceBetweenPoints(51, 18), 69976, 1);
    }

    @Test
    public void meterDistanceBetweenPoints_SamePlace() {
        double lat = place.getLatitude();
        double lng = place.getLongitude();
        assertEquals(place.meterDistanceBetweenPoints(lat, lng), 0.0, 0);
    }

    @Test
    public void rotationAngle_NoChange() {
        double lat = place.getLatitude();
        double lng = place.getLongitude();
        assertEquals(place.rotationAngle(lat, lng), 180, 0);
    }
}