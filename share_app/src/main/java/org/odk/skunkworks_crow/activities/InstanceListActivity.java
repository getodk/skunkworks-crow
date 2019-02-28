package org.odk.skunkworks_crow.activities;

import org.odk.collect.android.provider.InstanceProviderAPI;

import static org.odk.skunkworks_crow.utilities.ApplicationConstants.SortingOrder.BY_DATE_ASC;
import static org.odk.skunkworks_crow.utilities.ApplicationConstants.SortingOrder.BY_DATE_DESC;
import static org.odk.skunkworks_crow.utilities.ApplicationConstants.SortingOrder.BY_NAME_ASC;
import static org.odk.skunkworks_crow.utilities.ApplicationConstants.SortingOrder.BY_NAME_DESC;

abstract class InstanceListActivity extends AppListActivity {
    protected String getSortingOrder() {
        String sortingOrder = InstanceProviderAPI.InstanceColumns.DISPLAY_NAME + " COLLATE NOCASE ASC, " + InstanceProviderAPI.InstanceColumns.STATUS + " DESC";
        switch (getSelectedSortingOrder()) {
            case BY_NAME_ASC:
                sortingOrder = InstanceProviderAPI.InstanceColumns.DISPLAY_NAME + " COLLATE NOCASE ASC, " + InstanceProviderAPI.InstanceColumns.STATUS + " DESC";
                break;
            case BY_NAME_DESC:
                sortingOrder = InstanceProviderAPI.InstanceColumns.DISPLAY_NAME + " COLLATE NOCASE DESC, " + InstanceProviderAPI.InstanceColumns.STATUS + " DESC";
                break;
            case BY_DATE_ASC:
                sortingOrder = InstanceProviderAPI.InstanceColumns.LAST_STATUS_CHANGE_DATE + " ASC";
                break;
            case BY_DATE_DESC:
                sortingOrder = InstanceProviderAPI.InstanceColumns.LAST_STATUS_CHANGE_DATE + " DESC";
                break;
        }
        return sortingOrder;
    }
}