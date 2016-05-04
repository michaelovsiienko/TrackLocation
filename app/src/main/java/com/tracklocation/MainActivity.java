package com.tracklocation;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
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
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, OnMapReadyCallback, LocationListener, View.OnClickListener, AdapterView.OnItemClickListener {
    private NavigationView mNavigationView;
    private TextView mNavigationViewHeaderNickname;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
        mAutoCompleteTextView.setThreshold(4);
        mAutoCompleteTextView.setAdapter(new GooglePlacesAutocompleteAdapter(this, R.layout.maps_places_listview));
        mAutoCompleteTextView.setOnItemClickListener(this);

    }

    public void onResume() {
        super.onResume();
        if (mUserPhoneNumber != null) {
            mUserGroups = mFirebaseManager.getUserGroups(mUserPhoneNumber);
            mUserFriendList = mFirebaseManager.getUserFriendList(mUserPhoneNumber);
            Singleton.getInstance().setmUserFriendList(mUserFriendList);
            mUserFriendListGroup = mFirebaseManager.getUserFriendListGroup(mUserPhoneNumber);
            mMapFragment.getMapAsync(this);
            mFirebaseRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot != null) {
                        mDataSnapshot = dataSnapshot;
                        mNavigationViewHeaderPassword.setText(getResources().getString(R.string.header_password));
                        mNavigationViewHeaderNumber.setText(mUserPhoneNumber);
                        mNavigationViewHeaderNickname.setText(dataSnapshot.child(mUserPhoneNumber)
                                .child(Constants.NICKNAME).getValue().toString());
                    }
                }

                @Override
                public void onCancelled(FirebaseError firebaseError) {
                }
            });
            if (ActivityCompat.checkSelfPermission(
                    getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                    &&
                    ActivityCompat.checkSelfPermission(
                            getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 10, this);
            }
        }
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
        mMapFragment.getMapAsync(this);
        if (mTrackMyLocation.isChecked()) {
            mFirebaseRef.child(mUserPhoneNumber).child("first").setValue(location.getLatitude());
            mFirebaseRef.child(mUserPhoneNumber).child("second").setValue(location.getLongitude());
        }
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
                findViewById(R.id.autocompletetextview).setVisibility(View.VISIBLE);
                mMenu.findItem(R.id.add_menu_item).setVisible(false);
                mMenu.findItem(R.id.clear).setVisible(true);
                break;
            case R.id.clear:
                findViewById(R.id.autocompletetextview).setVisibility(View.GONE);
                mMenu.findItem(R.id.add_menu_item).setVisible(true);
                mMenu.findItem(R.id.clear).setVisible(false);

        }
        return super.onOptionsItemSelected(item);
    }

    public static ArrayList<String> autocomplete(String input) {
        ArrayList<String> resultList = null;

        HttpURLConnection conn = null;
        StringBuilder jsonResults = new StringBuilder();
        try {
            StringBuilder sb = new StringBuilder(Constants.PLACES_API_BASE + Constants.TYPE_AUTOCOMPLETE + Constants.OUT_JSON);
            sb.append("?key=" + Constants.API_KEY);
            sb.append("&input=" + URLEncoder.encode(input, "utf8"));

            URL url = new URL(sb.toString());

            System.out.println("URL: " + url);
            conn = (HttpURLConnection) url.openConnection();
            InputStreamReader in = new InputStreamReader(conn.getInputStream());

            // Load the results into a StringBuilder
            int read;
            char[] buff = new char[1024];
            while ((read = in.read(buff)) != -1) {
                jsonResults.append(buff, 0, read);
            }
        } catch (MalformedURLException e) {
            Log.e(Constants.LOG_TAG, "Error processing Places API URL", e);
            return resultList;
        } catch (IOException e) {
            Log.e(Constants.LOG_TAG, "Error connecting to Places API", e);
            return resultList;
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }

        try {

            // Create a JSON object hierarchy from the results
            JSONObject jsonObj = new JSONObject(jsonResults.toString());
            JSONArray predsJsonArray = jsonObj.getJSONArray("predictions");

            // Extract the Place descriptions from the results
            resultList = new ArrayList<String>(predsJsonArray.length());
            for (int i = 0; i < predsJsonArray.length(); i++) {
                System.out.println(predsJsonArray.getJSONObject(i).getString("description"));
                System.out.println("============================================================");
                resultList.add(predsJsonArray.getJSONObject(i).getString("description"));
            }
        } catch (JSONException e) {
            Log.e(Constants.LOG_TAG, "Cannot process JSON results", e);
        }

        return resultList;
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
        mNavigationViewHeaderNickname = (TextView) mNavigationViewHeaderView.findViewById(R.id.textViewHeaderNickName);
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
        String str = (String) parent.getItemAtPosition(position);
        Toast.makeText(this, str, Toast.LENGTH_SHORT).show();
    }
}
