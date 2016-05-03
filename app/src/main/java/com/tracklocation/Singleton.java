package com.tracklocation;

import com.firebase.client.DataSnapshot;

import java.util.ArrayList;
import java.util.List;


public class Singleton {
    private static Singleton mInstance = null;
    private List<String> mSelectedUsers = new ArrayList<>();

    private List<String> mUsersGroups = new ArrayList<>();
    private List<String> mUserFriendListGroup = new ArrayList<>();
    private List<String> mUserFriendList = new ArrayList<>();
    private DataSnapshot mDataSnapshot;
    private String mUserPhone = "";

    public static Singleton getInstance() {
        if (mInstance == null) {
            mInstance = new Singleton();
        }
        return mInstance;
    }

    private Singleton() {
    }

    public void setSelectedUsers(List<String> selectedUsers) {
        this.mSelectedUsers = selectedUsers;
    }

    public List<String> getSelectedUsers() {
        return this.mSelectedUsers;
    }

    public void setDataSnapshot(DataSnapshot dataSnapshot) {
        this.mDataSnapshot = dataSnapshot;
    }

    public DataSnapshot getDataSnapshot() {
        return this.mDataSnapshot;
    }

    public void setmUserPhone(String userPhone) {
        this.mUserPhone = userPhone;
    }

    public String getmUserPhone() {
        return this.mUserPhone;
    }

    public void setUsersGroups(List<String> usersGroups) {
        this.mUsersGroups = usersGroups;
    }

    public List<String> getUsersGroups() {
        return this.mUsersGroups;
    }

    public void setmUserFriendListGroup(List<String> userFriendListGroup) {
        this.mUserFriendListGroup = userFriendListGroup;
    }

    public List<String> getmUserFriendListGroup() {
        return this.mUserFriendListGroup;
    }

    public void setmUserFriendList(List<String> userFriendList) {
        this.mUserFriendList = userFriendList;
    }

    public List<String> getmUserFriendList() {
        return this.mUserFriendList;
    }

    public void clearAll() {
        List<String> mSelectedUsers = new ArrayList<>();
        List<String> mUsersGroups = new ArrayList<>();
        List<String> mUserFriendListGroup = new ArrayList<>();
        List<String> mUserFriendList = new ArrayList<>();
    }

}
