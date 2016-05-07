package com.example.mykhail.tracklocationv20.interfaces;

import android.support.annotation.Nullable;

import java.util.List;

/**
 * Created by kompot on 26.04.2016.
 */
public interface OnShowFriendsListener {
    void onShowFriends(boolean isMyLocation, @Nullable String phoneNumber, @Nullable List<String> selectedUsers);

}
