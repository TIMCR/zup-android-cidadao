package br.com.lfdb.particity.util;

import android.location.Address;
import android.util.Log;
import br.com.lfdb.particity.core.Constantes;
import br.com.lfdb.particity.core.ConstantesBase;
import br.com.lfdb.particity.domain.Place;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.VisibleRegion;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class GeoUtils {

  public static long getVisibleRadius(GoogleMap map) {
    LatLng corner = map.getProjection().getVisibleRegion().farLeft;
    LatLng center = map.getCameraPosition().target;
    return distance(corner, center);
  }

  public static long distance(LatLng point1, LatLng point2) {
    double lat1 = point1.latitude;
    double lat2 = point2.latitude;
    double lon1 = point1.longitude;
    double lon2 = point2.longitude;
    double dLat = Math.toRadians(lat2 - lat1);
    double dLon = Math.toRadians(lon2 - lon1);
    double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) + Math.cos(Math.toRadians(lat1)) * Math.cos(
        Math.toRadians(lat2)) * Math.sin(dLon / 2) * Math.sin(dLon / 2);
    double c = 2 * Math.asin(Math.sqrt(a));
    return (long) (6366000 * c);
  }

  public static boolean isVisible(VisibleRegion visibleRegion, LatLng position) {
    return visibleRegion.latLngBounds.contains(position);
  }

  public static Address search(String str, double lat, double lng) {

    Request request = new Request.Builder().url(
        "https://maps.googleapis.com/maps/api/place/search/json"
            + "?sensor=true"
            + "&name="
            + str.replace(" ", "%20")
            + "&key="
            + Constantes.PLACES_KEY
            + "&radius="
            + 50000
            + "&location="
            + lat
            + ','
            + lng
            + "&language=pt-BR").build();
    try {
      Response response = ConstantesBase.OK_HTTP_CLIENT.newCall(request).execute();
      String raw = response.body().string();
      JSONObject jsonObject = new JSONObject(raw);

      if ("OK".equalsIgnoreCase(jsonObject.getString("status"))) {
        JSONObject result = jsonObject.getJSONArray("results").getJSONObject(0);
        //String indiStr = result.getString("formatted_address");
        Address addr = new Address(Locale.getDefault());
        //addr.setAddressLine(0, indiStr);
        addr.setLatitude(
            result.getJSONObject("geometry").getJSONObject("location").getDouble("lat"));
        addr.setLongitude(
            result.getJSONObject("geometry").getJSONObject("location").getDouble("lng"));
        return addr;
      }
    } catch (IOException e) {
      Log.e("ZUP", "Error calling Google geocode webservice.", e);
    } catch (JSONException e) {
      Log.e("ZUP", "Error parsing Google geocode webservice response.", e);
    }

    return null;
  }

  public static Address getFromPlace(Place place) {
    String address = "https://maps.googleapis.com/maps/api/place/details/json?reference="
        +
        place.getReference()
        + "&sensor=true&language="
        + Locale.getDefault()
        + "&key="
        + Constantes.PLACES_KEY;
    Request request = new Request.Builder().url(address).build();
    try {
      Response response = ConstantesBase.OK_HTTP_CLIENT.newCall(request).execute();
      String raw = response.body().string();
      JSONObject jsonObject = new JSONObject(raw);

      if ("OK".equalsIgnoreCase(jsonObject.getString("status"))) {
        JSONObject result = jsonObject.getJSONObject("result");
        String indiStr = result.getString("formatted_address");
        Address addr = new Address(Locale.getDefault());
        addr.setAddressLine(0, indiStr);
        addr.setLatitude(
            result.getJSONObject("geometry").getJSONObject("location").getDouble("lat"));
        addr.setLongitude(
            result.getJSONObject("geometry").getJSONObject("location").getDouble("lng"));
        return addr;
      }
    } catch (IOException e) {
      Log.e("ZUP", "Error calling Google geocode webservice.", e);
    } catch (JSONException e) {
      Log.e("ZUP", "Error parsing Google geocode webservice response.", e);
    }

    return null;
  }

  public static List<Address> getFromLocation(double lat, double lng, int maxResults) {
    String address = String.format(Locale.ENGLISH,
        "http://maps.googleapis.com/maps/api/geocode/json?latlng=%1$f,%2$f&sensor=true&language="
            + Locale.getDefault().getCountry(), lat, lng);

    List<Address> retList = null;

    try {
      Request request = new Request.Builder().url(address).build();
      Response response = ConstantesBase.OK_HTTP_CLIENT.newCall(request).execute();
      String raw = response.body().string();
      JSONObject jsonObject = new JSONObject(raw);

      retList = new ArrayList<>();

      if ("OK".equalsIgnoreCase(jsonObject.getString("status"))) {
        JSONArray results = jsonObject.getJSONArray("results");
        for (int i = 0; i < (results.length() > maxResults ? maxResults : results.length()); i++) {
          JSONObject result = results.getJSONObject(i);
          String indiStr = result.getString("formatted_address");
          Address addr = new Address(Locale.getDefault());
          addr.setAddressLine(0, indiStr);
          addr.setLatitude(
              result.getJSONObject("geometry").getJSONObject("location").getDouble("lat"));
          addr.setLongitude(
              result.getJSONObject("geometry").getJSONObject("location").getDouble("lng"));
          retList.add(addr);
        }
      }
    } catch (IOException e) {
      Log.e("ZUP", "Error calling Google geocode webservice.", e);
    } catch (JSONException e) {
      Log.e("ZUP", "Error parsing Google geocode webservice response.", e);
    }

    return retList;
  }
}
