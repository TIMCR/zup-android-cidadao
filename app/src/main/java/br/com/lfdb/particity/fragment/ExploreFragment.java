package br.com.lfdb.particity.fragment;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.location.Address;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import br.com.lfdb.particity.DetalheMapaActivity;
import br.com.lfdb.particity.FiltroExploreNovoActivity;
import br.com.lfdb.particity.MainActivity;
import br.com.lfdb.particity.R;
import br.com.lfdb.particity.SolicitacaoDetalheActivity;
import br.com.lfdb.particity.api.model.Cluster;
import br.com.lfdb.particity.base.BaseFragment;
import br.com.lfdb.particity.core.Constantes;
import br.com.lfdb.particity.domain.BuscaExplore;
import br.com.lfdb.particity.domain.CategoriaInventario;
import br.com.lfdb.particity.domain.CategoriaRelato;
import br.com.lfdb.particity.domain.ItemInventario;
import br.com.lfdb.particity.domain.ItemRelato;
import br.com.lfdb.particity.domain.Place;
import br.com.lfdb.particity.domain.RequestModel;
import br.com.lfdb.particity.domain.SolicitacaoListItem;
import br.com.lfdb.particity.service.CategoriaInventarioService;
import br.com.lfdb.particity.service.CategoriaRelatoService;
import br.com.lfdb.particity.service.LoginService;
import br.com.lfdb.particity.task.MarkerRetriever;
import br.com.lfdb.particity.util.BitmapUtils;
import br.com.lfdb.particity.util.FontUtils;
import br.com.lfdb.particity.util.GeoUtils;
import br.com.lfdb.particity.util.MapUtils;
import br.com.lfdb.particity.util.PreferenceUtils;
import br.com.lfdb.particity.util.ToastHelper;
import br.com.lfdb.particity.util.ViewUtils;
import br.com.lfdb.particity.widget.PlacesAutoCompleteAdapter;

@EFragment(R.layout.fragment_explore) public class ExploreFragment extends BaseFragment
        implements GoogleMap.OnInfoWindowClickListener, GoogleMap.OnCameraChangeListener,
        AdapterView.OnItemClickListener, LocationListener, GoogleMap.OnMarkerClickListener,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,
        OnMapReadyCallback {

  public static final int MY_PERMISSIONS_REQUEST_READ_CONTACTS = 12122;
  @ViewById RelativeLayout header;
  @ViewById ImageView logo_header;
  @ViewById TextView botaoFiltrar;
  @ViewById ProgressBar loading;
  @ViewById ImageView clean;
  @ViewById ImageView locationButton;
  @ViewById AutoCompleteTextView autocomplete;

  private double userLongitude;
  private double userLatitude;
  private boolean updateCameraUser = true;
  private boolean wasLocalized = false;
  public float zoom = 12f;
  private static final long POLLING_FREQ = 1000 * 30;
  private static final long FASTEST_UPDATE_FREQ = 1000 * 5;
  private GoogleApiClient googleApiClient;
  private LocationRequest locationRequest;
  private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 1000;
  private RequestModel requestModel;
  private GoogleMap map;
  private BuscaExplore busca;
  public static double latitude = 0.0, longitude = 0.0;
  private Set<Object> itens = new HashSet<>();
  private Map<Marker, Object> marcadores = new HashMap<>();
  private Marker pontoBusca;
  private long raio = 0l;
  private LocationManager locationManager;
  private MarkerRetriever markerRetriever = null;
  private LoginService loginService;
  private ToastHelper toast;

  @Override public void onConnected(Bundle bundle) {
    if (!wasLocalized) {
      createLocationRequest();
      startLocationUpdates();
    }
  }

  void createLocationRequest() {
    locationRequest = LocationRequest.create();
    locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    locationRequest.setInterval(POLLING_FREQ);
    locationRequest.setFastestInterval(FASTEST_UPDATE_FREQ);
  }

  @Click void botaoFiltrar() {
    Intent intent = new Intent(getActivity(), FiltroExploreNovoActivity.class);
    intent.putExtra("busca", busca);
    getActivity().startActivityForResult(intent, MainActivity.FILTRO_CODE);
  }

  @Click void clean() {
    autocomplete.setText("");
  }

  @Click void locationButton() {
    CameraPosition position =
            new CameraPosition.Builder().target(new LatLng(userLatitude, userLongitude))
                    .zoom(15)
                    .build();
    CameraUpdate update = CameraUpdateFactory.newCameraPosition(position);
    map.animateCamera(update);
  }

  protected void startLocationUpdates() {
    LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest,
            this);
  }

  private boolean checkPlayServices() {
    int permissionCheck = ContextCompat.checkSelfPermission(getActivity(),
            Manifest.permission.ACCESS_COARSE_LOCATION);
    if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
      ActivityCompat.requestPermissions(getActivity(),
              new String[] { Manifest.permission.ACCESS_COARSE_LOCATION },
              MY_PERMISSIONS_REQUEST_READ_CONTACTS);
      return false;
    }
    return true;
  }

  @Override public void onConnectionSuspended(int i) {

  }

  @UiThread @Override public void onLocationChanged(Location location) {
    if (map == null) {
      return;
    }
    userLatitude = location.getLatitude();
    userLongitude = location.getLongitude();
    if (updateCameraUser) {
      CameraPosition position = new CameraPosition.Builder().target(
              new LatLng(location.getLatitude(), location.getLongitude())).zoom(15).build();
      CameraUpdate update = CameraUpdateFactory.newCameraPosition(position);
      map.clear();
      map.animateCamera(update);
      updateCameraUser = false;
      wasLocalized = true;
    }
  }

  @Override public boolean onMarkerClick(Marker marker) {
    Object marcador = marcadores.get(marker);
    if (marcador instanceof Cluster) {
      Cluster cluster = (Cluster) marcador;
      increaseZoomTo(new LatLng(cluster.getLatitude(), cluster.getLongitude()));
    }
    return false;
  }

  @Override protected String getScreenName() {
    return getString(R.string.explore);
  }

  @Override public void onConnectionFailed(ConnectionResult connectionResult) {

  }

  protected synchronized void buildGoogleApiClient() {
    try {
      locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
      boolean gpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
      if (gpsEnabled) {
        googleApiClient = new GoogleApiClient.Builder(getActivity()).addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
      }
    } catch (Exception ex) {
      Log.e("TAG", "GPS Error : " + ex.getLocalizedMessage());
    }
  }

  @UiThread void fontFace() {
    botaoFiltrar.setTypeface(FontUtils.getRegular(getActivity()));
    autocomplete.setTypeface(FontUtils.getRegular(getActivity()));
  }

  @AfterViews void init() {
    try {
      loginService = new LoginService();
      toast = new ToastHelper();
      fontFace();
      SupportMapFragment fragment =
              (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
      if (checkPlayServices()) {
        buildGoogleApiClient();
        fragment.getMapAsync(this);
      } else {
        toast.show(getActivity(), "Necessitamos saber da sua localização. Por favor, autorize nas configurações do seu aparelho.", Toast.LENGTH_SHORT);
      }
      boolean showLogo = getResources().getBoolean(R.bool.show_logo_header);
      if (showLogo) {
        logo_header.setVisibility(View.VISIBLE);
      }
      busca = PreferenceUtils.obterBuscaExplore(getActivity());
      if (busca == null) {
        busca = new BuscaExplore();
        List<CategoriaRelato> categorias =
                new CategoriaRelatoService().getCategorias(getActivity());
        for (CategoriaRelato categoria : categorias) {
          busca.getIdsCategoriaRelato().add(categoria.getId());
          for (CategoriaRelato sub : categoria.getSubcategorias()) {
            busca.getIdsCategoriaRelato().add(sub.getId());
          }
        }
      }
      autocomplete.setAdapter(
              new PlacesAutoCompleteAdapter(getActivity(), R.layout.autocomplete_list_item,
                      ExploreFragment.class));
      autocomplete.setOnItemClickListener(this);
      autocomplete.setOnEditorActionListener(new TextView.OnEditorActionListener() {
        @Override public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
          boolean handled = false;
          if (actionId == EditorInfo.IME_ACTION_SEARCH) {
            searchTask(v.getText().toString());
            ViewUtils.hideKeyboard(getActivity(), v);
            handled = true;
          }
          return handled;
        }
      });
      getActivity().getWindow()
              .setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
      if (loginService.usuarioLogado(getActivity())) {
        timerTask();
      }
      buildGoogleApiClient();
    } catch (Exception e) {
      Log.e("ZUP", e.getMessage(), e);
      Crashlytics.logException(e);
    }
  }

  @UiThread void mapSettings() {
    if (map == null) {
      return;
    }
    map.setMyLocationEnabled(true);
    map.getUiSettings().setMyLocationButtonEnabled(false);
    map.getUiSettings().setZoomControlsEnabled(false);
    map.setOnInfoWindowClickListener(this);
    map.setOnCameraChangeListener(this);
    map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
    map.setOnMarkerClickListener(this);
    CameraPosition p = new CameraPosition.Builder().target(
            new LatLng(Constantes.INITIAL_LATITUDE, Constantes.INITIAL_LONGITUDE)).zoom(12).build();
    CameraUpdate update = CameraUpdateFactory.newCameraPosition(p);
    map.moveCamera(update);
  }

  @Override public void onResume() {
    super.onResume();
    if (googleApiClient != null) {
      googleApiClient.connect();
    }
  }

  @Override public void onPause() {
    super.onPause();
    if (googleApiClient != null) {
      googleApiClient.disconnect();
    }
  }

  @Override public void onInfoWindowClick(Marker marker) {
    Intent intent = null;
    Object marcador = marcadores.get(marker);
    if (marcador instanceof ItemInventario) {
      intent = new Intent(getActivity(), DetalheMapaActivity.class);
      intent.putExtra("item", (ItemInventario) marcador);
    } else if (marcador instanceof ItemRelato) {
      ItemRelato ir = (ItemRelato) marcador;
      SolicitacaoListItem item = new SolicitacaoListItem();
      item.setTitulo(marker.getTitle());
      item.setComentario(ir.getDescricao());
      item.setData(ir.getData());
      item.setId(ir.getId());
      item.setEndereco(ir.getEndereco());
      item.setFotos(ir.getFotos());
      item.setProtocolo(ir.getProtocolo());
      item.setStatus(new SolicitacaoListItem.Status());
      item.setLatitude(ir.getLatitude());
      item.setLongitude(ir.getLongitude());
      item.setCategoria(ir.getCategoria());
      item.setReferencia(ir.getReferencia());
      for (CategoriaRelato.Status status : ir.getCategoria().getStatus()) {
        if (status.getId() == ir.getIdStatus()) {
          item.setStatus(new SolicitacaoListItem.Status(status.getNome(), status.getCor()));
        }
      }
      intent = new Intent(getActivity(), SolicitacaoDetalheActivity.class);
      intent.putExtra("solicitacao", item);
    }
    startActivity(intent);
  }

  @UiThread void increaseZoomTo(LatLng position) {
    if (map == null) {
      return;
    }
    CameraPosition p = new CameraPosition.Builder().target(position)
            .zoom(map.getCameraPosition().zoom + 0.5f)
            .build();
    CameraUpdate update = CameraUpdateFactory.newCameraPosition(p);
    map.animateCamera(update);
  }

  @UiThread public void addMarkerInventory(ItemInventario item) {
    if (map == null) {
      return;
    }
    try {
      itens.add(item);
      if (!isInsideFilter(item.getCategoria())) {
        return;
      }
      LatLng latLong = new LatLng(item.getLatitude(), item.getLongitude());
      String markerToInventory = "";
      if (item.getCategoria().isShowMarker()) {
        markerToInventory = item.getCategoria().getMarcador();
      } else {
        markerToInventory = item.getCategoria().getPin();
      }
      Bitmap marker = BitmapUtils.getInventoryMarker(getActivity(), markerToInventory);
      BitmapDescriptor bdf = BitmapDescriptorFactory.fromBitmap(marker);
      MarkerOptions optionsMarker = new MarkerOptions();
      optionsMarker.position(latLong);
      optionsMarker.icon(bdf);
      optionsMarker.title(item.getCategoria().getNome());
      marcadores.put(map.addMarker(optionsMarker), item);
    } catch (Exception e) {
      Log.e("ZUP", e.getMessage(), e);
      Crashlytics.logException(e);
    }
  }

  @UiThread public void addMarkerReport(ItemRelato item) {
    if (map == null || item == null) {
      return;
    }
    try {
      itens.add(item);
      if (!isInsideFilter(item.getCategoria())) {
        return;
      }
      LatLng latLong = new LatLng(item.getLatitude(), item.getLongitude());
      String markerFromCategory = item.getCategoria().getMarcador();
      Bitmap marker = BitmapUtils.getReportMarker(getActivity(), markerFromCategory);
      BitmapDescriptor bdf = BitmapDescriptorFactory.fromBitmap(marker);
      MarkerOptions optionsMarker = new MarkerOptions();
      optionsMarker.position(latLong);
      optionsMarker.icon(bdf);
      optionsMarker.title(item.getCategoria().getNome());
      marcadores.put(map.addMarker(optionsMarker), item);
    } catch (Exception e) {
      Log.e("ZUP", e.getMessage(), e);
      Crashlytics.logException(e);
    }
  }

  @UiThread public void addMarkerCluster(Cluster item) {
    try {
      validateCluster(item);
      LatLng latLong = new LatLng(item.getLatitude(), item.getLongitude());
      BitmapDescriptor bdf = BitmapDescriptorFactory.fromBitmap(
              MapUtils.createMarker(getActivity(), getClusterColor(item), item.getCount()));
      MarkerOptions optionsMarker = new MarkerOptions();
      optionsMarker.position(latLong);
      optionsMarker.icon(bdf);
      itens.add(item);
      marcadores.put(map.addMarker(optionsMarker), item);
    } catch (Exception e) {
      Log.e("ZUP", e.getMessage(), e);
      Crashlytics.logException(e);
    }
  }

  @UiThread void validateCluster(Cluster item) {
    for (Long id : item.getCategoriesIds()) {
      removeIfContained(id);
    }
  }

  @UiThread void removeIfContained(long id) {
    try {
      for (Object item : itens) {
        if (item instanceof ItemInventario) {
          if (((ItemInventario) item).getId() == id) {
            itens.remove(item);
            Marker marker = getKeyFromValue(marcadores, item);
            marcadores.remove(marker);
            marker.remove();
            break;
          }
        }
        if (item instanceof ItemRelato) {
          if (((ItemRelato) item).getId() == id) {
            itens.remove(item);
            Marker marker = getKeyFromValue(marcadores, item);
            marcadores.remove(marker);
            marker.remove();
            break;
          }
        }
      }
    } catch (Exception e) {
      Log.e("ZUP", e.getMessage(), e);
      Crashlytics.logException(e);
    }
  }

  private int getClusterColor(Cluster item) {
    if (!item.isSingleCategory()) return Color.parseColor("#2ab4db");

    if (item.isReport()) {
      return Color.parseColor(
              new CategoriaRelatoService().getById(getActivity(), item.getCategoryId()).getCor());
    } else {
      return Color.parseColor(
              new CategoriaInventarioService().getById(getActivity(), item.getCategoryId()).getCor());
    }
  }

  @Override public void onStop() {
    super.onStop();
    PreferenceUtils.salvarBusca(getActivity(), busca);
  }

  @Override public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
    geocoderTask((Place) adapterView.getItemAtPosition(i));
    ViewUtils.hideKeyboard(getActivity(), autocomplete);
  }

  @UiThread @Override public void onCameraChange(CameraPosition cameraPosition) {
    latitude = cameraPosition.target.latitude;
    longitude = cameraPosition.target.longitude;
    raio = GeoUtils.getVisibleRadius(map);
    if (zoom != cameraPosition.zoom) {
      removerTodosItensMapa();
    }
    exibirElementos();
    zoom = cameraPosition.zoom;
    refresh();
  }

  @UiThread void exibirElementos() {
    try {
      for (Object pontoMapa : itens) {
        if (pontoMapa instanceof ItemRelato) {
          ItemRelato item = (ItemRelato) pontoMapa;
          if (busca.getIdsCategoriaRelato().contains(item.getId())) {
            addMarkerReport(item);
          }
        } else if (pontoMapa instanceof ItemInventario) {
          ItemInventario item = (ItemInventario) pontoMapa;
          if (busca.getIdsCategoriaInventario().contains(item.getId())) {
            addMarkerInventory(item);
          }
        }
      }
    } catch (Exception e) {
      Log.e("ZUP", e.getMessage(), e);
      Crashlytics.logException(e);
    }
  }

  @UiThread void removerItensMapa() {
    if (map == null) {
      return;
    }
    try {
      Iterator<Marker> it = marcadores.keySet().iterator();
      while (it.hasNext()) {
        Marker marker = it.next();
        if (!GeoUtils.isVisible(map.getProjection().getVisibleRegion(), marker.getPosition())) {
          marker.remove();
          marcadores.remove(marker);
        }
      }
      map.clear();
    } catch (Exception e) {
      Log.e("ZUP", e.getMessage(), e);
      Crashlytics.logException(e);
    }
  }

  @UiThread void removerTodosItensMapa() {
    map.clear();
    marcadores.clear();
  }

  @UiThread void applyFilter(BuscaExplore busca) {
    this.busca = busca;
    removerTodosItensMapa();
    exibirElementos();
    refresh();
  }

  @UiThread void refresh() {
    requestModel = new RequestModel(latitude, longitude, raio);
  }

  @Background void timerTask() {
    boolean run = true;
    while (run) {
      try {
        Thread.sleep(400);
        if (requestModel != null) {
          if (markerRetriever != null) {
            markerRetriever.cancel(true);
          }
          markerRetriever = new MarkerRetriever(requestModel, loading, busca, getActivity(), this);
          if (Build.VERSION.SDK_INT > Build.VERSION_CODES.GINGERBREAD_MR1) {
            markerRetriever.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
          } else {
            markerRetriever.execute();
          }
          requestModel = null;
        }
      } catch (Exception e) {
        Log.e("ZUP", e.getMessage(), e);
        Crashlytics.logException(e);
      }
    }
  }

  private boolean isInsideFilter(CategoriaRelato categoria) {
    return busca != null && busca.getIdsCategoriaRelato() != null && busca.getIdsCategoriaRelato()
            .contains(categoria.getId());
  }

  private boolean isInsideFilter(CategoriaInventario categoria) {
    return busca != null
            && busca.getIdsCategoriaInventario() != null
            && busca.getIdsCategoriaInventario().contains(categoria.getId());
  }

  @Background void geocoderTask(Place place) {
    try {
      Address addr = GeoUtils.getFromPlace(place);
      if (addr == null) {
        return;
      }
      if (pontoBusca != null) {
        removePoint();
      }
      updateCameraWithMarkerb(addr);
    } catch (Exception e) {
      Log.e("ZUP", e.getMessage(), e);
      Crashlytics.logException(e);
    }
  }

  @UiThread void removePoint() {
    pontoBusca.remove();
  }

  @Background void searchTask(String param) {
    try {
      Address addr = GeoUtils.search(param, latitude, longitude);
      if (addr == null) {
        return;
      }
      if (pontoBusca != null) {
        removePoint();
      }
      updateCamera(addr);
    } catch (Exception e) {
      Log.e("ZUP", e.getMessage(), e);
      Crashlytics.logException(e);
    }
  }

  @UiThread void updateCamera(Address addr) {
    if (map == null) {
      return;
    }
    LatLng latLong = new LatLng(addr.getLatitude(), addr.getLongitude());
    CameraPosition p = new CameraPosition.Builder().target(latLong).zoom(15).build();
    CameraUpdate update = CameraUpdateFactory.newCameraPosition(p);
    map.animateCamera(update);
  }

  @UiThread void updateCameraWithMarkerb(Address addr) {
    if (map == null) {
      return;
    }
    LatLng latLong = new LatLng(addr.getLatitude(), addr.getLongitude());
    pontoBusca = map.addMarker(
            new MarkerOptions().position(latLong).icon(BitmapDescriptorFactory.defaultMarker()));
    CameraPosition p = new CameraPosition.Builder().target(latLong).zoom(15).build();
    CameraUpdate update = CameraUpdateFactory.newCameraPosition(p);
    map.animateCamera(update);
  }

  public Marker getKeyFromValue(Map<Marker, Object> map, Object value) {
    if (map == null) {
      return null;
    }
    for (Marker m : map.keySet()) {
      if (map.get(m).equals(value)) {
        return m;
      }
    }
    return null;
  }

  @Override public void onMapReady(GoogleMap googleMap) {
    map = googleMap;
    mapSettings();
  }
}
