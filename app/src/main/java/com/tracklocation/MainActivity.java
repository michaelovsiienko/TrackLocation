package com.tracklocation;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{
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

    public String mUserPhoneNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (isNeedLogin()) {
            startActivityForResult(new Intent(this, Login_activity.class), 1);
        } else {
            mSharedPreferences = getPreferences(MODE_PRIVATE);
            mUserPhoneNumber = mSharedPreferences.getString("number", "");
        }
        setContentView(R.layout.activity_main);
        initialize_NavigationView();
        initialize_Toolbar();
        initialize_DrawerLayout();
    }

    private void initialize_NavigationView(){
        mNavigationView = (NavigationView)findViewById(R.id.navigation_view);
        mNavigationView.setNavigationItemSelectedListener(this);
        mNavigationViewHeaderView = mNavigationView.getHeaderView(0);
        mNavigationViewHeaderNickname = (TextView) mNavigationViewHeaderView.findViewById(R.id.textViewHeaderNickName);
        mNavigationViewHeaderNumber = (TextView) mNavigationViewHeaderView.findViewById(R.id.textViewHeaderName);
        mNavigationViewHeaderPassword = (TextView) mNavigationViewHeaderView.findViewById(R.id.textViewHeaderPassword);
        mNavigationViewHeaderResetPassword = (ImageButton) mNavigationViewHeaderView.findViewById(R.id.imageButtonPassword);
    }

    private void initialize_Toolbar () {
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
    }
    private void initialize_DrawerLayout() {
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, mToolbar, R.string.drawer_open, R.string.drawer_close);
        mDrawerLayout.setDrawerListener(mDrawerToggle);
        mDrawerToggle.syncState();
    }


    @Override
    public boolean onNavigationItemSelected(MenuItem menuItem) {

        mDrawerLayout.closeDrawer(GravityCompat.START);
        switch (menuItem.getItemId()) {
            case R.id.navigation_item_contacts:
              prepareFragment(FriendListFragment.newInstance(mUserPhoneNumber,,,getUsersFriendGroup));
                break;
            case R.id.navigation_item_home:
//                prepareFragment(GoogleMapFragment.newInstance(true, mUserPhoneNumber, null));
                break;
            case R.id.navigation_item_logout:
                mSharedPreferences = getPreferences(MODE_PRIVATE);
                SharedPreferences.Editor editor = mSharedPreferences.edit();
                editor.putBoolean("login", true);
                editor.putString("number", "");
                editor.apply();
                startActivityForResult(new Intent(this, Login_activity.class), 1);
                break;
        }

        return true;
    }

    private void prepareFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.GoogleMapFragment, fragment)
                .addToBackStack(null)
                .commit();
    }
    private boolean isNeedLogin() {
        mSharedPreferences = getPreferences(MODE_PRIVATE);
        return mSharedPreferences.getBoolean("login", true);
    }

}
