package br.com.lfdb.particity.util;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GPSUtils {

    public static List<Address> getFromLocationName(Context context, String s) {
        if (context == null) return new ArrayList<>();

        try {
            Geocoder geocoder = new Geocoder(context);
            List<Address> list = filterResults(geocoder.getFromLocationName(s, 10));
            if (!list.isEmpty()) return list;
        } catch (IOException e) {
            Log.w("ZUP", e.getMessage(), e);
        }

        try {
            return filterResults(Geocoder2.getFromLocationName(s));
        } catch (IOException e) {
            Log.e("ZUP", e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    public static List<Address> getFromLocation(Context context, double latitude, double longitude) {
        try {
            if (context == null){
                return Collections.emptyList();
            }
            Geocoder geocoder = new Geocoder(context);
            List<Address> list = filterResults(geocoder.getFromLocation(latitude, longitude, 6));
            if (list != null || !list.isEmpty()) {
                return list;
            }
            List<Address> addrs = filterResults(Geocoder2.getFromLocation(latitude, longitude));
            return addrs;
        } catch (IOException e) {
            Log.e("ZUP", e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    private static List<Address> filterResults(List<Address> list) {
        List<Address> arraylist = new ArrayList<>();
        for (Address address : list) {
            if (!Strings.isTrimmedNullOrEmpty(address.getLocality()) || !Strings.isTrimmedNullOrEmpty(address.getSubAdminArea())) {
                arraylist.add(address);
            }
        }
        return arraylist;
    }
}
