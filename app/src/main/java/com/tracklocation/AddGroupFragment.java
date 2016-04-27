package com.tracklocation;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.firebase.client.Firebase;

/**
 * Created by kompot on 26.04.2016.
 */
public class AddGroupFragment extends DialogFragment implements DialogInterface.OnClickListener {
    private String mNumberPhone;
    private Firebase mFirebaseRef;
    private View mView;
    private EditText mNameGroup;
    private TextInputLayout mGroupNameLayout;
    public static AddGroupFragment newInstance(String numberPhone) {
        AddGroupFragment addGroupFragment = new AddGroupFragment();
        Bundle arguments = new Bundle();
        arguments.putString(Constants.PHONE_NUM_ARG, numberPhone);
        addGroupFragment.setArguments(arguments);
        return addGroupFragment;
    }
    public Dialog onCreateDialog(Bundle bundle) {
        Firebase.setAndroidContext(getActivity());
        mFirebaseRef = new Firebase(Constants.DATABASE_URL);
        LayoutInflater inflater = getActivity().getLayoutInflater();
        if (getArguments() != null) {
            mNumberPhone = getArguments().getString(Constants.PHONE_NUM_ARG);
        }
        mView = inflater.inflate(R.layout.fragment_addgroup, null);
        mGroupNameLayout = (TextInputLayout) mView.findViewById(R.id.namegroup_fragmentaddgroupLayout);
        mNameGroup = (EditText) mView.findViewById(R.id.namegroup_fragmentaddgroup);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                .setTitle(getResources().getString(R.string.add_group_tittle))
                .setView(mView)
                .setNegativeButton(R.string.cancel_dialogfragment, this)
                .setPositiveButton(R.string.add_friend, this);
        return builder.create();
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        switch (which) {
            case Dialog.BUTTON_POSITIVE:
               int count = (int) Singleton.getInstance().getDataSnapshot().child(mNumberPhone).child(Constants.GROUPS).getChildrenCount();
                mFirebaseRef.child(mNumberPhone).child(Constants.GROUPS).child( Integer.toString(count+1)).setValue(mNameGroup.getText().toString());
               // Singleton.getInstance().getSelectedUsers().add(mNameGroup.getText().toString());
                FriendListFragment.sExpandableListAdapter.addGroup(mNameGroup.getText().toString());
                //FriendListFragment.sExpandableListAdapter.notifyDataSetChanged();
                Toast.makeText(getActivity(),"Группа успешно добавлена",Toast.LENGTH_SHORT).show();
                break;
        }
    }
}
