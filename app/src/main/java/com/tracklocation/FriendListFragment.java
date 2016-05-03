package com.tracklocation;

import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ExpandableListView;
import android.widget.TextView;

import com.tracklocation.interfaces.OnShowFriendsListener;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by kompot on 26.04.2016.
 */
public class FriendListFragment extends Fragment implements ExpandableListView.OnChildClickListener {
    public static OnShowFriendsListener mOnShowFriendsListener;

    private String mPhoneNumberArgument;
    private List<String> mUsersGroups;
    private List<String> mUsersFriendList;
    private List<String> mUsersFriendListGroups;

    private List<String> mSelectedUsers;

    private ExpandableListView mExpandableListView;
    public static ExpandableList sExpandableList;
    public static ExpListAdapter sExpandableListAdapter;

    private NavigationView mNavigationView;

    public static FriendListFragment newInstance(String phoneNumber, List<String> usersGroups, List<String> usersFriendList, List<String> usersFriendListGroups) {
        FriendListFragment fragment = new FriendListFragment();
        Bundle arguments = new Bundle();
        arguments.putString(Constants.PHONE_NUM_ARG, phoneNumber);
        arguments.putStringArrayList(Constants.GROUPS, (ArrayList<String>) usersGroups);
        arguments.putStringArrayList(Constants.USERS_FRIEND, (ArrayList<String>) usersFriendList);
        arguments.putStringArrayList(Constants.USERS_FRIEND_GROUPS, (ArrayList<String>) usersFriendListGroups);
        fragment.setArguments(arguments);
        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnShowFriendsListener) {
            mOnShowFriendsListener = (OnShowFriendsListener) context;
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSelectedUsers = new ArrayList<>();
        mNavigationView = (NavigationView) getActivity().findViewById(R.id.navigation_view);
        mPhoneNumberArgument = "";
        if (getArguments() != null) {
            mPhoneNumberArgument = getArguments().getString(Constants.PHONE_NUM_ARG);
            mUsersGroups = getArguments().getStringArrayList(Constants.GROUPS);
            mUsersFriendList = getArguments().getStringArrayList(Constants.USERS_FRIEND);
            mUsersFriendListGroups = getArguments().getStringArrayList(Constants.USERS_FRIEND_GROUPS);
        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        View view = inflater.inflate(R.layout.fragment_friendslist, container, false);
        mExpandableListView = (ExpandableListView) view.findViewById(R.id.expandable_listview);

        sExpandableList = new ExpandableList(mUsersGroups);
        for (int i = 0; i < mUsersFriendList.size(); i++) {
            sExpandableList.addContactToGroup(mUsersFriendListGroups.get(i), mUsersFriendList.get(i));
        }
        sExpandableListAdapter = new ExpListAdapter(getActivity(), sExpandableList.getGroupInformation());
        mExpandableListView.setAdapter(sExpandableListAdapter);
        mExpandableListView.setOnChildClickListener(this);
        setGroupIndicatorToRight();
        Button showFriends = (Button) view.findViewById(R.id.onShowFriendsButton);
        Singleton.getInstance().getSelectedUsers().clear();
        showFriends.setText(getResources().getString(R.string.review));
        showFriends.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSelectedUsers = Singleton.getInstance().getSelectedUsers();
                if (mSelectedUsers.size() != 0) {
                    ((MainActivity) getActivity()).reloadMap();
                    getActivity().getSupportFragmentManager().popBackStack();
                    mNavigationView.getMenu().getItem(0).setChecked(true);
                }
            }
        });
        return view;
    }

    @Override
    public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {

        CheckBox childCheckBox = (CheckBox) v.findViewById(R.id.childViewCheckBox);
        TextView text = (TextView) v.findViewById(R.id.childTextView);
        if (childCheckBox.getVisibility() == View.VISIBLE) {
            if (!childCheckBox.isChecked()) {
                childCheckBox.setChecked(true);
            } else {
                childCheckBox.setChecked(false);
            }

        } else {
            IncorrectPasswordFragment incorrectPasswordFragment =
                    IncorrectPasswordFragment.newInstance(text.getText().toString(), mPhoneNumberArgument);
            incorrectPasswordFragment.show(getActivity().getFragmentManager().beginTransaction(), "dialog");
        }
        return false;
    }


    private void setGroupIndicatorToRight() {
        DisplayMetrics dm = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(dm);
        int width = dm.widthPixels;
        mExpandableListView.setIndicatorBounds(width - getDipsFromPixel(35), width - getDipsFromPixel(5));
    }

    public int getDipsFromPixel(float pixels) {
        final float scale = getResources().getDisplayMetrics().density;
        return (int) (pixels * scale + 0.5f);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.menu_addgroup, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.add_menu_item_group) {
            AddGroupFragment addGroupFragment = AddGroupFragment.newInstance(mPhoneNumberArgument);
            addGroupFragment.show(getActivity().getFragmentManager().beginTransaction(), "dialog");
        }
        return false;
    }
}
