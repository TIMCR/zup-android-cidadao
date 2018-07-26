package br.com.lfdb.particity.util;

import android.location.Address;
import android.util.Log;

import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import br.com.lfdb.particity.core.ConstantesBase;

public class Geocoder2 {

    public static List<Address> getFromLocationName(String s) throws IOException {
        JSONObject jsonObject = getLocationInfo(
                new StringBuilder("http://maps.google.com/maps/api/geocode/json?address=")
                        .append(URLEncoder.encode(s, "UTF-8")).append("&sensor=true")
                        .append("&language=pt-BR").toString());
        try {
            return getAddressList(jsonObject);
        } catch (Exception e) {
            Log.w("ZUP", e.getMessage(), e);
            throw new IOException(new StringBuilder().append("Could not get address list from location name: '").append(s).append("'").toString());
        }
    }

    public static List<Address> getFromLocation(double lat, double lng) throws IOException {
        JSONObject jsonObject = getLocationInfo(
                new StringBuilder("http://maps.google.com/maps/api/geocode/json?latlng=")
                        .append(lat).append(",").append(lng)
                        .append("&sensor=true")
                        .append("&language=pt-BR").toString());
        try {
            return getAddressList(jsonObject);
        } catch (Exception e) {
            Log.w("ZUP", e.getMessage(), e);
            throw new IOException(new StringBuilder().append("Could not get address list from location lat: '").append(lat)
                    .append("' lng: '").append(lng).append("'").toString());
        }
    }

    private static JSONObject getLocationInfo(String s) throws IOException {
        Request request = new Request.Builder().url(s).build();
        Response response = ConstantesBase.OK_HTTP_CLIENT.newCall(request).execute();
        String s1 = response.body().string();
        try {
            return new JSONObject(s1);
        } catch (Exception e) {
            Log.e("ZUP", e.getMessage() != null ? e.getMessage() : "null", e);
            return new JSONObject();
        }
    }

    private static List<Address> getAddressList(JSONObject jsonObject) throws Exception {
        JSONArray jsonArray = jsonObject.getJSONArray("results");
        List<Address> addresses = new ArrayList<Address>();
        int i = jsonArray.length();
        for (int j = 0; j < i && addresses.size() < 7; j++) {
            Address address = getAddress(jsonArray.getJSONObject(j));
            if (address != null) addresses.add(address);
        }

        return addresses;
    }

    private static Address getAddress(JSONObject jsonObject) throws JSONException {
        JSONObject jsonLocation = jsonObject.getJSONObject("geometry").getJSONObject("location");

        Address address = new Address(Locale.getDefault());
        address.setLatitude(jsonLocation.optDouble("lat", 0.0));
        address.setLongitude(jsonLocation.optDouble("lng", 0.0));

        JSONArray addressArray = jsonObject.getJSONArray("address_components");

        for (int i = 0; i < addressArray.length(); i++) {
            JSONObject partial = addressArray.getJSONObject(i);
            JSONArray types = partial.getJSONArray("types");
            for (int j = 0; j < types.length(); j++) {
                String type = types.getString(j);
                if ("postal_code".equals(type)) {
                    address.setPostalCode(partial.getString("long_name"));
                } else if ("locality".equals(type)) {
                    address.setLocality(partial.getString("long_name"));
                } else if ("street_number".equals(type)) {
                    address.setFeatureName(partial.getString("long_name"));
                } else if ("route".equals(type)) {
                    address.setThoroughfare(partial.getString("long_name"));
                }
            }
        }

        address.setAddressLine(0, new StringBuilder().append(address.getThoroughfare()).append(", ").append(address.getFeatureName()).toString());

        return address;
    }
}
