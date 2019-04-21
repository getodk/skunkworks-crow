

package org.odk.share.fragments;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.odk.collect.android.dao.FormsDao;
import org.odk.collect.android.dao.InstancesDao;
import org.odk.collect.android.provider.FormsProviderAPI;
import org.odk.share.R;
import org.odk.share.activities.SendActivity;
import org.odk.share.adapters.FormsAdapter;
import org.odk.share.adapters.basecursoradapter.BaseCursorViewHolder;
import org.odk.share.adapters.basecursoradapter.ItemClickListener;
import org.odk.share.dao.TransferDao;
import org.odk.share.utilities.ApplicationConstants;
import org.odk.share.utilities.ArrayUtils;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static org.odk.share.fragments.ReviewedInstancesFragment.MODE;

/**
 * Created by laksh on 10/29/2018.
 */

public class BlankFormsFragment extends FormListFragment implements LoaderManager.LoaderCallbacks<Cursor>, ItemClickListener {

    public static final String FORM_IDS = "form_ids";
    private static final String FORM_CHOOSER_LIST_SORTING_ORDER = "formChooserListSortingOrder";
    private static final int FORM_LOADER = 2;

    @BindView(R.id.recyclerview)
    RecyclerView recyclerView;
    @BindView(R.id.buttonholder)
    LinearLayout buttonLayout;
    @BindView(R.id.empty_view)
    TextView emptyView;
    @BindView(R.id.send_button)
    Button sendButton;
    @BindView(R.id.toggle_button)
    Button toggleButton;

    @Inject
    InstancesDao instancesDao;

    @Inject
    FormsDao formsDao;

    @Inject
    TransferDao transferDao;

    private FormsAdapter formAdapter;

    public BlankFormsFragment() {
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        View view = inflater.inflate(R.layout.fragment_forms, container, false);
        ButterKnife.bind(this, view);

        LinearLayoutManager llm = new LinearLayoutManager(getActivity());
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(llm);

        restoreState(savedInstanceState);

        return view;
    }

    @Override
    public void onResume() {
        getActivity().getSupportLoaderManager().initLoader(FORM_LOADER, null, this);
        super.onResume();
    }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return formsDao.getFormsCursorLoader(getFilterText(), getSortingOrder());
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor cursor) {
        if (cursor != null) {
            cursor.moveToFirst();
            formAdapter = new FormsAdapter(getActivity(), cursor, this, selectedInstances, instancesDao, transferDao);
            recyclerView.setAdapter(formAdapter);
            setEmptyViewVisibility(cursor.getCount());
            if (formAdapter.getItemCount() > 0) {
                toggleButton.setText(getString(R.string.select_all));
                toggleButton.setEnabled(true);
            } else {
                toggleButton.setEnabled(false);
            }
        } else {
            setEmptyViewVisibility(0);
        }
        addListItemDivider();
    }


    @Override
    public void onLoaderReset(@NonNull Loader loader) {
    }

    @Override
    public void onItemClick(BaseCursorViewHolder holder, int position) {
        ((FormsAdapter.FormHolder) holder).toggleCheckbox();

        long id = ((FormsAdapter.FormHolder) holder).getForm().getId();
        if (selectedInstances.contains(id)) {
            selectedInstances.remove(id);
        } else {
            selectedInstances.add(id);
        }

        sendButton.setEnabled(selectedInstances.size() > 0);

        toggleButtonLabel();
    }

    private void setEmptyViewVisibility(int len) {
        if (len > 0) {
            recyclerView.setVisibility(View.VISIBLE);
            emptyView.setVisibility(View.GONE);
        } else {
            recyclerView.setVisibility(View.GONE);
            emptyView.setVisibility(View.VISIBLE);
            emptyView.setText(getString(R.string.no_forms));
        }
    }

    @OnClick(R.id.send_button)
    public void send() {
        Intent intent = new Intent(getActivity(), SendActivity.class);
        Long[] arr = selectedInstances.toArray(new Long[selectedInstances.size()]);
        long[] a = ArrayUtils.toPrimitive(arr);
        intent.putExtra(FORM_IDS, a);
        intent.putExtra(MODE, ApplicationConstants.SEND_BLANK_FORM_MODE);
        startActivity(intent);
        getActivity().finish();
    }

    @OnClick(R.id.toggle_button)
    public void toggle() {
        boolean newState = formAdapter.getItemCount() > selectedInstances.size();
        sendButton.setEnabled(newState);

        if (newState) {
            Cursor cursor = formAdapter.getCursor();
            if (cursor.moveToFirst()) {
                do {
                    selectedInstances.add(cursor.getLong(cursor.getColumnIndex(FormsProviderAPI.FormsColumns._ID)));
                } while (cursor.moveToNext());
            }
        } else {
            selectedInstances.clear();
        }

        formAdapter.notifyDataSetChanged();
        toggleButtonLabel();
    }

    private void toggleButtonLabel() {
        if (selectedInstances.size() == formAdapter.getItemCount()) {
            toggleButton.setText(getString(R.string.clear_all));
        } else {
            toggleButton.setText(getString(R.string.select_all));
        }
    }

    @Override
    protected void updateAdapter() {
        getActivity().getSupportLoaderManager().restartLoader(FORM_LOADER, null, this);
    }

    @Override
    protected String getSortingOrderKey() {
        return FORM_CHOOSER_LIST_SORTING_ORDER;
    }

    private void addListItemDivider() {
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(
                recyclerView.getContext(),
                DividerItemDecoration.VERTICAL);

        recyclerView.addItemDecoration(dividerItemDecoration);
    }
}