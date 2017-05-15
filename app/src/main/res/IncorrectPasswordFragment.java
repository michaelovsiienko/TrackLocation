package com.example.mykhail.tracklocationv20;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.firebase.client.Firebase;


public class IncorrectPasswordFragment extends DialogFragment implements DialogInterface.OnClickListener {
    private Firebase mFirebaseRef;
    private String mFriendPhone;
    private String myNumberPhone;
    private EditText mNewPassword;
    private TextInputLayout mInputPasswordLayout;
    private View mView;

    public static com.example.mykhail.tracklocationv20.IncorrectPasswordFragment newInstance(String friendNumberPhone, String myNumberPhone) {
        com.example.mykhail.tracklocationv20.IncorrectPasswordFragment incorrectPasswordFragment = new com.example.mykhail.tracklocationv20.IncorrectPasswordFragment();
        Bundle arguments = new Bundle();
        arguments.putString(Constants.FRIEND_NUMBER, friendNumberPhone);
        arguments.putString(Constants.PHONE_NUM_ARG, myNumberPhone);
        incorrectPasswordFragment.setArguments(arguments);
        return incorrectPasswordFragment;
    }

    public Dialog onCreateDialog(Bundle bundle) {
        Firebase.setAndroidContext(getActivity());
        mFirebaseRef = new Firebase(Constants.DATABASE_URL);
        LayoutInflater layoutInflater = getActivity().getLayoutInflater();
        if (getArguments() != null) {
            mFriendPhone = getArguments().getString(Constants.FRIEND_NUMBER);
            myNumberPhone = getArguments().getString(Constants.PHONE_NUM_ARG);
        }
        mView = layoutInflater.inflate(R.layout.frament_incorrectpassword, null);

        TextView title = (TextView) mView.findViewById(R.id.numberPhone_incorrectPassword);
        mNewPassword = (EditText) mView.findViewById(R.id.passwordFriend_fragmentIncorrect);
        mInputPasswordLayout = (TextInputLayout) mView.findViewById(R.id.passwordfriend_fragmentIncorrectLayout);
        title.setText(mFriendPhone);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                .setTitle(getResources().getString(R.string.chanche_password))
                .setView(mView)
                .setNegativeButton(getResources().getString(R.string.cancel_dialogfragment), this)
                .setPositiveButton(getResources().getString(R.string.chanche), this);

        return builder.create();
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        switch (which) {
            case Dialog.BUTTON_POSITIVE:
                if (mNewPassword.getText().toString().equals(MainActivity.mDataSnapshot.child(mFriendPhone).child(Constants.PASSWORD).getValue().toString())) {
                    mFirebaseRef.child(myNumberPhone)
                            .child(Constants.FRIENDS)
                            .child(mFriendPhone).child(Constants.PASSWORD).setValue(mNewPassword.getText().toString());
                } else {
                    mInputPasswordLayout.setHint(getResources().getString(R.string.incorrect_password));
                }
                break;
        }

    }
}
