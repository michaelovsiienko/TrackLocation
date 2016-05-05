package com.tracklocation;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


class ExpListAdapter extends BaseExpandableListAdapter {

    private Context mContext;

    private Map<String, List<String>> mGroupInformationMap;
    private List<String> mGroupNames;

    public ExpListAdapter(Context context, Map<String, List<String>> groupInformation) {
        mContext = context;

        mGroupNames = new ArrayList<>();

        mGroupInformationMap = groupInformation;
        for (String currentGroupName : mGroupInformationMap.keySet()) {
            mGroupNames.add(currentGroupName);
        }

    }

    public int getGroupCount() {
        return mGroupNames.size();
    }

    public int getChildrenCount(int groupPosition) {
        return mGroupInformationMap.get(mGroupNames.get(groupPosition)).size();
    }

    public String getGroup(int groupPosition) {
        return mGroupNames.get(groupPosition);
    }

    public String getChild(int groupPosition, int childPosition) {
        return mGroupInformationMap.get(mGroupNames.get(groupPosition)).get(childPosition);
    }

    public void addGroup(String groupName) {
        mGroupNames.add(groupName);
    }

    public void addContact(String groupName, String contactName) {
        mGroupInformationMap.get(groupName).add(contactName);
    }

    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }


    public boolean hasStableIds() {
        return true;
    }


    public View getGroupView(int groupPosition, boolean isExpanded, View convertView,
                             ViewGroup parent) {

        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.groupview, null);

        }
        TextView groupTextView = (TextView) convertView.findViewById(R.id.groupTextView);
        groupTextView.setText(mGroupNames.get(groupPosition));
        return convertView;

    }

    public View getChildView(final int groupPosition, final int childPosition, boolean isLastChild,
                             View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.childview, null);
        }
        final TextView childTextView = (TextView) convertView.findViewById(R.id.childTextView);
        final CheckBox childCheckBox = (CheckBox) convertView.findViewById(R.id.childViewCheckBox);
        childCheckBox.setFocusable(false);
        childTextView.setText(getChild(groupPosition, childPosition).toString());

        String bufferMyPassword = MainActivity.mDataSnapshot
                .child(Singleton.getInstance().getUserPhone())
                .child(Constants.FRIENDS).child(childTextView.getText().toString())
                .child(Constants.PASSWORD).getValue().toString();
        String bufferFriendPassword = MainActivity.mDataSnapshot
                .child(childTextView.getText().toString())
                .child(Constants.PASSWORD).getValue().toString();
        String status = MainActivity.mDataSnapshot
                .child(childTextView.getText().toString())
                .child(Constants.STATUS).getValue().toString();

        TextView oldPassword = (TextView) convertView.findViewById(R.id.oldPasschildTextView);
        TextView statusTextView = (TextView) convertView.findViewById(R.id.status);
        if (status.equals("online"))
            statusTextView.setText("Online");
        else
            statusTextView.setText("Offline");
        if (!bufferMyPassword.equals(bufferFriendPassword)) {
            oldPassword.setVisibility(View.VISIBLE);
            childCheckBox.setVisibility(View.GONE);
        } else {
            oldPassword.setVisibility(View.GONE);
            childCheckBox.setVisibility(View.VISIBLE);
        }

        childCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (childCheckBox.isChecked()) {
                    Singleton.getInstance().getSelectedUsers().add(childTextView.getText().toString());
                } else
                    Singleton.getInstance().getSelectedUsers().remove(childTextView.getText().toString());
            }
        });


        return convertView;
    }


    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

    public boolean areAllItemsEnabled() {
        return false;
    }


}
