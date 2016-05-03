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
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.List;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, OnMapReadyCallback, LocationListener, View.OnClickListener {
    private NavigationView mNavigationView;
    private TextView mNavigationViewHeaderNickname;
    private TextView mNavigationViewHeaderNumber;
    private TextView mNavigationViewHeaderPassword;
    private ImageButton mNavigationViewHeaderResetPassword;
    private View mNavigationViewHeaderView;

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
            Singleton.getInstance().setmUserPhone(mUserPhoneNumber);
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
                Singleton.getInstance().setmUserPhone(mUserPhoneNumber);
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
        ResetPasswordFragment resetPasswordFragment = ResetPasswordFragment.newInstance();
        resetPasswordFragment.show(getFragmentManager().beginTransaction(), "dialog");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_activity_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.add_menu_item) {
            AddFriendFragment addFriendFragment = AddFriendFragment.newInstance(mUserPhoneNumber, mUserGroups);
            addFriendFragment.show(getFragmentManager().beginTransaction(), "dialog");
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


}
