package com.tracklocation;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStates;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.places.AutocompletePrediction;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.net.InetAddress;
import java.util.List;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, OnMapReadyCallback, LocationListener, View.OnClickListener, AdapterView.OnItemClickListener, GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks {
    private NavigationView mNavigationView;
    private TextView mNavigationViewHeaderNumber;
    private TextView mNavigationViewHeaderPassword;
    private ImageButton mNavigationViewHeaderResetPassword;
    private View mNavigationViewHeaderView;
    private AutoCompleteTextView mAutoCompleteTextView;

    private Toolbar mToolbar;
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;

    private SharedPreferences mSharedPreferences;

    private String mUserPhoneNumber;
    private FirebaseManager mFirebaseManager;
    private Firebase mFirebaseRef;
    public static DataSnapshot mDataSnapshot;

    private List<String> mUserGroups;
    private List<String> mUserFriendList;
    private List<String> mUserFriendListGroup;

    private SupportMapFragment mMapFragment;
    private Location mMyLocation;
    private LocationManager mLocationManager;
    private ToggleButton mTrackMyLocation;
    private GoogleMap mGoogleMap;
    private GoogleApiClient mGoogleApiClient;
    private GooglePlacesAutocompleteAdapter mAdapter;
    private LocationRequest mLocationRequest;
    private PendingResult<LocationSettingsResult> result;
    private String mBestProvider;
    private ProgressDialog mProgressDialog;

    final static int REQUEST_LOCATION = 199;
    private boolean mStopThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!isInternetAvailable())
            Toast.makeText(this, getString(R.string.internet_info), Toast.LENGTH_LONG).show();
        if (isNeedLogin()) {
            startActivityForResult(new Intent(this, Login_activity.class), 1);
        } else {
            mSharedPreferences = getPreferences(MODE_PRIVATE);
            mUserPhoneNumber = mSharedPreferences.getString("number", "");
            Singleton.getInstance().setUserPhone(mUserPhoneNumber);
        }

        setContentView(R.layout.activity_main);
        Firebase.setAndroidContext(getApplicationContext());
        initializeNavigationView();
        initializeToolbar();
        initializeDrawerLayout();
        mFirebaseManager = new FirebaseManager(getApplicationContext());
        mFirebaseRef = new Firebase(Constants.DATABASE_URL);
        mMapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.GoogleMapFragment);
        mLocationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        mAutoCompleteTextView = (AutoCompleteTextView) findViewById(R.id.autocompletetextview);


        mGoogleApiClient = new GoogleApiClient
                .Builder(this)
                .addApi(Places.GEO_DATA_API)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addApi(Places.PLACE_DETECTION_API)
                .build();
        mGoogleApiClient.connect();

        mAutoCompleteTextView.setThreshold(4);
        mAdapter = new GooglePlacesAutocompleteAdapter(this, mGoogleApiClient, null, null);
        mAutoCompleteTextView.setAdapter(mAdapter);
        mAutoCompleteTextView.setOnItemClickListener(this);
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setTitle(getString(R.string.loading_tittle));
        mProgressDialog.setMessage(getString(R.string.loading_message));
        mProgressDialog.setCancelable(false);
        mStopThread = false;

    }

    @Override
    public void onResume() {
        super.onResume();
        if (mUserPhoneNumber != null) {
            mFirebaseRef.child(mUserPhoneNumber).child(Constants.STATUS).setValue("online");
            mUserGroups = mFirebaseManager.getUserGroups(mUserPhoneNumber);
            mUserFriendList = mFirebaseManager.getUserFriendList(mUserPhoneNumber);
            Singleton.getInstance().setmUserFriendList(mUserFriendList);
            mUserFriendListGroup = mFirebaseManager.getUserFriendListGroup(mUserPhoneNumber);
            Criteria crit = new Criteria();
            crit.setAccuracy(Criteria.ACCURACY_FINE);
            mBestProvider = mLocationManager.getBestProvider(crit, false);
            if (checkPermission()) {
                mLocationManager.requestLocationUpdates(mBestProvider, 1000, 1, this);
                if (mTrackMyLocation.isChecked() && mLocationManager.getLastKnownLocation(mBestProvider) != null) {
                    mFirebaseRef.child(mUserPhoneNumber).child("first")
                            .setValue(mLocationManager.getLastKnownLocation(mBestProvider).getLatitude());
                    mFirebaseRef.child(mUserPhoneNumber).child("second")
                            .setValue(mLocationManager.getLastKnownLocation(mBestProvider).getLongitude());
                }
            }
            mProgressDialog.show();
            new Thread(new Runnable() {
                @Override
                public void run() {
                    while (!mStopThread) ;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (mProgressDialog.isShowing())
                                mProgressDialog.dismiss();
                        }
                    });
                }
            }).start();

            mMapFragment.getMapAsync(this);
            mFirebaseRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    mDataSnapshot = dataSnapshot;
                    mNavigationViewHeaderPassword.setText(getResources().getString(R.string.header_password));
                    mNavigationViewHeaderNumber.setText(mUserPhoneNumber);

                    mStopThread = true;

                }

                @Override
                public void onCancelled(FirebaseError firebaseError) {
                }
            });


        }

    }

    @Override
    public void onStop() {
        if (mUserPhoneNumber != null)
            mFirebaseRef.child(mUserPhoneNumber).child(Constants.STATUS).setValue("offline");
        super.onStop();
    }

    public void reloadMap() {
        mMapFragment.getMapAsync(this);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem menuItem) {

        mDrawerLayout.closeDrawer(GravityCompat.START);
        switch (menuItem.getItemId()) {
            case R.id.navigation_item_contacts:
                Singleton.getInstance().setUserPhone(mUserPhoneNumber);
                prepareFragment(FriendListFragment.newInstance(mUserPhoneNumber
                        , mUserGroups
                        , mUserFriendList
                        , mUserFriendListGroup));
                break;
            case R.id.navigation_item_home:
                getSupportFragmentManager().popBackStack();
                break;
            case R.id.navigation_item_logout:
                mSharedPreferences = getPreferences(MODE_PRIVATE);
                SharedPreferences.Editor editor = mSharedPreferences.edit();
                editor.putBoolean("login", true);
                editor.putString("number", "");
                editor.apply();
                Singleton.getInstance().clearAll();
                if (mGoogleMap != null)
                    mGoogleMap.clear();
                startActivityForResult(new Intent(this, Login_activity.class), 1);
                break;
        }

        return true;
    }

    private boolean checkPermission() {

        return !(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED);
    }

    @Override
    public void onMapReady(final GoogleMap googleMap) {
        mGoogleMap = googleMap;
        if (checkPermission()) {
            mGoogleMap.setMyLocationEnabled(true);
            mGoogleMap.getUiSettings().setMyLocationButtonEnabled(true);
        }
        mGoogleMap.getUiSettings().setZoomControlsEnabled(true);
        mGoogleMap.getUiSettings().setCompassEnabled(true);

        if (Singleton.getInstance().getSelectedUsers() != null)
            mFirebaseRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    List<String> mSelectedUsers = Singleton.getInstance().getSelectedUsers();
                    mGoogleMap.clear();
                    for (int i = 0; i < mSelectedUsers.size(); i++) {

                        mGoogleMap.addMarker(new MarkerOptions()
                                .position(new LatLng(getUserLocation(mSelectedUsers.get(i), dataSnapshot).getLatitude(), getUserLocation(mSelectedUsers.get(i), dataSnapshot).getLongitude()))
                                .title(mSelectedUsers.get(i)));
                    }
                }

                @Override
                public void onCancelled(FirebaseError firebaseError) {

                }
            });
    }

    @Override
    public void onLocationChanged(Location location) {
        mMyLocation = location;
        if (mTrackMyLocation.isChecked()) {
            mFirebaseRef.child(mUserPhoneNumber).child("first").setValue(location.getLatitude());
            mFirebaseRef.child(mUserPhoneNumber).child("second").setValue(location.getLongitude());
        }
        mMapFragment.getMapAsync(this);

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    @Override
    public void onBackPressed() {
        if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            mDrawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.imageButtonPassword:
                ResetPasswordFragment resetPasswordFragment = ResetPasswordFragment.newInstance();
                resetPasswordFragment.show(getFragmentManager().beginTransaction(), "dialog");
                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_activity_menu, menu);
        mMenu = menu;
        return true;
    }

    private Menu mMenu;

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.add_menu_item:
                AddFriendFragment addFriendFragment = AddFriendFragment.newInstance(mUserPhoneNumber, mUserGroups);
                addFriendFragment.show(getFragmentManager().beginTransaction(), "dialog");
                break;
            case R.id.search:
                mAutoCompleteTextView.setVisibility(View.VISIBLE);
                mAutoCompleteTextView.requestFocus();
                mMenu.findItem(R.id.add_menu_item).setVisible(false);
                mMenu.findItem(R.id.clear).setVisible(true);
                break;
            case R.id.clear:
                mAutoCompleteTextView.setVisibility(View.GONE);
                mAutoCompleteTextView.setText("");
                mMenu.findItem(R.id.add_menu_item).setVisible(true);
                mMenu.findItem(R.id.clear).setVisible(false);
                hideKeyboard(getApplicationContext(), mAutoCompleteTextView);
                break;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK && requestCode == 1) {
            mSharedPreferences = getPreferences(MODE_PRIVATE);
            mUserPhoneNumber = data.getStringExtra("number");
            SharedPreferences.Editor editor = mSharedPreferences.edit();
            editor.putBoolean("login", false);
            editor.putString("number", mUserPhoneNumber);
            editor.apply();
        }
    }

    private void prepareFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.GoogleMapFragment, fragment)
                .addToBackStack(null)
                .commit();
    }

    private boolean isNeedLogin() {
        mSharedPreferences = getPreferences(MODE_PRIVATE);
        return mSharedPreferences.getBoolean("login", true);
    }

    private Location getUserLocation(String userName, DataSnapshot dataSnapshot) {
        Location location = new Location("");
        if (dataSnapshot != null) {
            location.setLatitude(Double.parseDouble(dataSnapshot.child(userName).child("first").getValue().toString()));
            location.setLongitude(Double.parseDouble(dataSnapshot.child(userName).child("second").getValue().toString()));
        }
        return location;
    }

    private void initializeNavigationView() {
        mNavigationView = (NavigationView) findViewById(R.id.navigation_view);
        mNavigationView.setNavigationItemSelectedListener(this);
        mNavigationViewHeaderView = mNavigationView.getHeaderView(0);
        mNavigationViewHeaderNumber = (TextView) mNavigationViewHeaderView.findViewById(R.id.textViewHeaderName);
        mNavigationViewHeaderPassword = (TextView) mNavigationViewHeaderView.findViewById(R.id.textViewHeaderPassword);
        mNavigationViewHeaderResetPassword = (ImageButton) mNavigationViewHeaderView.findViewById(R.id.imageButtonPassword);
        mTrackMyLocation = (ToggleButton) mNavigationViewHeaderView.findViewById(R.id.toggleButton_navigationview);
        mNavigationViewHeaderResetPassword.setOnClickListener(this);
    }

    private void initializeToolbar() {
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
    }

    private void initializeDrawerLayout() {
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, mToolbar, R.string.drawer_open, R.string.drawer_close);
        mDrawerLayout.setDrawerListener(mDrawerToggle);
        mDrawerToggle.syncState();
    }

    @Override
    public void onItemClick(AdapterView parent, View view, int position, long id) {
        mGoogleMap.clear();
        final AutocompletePrediction item = mAdapter.getItem(position);
        final String placeId = item.getPlaceId();
        final CharSequence primaryText = item.getPrimaryText(null);
        Places.GeoDataApi.getPlaceById(mGoogleApiClient, placeId).setResultCallback(new ResultCallback<PlaceBuffer>() {
            @Override
            public void onResult(@NonNull PlaceBuffer places) {
                final Place myPlace = places.get(0);

                if (myPlace.getPlaceTypes().get(0) == Place.TYPE_COUNTRY)
                    mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(myPlace.getLatLng(), 5.0f));
                else if (myPlace.getPlaceTypes().get(0) == Place.TYPE_CITY_HALL)
                    mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(myPlace.getLatLng(), 8.0f));
                else if (myPlace.getPlaceTypes().get(0) == Place.TYPE_CAFE)
                    mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(myPlace.getLatLng(), 14.0f));
                else if (myPlace.getPlaceTypes().get(0) == Place.TYPE_STREET_ADDRESS)
                    mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(myPlace.getLatLng(), 12.0f));
                else
                    mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(myPlace.getLatLng(), 14.0f));
                mGoogleMap.addMarker(new MarkerOptions()
                        .position(myPlace.getLatLng())
                        .title(myPlace.getName().toString())
                );

            }
        });
        hideKeyboard(getApplicationContext(), mAutoCompleteTextView);
        Log.i(Constants.LOG_TAG, "Autocomplete item selected: " + primaryText);
        Log.i(Constants.LOG_TAG, placeId);
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    private void hideKeyboard(Context context, View view) {
        InputMethodManager keyboardManager =
                (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        keyboardManager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(30 * 1000);
        locationRequest.setFastestInterval(5 * 1000);
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);

        builder.setAlwaysShow(true);

        PendingResult<LocationSettingsResult> result =
                LocationServices.SettingsApi.checkLocationSettings(mGoogleApiClient, builder.build());
        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(LocationSettingsResult result) {
                final Status status = result.getStatus();
                final LocationSettingsStates state = result.getLocationSettingsStates();
                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:
                        // All location settings are satisfied. The client can initialize location
                        // requests here.
                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        // Location settings are not satisfied. But could be fixed by showing the user
                        // a dialog.
                        try {
                            // Show the dialog by calling startResolutionForResult(),
                            // and check the result in onActivityResult().
                            status.startResolutionForResult(
                                    MainActivity.this, 1000);
                        } catch (IntentSender.SendIntentException e) {
                            // Ignore the error.
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        // Location settings are not satisfied. However, we have no way to fix the
                        // settings so we won't show the dialog.
                        break;
                }
            }
        });
    }

    @Override
    public void onConnectionSuspended(int i) {
    }

    private boolean isInternetAvailable() {
        try {
            InetAddress ipAddr = InetAddress.getByName("google.com"); //You can replace it with your name

            if (ipAddr.equals("")) {
                return false;
            } else {
                return true;
            }

        } catch (Exception e) {
            return false;
        }

    }
}
