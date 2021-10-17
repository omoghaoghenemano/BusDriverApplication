package com.bustracking.myschool.bustracker.Utils;

import android.location.Location;

import com.google.android.gms.maps.model.LatLng;

public class Utils
{
    public static boolean findDistance(LatLng currentLatLng,LatLng busLatLng)
    {
        float[] results = new float[1];
        Location.distanceBetween(currentLatLng.latitude, currentLatLng.longitude, busLatLng.latitude, busLatLng.longitude, results);
        float distanceInMeters = results[0];
        boolean isWithin5km = distanceInMeters < 500;
        return isWithin5km;
    }

}
