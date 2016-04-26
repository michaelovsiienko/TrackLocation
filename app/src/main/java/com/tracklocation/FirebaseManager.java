package com.tracklocation;

import android.content.Context;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by kompot on 26.04.2016.
 */
public class FirebaseManager {
    private Context mContext;
    private Firebase mFirebaseRef;
    private DataSnapshot mDataSnapshot;

    FirebaseManager() {
        Firebase.setAndroidContext(mContext);
        mFirebaseRef = new Firebase(Constants.DATABASE_URL);
    }

    public DataSnapshot getDataSnapshot() {
        mFirebaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                mDataSnapshot = dataSnapshot;
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });
        Singleton.getInstance().setDataSnapshot(mDataSnapshot);
        return mDataSnapshot;
    }

    public boolean userExists(String numberPhone) {
        return mDataSnapshot.child(numberPhone).exists();
    }

    public List<String> getUserGroups(String numberPhone) {
        List<String> usersGroups = new ArrayList<>();
        usersGroups.clear();
        DataSnapshot dataSnapshot1 = (DataSnapshot) mDataSnapshot.child(numberPhone).child(Constants.GROUPS);
        for (DataSnapshot child : dataSnapshot1.getChildren())
            usersGroups.add(child.getValue().toString());
        Singleton.getInstance().setUsersGroups(usersGroups);
        return usersGroups;
    }
    public List<String> getUserFriendListGroup (String numberPhone){
        List<String> mUsersFriendListGroups = new ArrayList<>();
        mUsersFriendListGroups.clear();
        DataSnapshot dataSnapshot = (DataSnapshot) mDataSnapshot.child(numberPhone).child(Constants.FRIENDS);
        for (DataSnapshot child : dataSnapshot.getChildren())
            mUsersFriendListGroups.add(child
                    .child(Constants.GROUP).getValue().toString());
        Singleton.getInstance().setmUserFriendListGroup(mUsersFriendListGroups);
        return  mUsersFriendListGroups;
    }
    public List<String> getUserFriendList (String numberPhone){
        List<String> mUsersFriendList = new ArrayList<>();
        mUsersFriendList.clear();
        DataSnapshot dataSnapshot = (DataSnapshot) mDataSnapshot.child(numberPhone).child(Constants.FRIENDS);
        for (DataSnapshot child : dataSnapshot.getChildren())
            mUsersFriendList.add(child.getKey().toString());
        return mUsersFriendList;
    }

}
