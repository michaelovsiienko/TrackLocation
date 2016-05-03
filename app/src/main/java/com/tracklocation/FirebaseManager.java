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

    FirebaseManager(Context context) {
        mContext = context;
        Firebase.setAndroidContext(mContext);
        mFirebaseRef = new Firebase(Constants.DATABASE_URL);
    }

    public DataSnapshot getDataSnapshot() {
        mFirebaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot != null)
                    mDataSnapshot = dataSnapshot;
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });
        if (mDataSnapshot != null)
            Singleton.getInstance().setDataSnapshot(mDataSnapshot);
        return mDataSnapshot;
    }

    private boolean flag;

    public boolean userExists(final String numberPhone) {
        flag = true;
        mFirebaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                flag = mDataSnapshot.child(numberPhone).exists();
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });
        return flag;
    }

    public List<String> getUserGroups(final String numberPhone) {
        final List<String> usersGroups = new ArrayList<>();

        mFirebaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                usersGroups.clear();
                mDataSnapshot = dataSnapshot;
                DataSnapshot dataSnapshot1 = (DataSnapshot) mDataSnapshot.child(numberPhone).child(Constants.GROUPS);
                for (DataSnapshot child : dataSnapshot1.getChildren())
                    usersGroups.add(child.getValue().toString());
                Singleton.getInstance().setUsersGroups(usersGroups);
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });

        return usersGroups;
    }

    public List<String> getUserFriendListGroup(final String numberPhone) {
        final List<String> mUsersFriendListGroups = new ArrayList<>();

        mFirebaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                mDataSnapshot = dataSnapshot;
                mUsersFriendListGroups.clear();
                DataSnapshot dataSnapshot1 = (DataSnapshot) mDataSnapshot.child(numberPhone).child(Constants.FRIENDS);
                for (DataSnapshot child : dataSnapshot1.getChildren())
                    mUsersFriendListGroups.add(child
                            .child(Constants.GROUP).getValue().toString());
                Singleton.getInstance().setmUserFriendListGroup(mUsersFriendListGroups);
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });

        return mUsersFriendListGroups;
    }

    public List<String> getUserFriendList(final String numberPhone) {
        final List<String> mUsersFriendList = new ArrayList<>();

        mFirebaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                mUsersFriendList.clear();
                mDataSnapshot = dataSnapshot;
                DataSnapshot dataSnapshot1 = (DataSnapshot) mDataSnapshot.child(numberPhone).child(Constants.FRIENDS);
                for (DataSnapshot child : dataSnapshot1.getChildren())
                    mUsersFriendList.add(child.getKey().toString());
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });

        return mUsersFriendList;
    }

}
