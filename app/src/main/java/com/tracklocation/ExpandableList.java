package com.tracklocation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


public class ExpandableList {

    private Map<String, List<String>> mExpandableMap;

    public ExpandableList(List<String> groupNames) {
        mExpandableMap = new HashMap<>();
        Set<String> namesSet = new HashSet<>();
        for (String currentGroupName : groupNames) {
            namesSet.add(currentGroupName);
        }

        for (String currentGroupName : namesSet) {
            mExpandableMap.put(currentGroupName, new ArrayList<String>());
        }
    }

    public void addContactToGroup(String groupName, String contactName) {
        if (mExpandableMap.containsKey(groupName)) {
            mExpandableMap.get(groupName).add(contactName);
        }
    }

    public List<String> getContactsByGroup(String groupName) {
        if (mExpandableMap.containsKey(groupName)) {
            return mExpandableMap.get(groupName);
        } else {
            return null;
        }
    }

    public Map<String, List<String>> getGroupInformation() {
        return mExpandableMap;
    }

}
