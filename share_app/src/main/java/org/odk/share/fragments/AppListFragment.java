package org.odk.share.fragments;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.SearchView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import org.odk.share.R;

import java.util.LinkedHashSet;

abstract class AppListFragment extends Fragment {

    protected LinkedHashSet<Long> selectedInstances = new LinkedHashSet<>();
    private static final String SELECTED_INSTANCES = "selectedInstances";

    private String filterText;


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState != null) {
            selectedInstances.addAll((LinkedHashSet<Long>) savedInstanceState.getSerializable(SELECTED_INSTANCES));
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable(SELECTED_INSTANCES, selectedInstances);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        inflater.inflate(R.menu.list_menu, menu);

        final MenuItem sortItem = menu.findItem(R.id.menu_sort);
        final MenuItem searchItem = menu.findItem(R.id.menu_filter);
        final SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        searchView.setQueryHint(getResources().getString(R.string.search));
        searchView.setMaxWidth(Integer.MAX_VALUE);

        SearchView.SearchAutoComplete searchAutoComplete =
                (SearchView.SearchAutoComplete) searchView.findViewById(android.support.v7.appcompat.R.id.search_src_text);
        searchAutoComplete.setCursorVisible(true);
        searchAutoComplete.setHintTextColor(ContextCompat.getColor(getActivity(), android.R.color.white));
        searchAutoComplete.setTextColor(ContextCompat.getColor(getActivity(), android.R.color.white));

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                filterText = query;
                updateAdapter();
                searchView.clearFocus();
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterText = newText;
                updateAdapter();
                return false;
            }
        });

        MenuItemCompat.setOnActionExpandListener(searchItem, new MenuItemCompat.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                sortItem.setVisible(false);
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                sortItem.setVisible(true);
                return true;
            }
        });
    }

    protected abstract void updateAdapter();

    protected CharSequence getFilterText() {
        return filterText != null ? filterText : "";
    }
}
