package org.odk.share.views.ui.send.fragment;

import org.odk.collect.android.provider.FormsProviderAPI;
import org.odk.share.views.ui.common.applist.AppListFragment;

import static org.odk.share.utilities.ApplicationConstants.SortingOrder.BY_DATE_ASC;
import static org.odk.share.utilities.ApplicationConstants.SortingOrder.BY_DATE_DESC;
import static org.odk.share.utilities.ApplicationConstants.SortingOrder.BY_NAME_ASC;
import static org.odk.share.utilities.ApplicationConstants.SortingOrder.BY_NAME_DESC;

/**
 * Created by laksh on 11/7/2018.
 */

public abstract class FormListFragment extends AppListFragment {
    protected static final String SORT_BY_NAME_ASC
            = FormsProviderAPI.FormsColumns.DISPLAY_NAME + " COLLATE NOCASE ASC";
    protected static final String SORT_BY_NAME_DESC
            = FormsProviderAPI.FormsColumns.DISPLAY_NAME + " COLLATE NOCASE DESC";
    protected static final String SORT_BY_DATE_ASC = FormsProviderAPI.FormsColumns.DATE + " ASC";
    protected static final String SORT_BY_DATE_DESC = FormsProviderAPI.FormsColumns.DATE + " DESC";

    protected String getSortingOrder() {
        String sortingOrder = SORT_BY_NAME_ASC;
        switch (getSelectedSortingOrder()) {
            case BY_NAME_ASC:
                sortingOrder = SORT_BY_NAME_ASC;
                break;
            case BY_NAME_DESC:
                sortingOrder = SORT_BY_NAME_DESC;
                break;
            case BY_DATE_ASC:
                sortingOrder = SORT_BY_DATE_ASC;
                break;
            case BY_DATE_DESC:
                sortingOrder = SORT_BY_DATE_DESC;
                break;
        }
        return sortingOrder;
    }
}