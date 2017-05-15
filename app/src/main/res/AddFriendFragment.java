package com.example.mykhail.tracklocationv20;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by kompot on 20.04.2016.
 */
public class AddFriendFragment extends DialogFragment implements DialogInterface.OnClickListener {
    private String mPhoneNumberArgument;
    private Firebase mFirebaseRef;
    private List<String> groups;
    private String password;

    private Spinner mSpinner;
    private View mView;

    private TextInputLayout numberPhoneFriendLayout;
    private TextInputLayout passwordFriendLayout;

    private EditText numberFriend;
    private EditText passwordFriend;

    private AlertDialog mDialog;
    public static com.example.mykhail.tracklocationv20.AddFriendFragment newInstance(String numberPhone, List<String> usersGroups) {
        com.example.mykhail.tracklocationv20.AddFriendFragment addFriendFragment = new com.example.mykhail.tracklocationv20.AddFriendFragment();
        Bundle arguments = new Bundle();
        arguments.putString(Constants.PHONE_NUM_ARG, numberPhone);
        arguments.putStringArrayList(Constants.GROUPS, (ArrayList<String>) usersGroups);
        addFriendFragment.setArguments(arguments);
        return addFriendFragment;
    }

    public Dialog onCreateDialog(Bundle bundle) {
        Firebase.setAndroidContext(getActivity());
        mFirebaseRef = new Firebase(Constants.DATABASE_URL);
        LayoutInflater inflater = getActivity().getLayoutInflater();
        if (getArguments() != null) {
            mPhoneNumberArgument = getArguments().getString(Constants.PHONE_NUM_ARG);
            groups = getArguments().getStringArrayList(Constants.GROUPS);

        }


        mView = inflater.inflate(R.layout.fragment_addfriend, null);

        numberPhoneFriendLayout = (TextInputLayout) mView.findViewById(R.id.namefriend_fragmentAddFriendLayout);
        passwordFriendLayout = (TextInputLayout) mView.findViewById(R.id.passwordfriend_fragmentAddFriendLayout);

        numberFriend = (EditText) mView.findViewById(R.id.namefriend_fragmentAddFriend);
        passwordFriend = (EditText) mView.findViewById(R.id.passwordFriend_fragmentAddFriend);


        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(),
                android.R.layout.simple_spinner_item, groups);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        mSpinner = (Spinner) mView.findViewById(R.id.spinner_fragmentAddFriend);
        mSpinner.setAdapter(adapter);


        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                .setTitle(getResources().getString(R.string.add_friend_tittle))
                .setView(mView)
                .setNegativeButton(R.string.cancel_dialogfragment, this)
                .setPositiveButton(R.string.add_friend, this);

        return builder.create();
    }
    @Override
    public void onResume() {
        super.onResume();
        mDialog = (AlertDialog) getDialog();
        mDialog.getButton(Dialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                com.example.mykhail.tracklocationv20.FirebaseManager firebaseManager = new com.example.mykhail.tracklocationv20.FirebaseManager(getActivity());
                final List<String> userFriends = new ArrayList<String>();
                mFirebaseRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.child(numberFriend.getText().toString()).exists()) {
                            for (DataSnapshot child : dataSnapshot.child(mPhoneNumberArgument).child(Constants.FRIENDS).getChildren()) {
                                userFriends.add(child.getKey().toString());
                            }
                            if (!userFriends.contains(numberFriend.getText().toString())) {
                                String buffer = passwordFriend.getText().toString();
                                if (dataSnapshot != null)
                                    password = dataSnapshot.child(numberFriend.getText().toString()).child(Constants.PASSWORD).getValue().toString();
                                if (password.equals(buffer)) {
                                    mFirebaseRef.child(mPhoneNumberArgument).child(Constants.FRIENDS).child(numberFriend.getText().toString()).child(Constants.GROUP).setValue(mSpinner.getSelectedItem().toString());
                                    mFirebaseRef.child(mPhoneNumberArgument).child(Constants.FRIENDS).child(numberFriend.getText().toString()).child(Constants.PASSWORD).setValue(password);
                                    Toast.makeText(getActivity(), getResources().getString(R.string.succes), Toast.LENGTH_LONG).show();
                                    mDialog.dismiss();
                                } else {
                                    passwordFriendLayout.setHint(getResources().getString(R.string.incorrect_password));
                                }
                            } else
                                Toast.makeText(getActivity(),
                                        getResources().getString(R.string.havefriend),
                                        Toast.LENGTH_SHORT).show();
                        } else
                            numberPhoneFriendLayout.setHint(getResources().getString(R.string.incorrect_number));
                        }

                    @Override
                    public void onCancelled(FirebaseError firebaseError) {

                    }
                });

            }
        });
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
    }

}
