package org.odk.share.fragments;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.odk.share.R;
import org.odk.share.activities.SendActivity;
import org.odk.share.adapters.FormsAdapter;
import org.odk.share.adapters.basecursoradapter.BaseCursorViewHolder;
import org.odk.share.adapters.basecursoradapter.ItemClickListener;
import org.odk.share.dao.FormsDao;
import org.odk.share.provider.FormsProviderAPI;
import org.odk.share.utilities.ApplicationConstants;
import org.odk.share.utilities.ArrayUtils;

import java.util.LinkedHashSet;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static org.odk.share.fragments.ReviewedInstancesFragment.MODE;

/**
 * Created by laksh on 10/29/2018.
 */

public class BlankFormsFragment extends FormListFragment implements LoaderManager.LoaderCallbacks<Cursor>, ItemClickListener {

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

    private static final String FORM_CHOOSER_LIST_SORTING_ORDER = "formChooserListSortingOrder";

    public static final String FORM_IDS = "form_ids";

    private static final int FORM_LOADER = 2;
    private FormsAdapter formAdapter;
    private LinkedHashSet<Long> selectedForms;


    public BlankFormsFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        View view = inflater.inflate(R.layout.fragment_forms, container, false);
        ButterKnife.bind(this, view);

        selectedForms = new LinkedHashSet<>();

        LinearLayoutManager llm = new LinearLayoutManager(getActivity());
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(llm);
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
        return new FormsDao().getFormsCursorLoader(getFilterText(), getSortingOrder());
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor cursor) {
        if (cursor != null) {
            cursor.moveToFirst();
            formAdapter = new FormsAdapter(getActivity(), cursor, this, selectedForms);
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
    }


    @Override
    public void onLoaderReset(@NonNull Loader loader) {
    }

    @Override
    public void onItemClick(BaseCursorViewHolder holder, int position) {
        CheckBox checkBox = ((FormsAdapter.FormHolder) holder).checkBox;
        checkBox.setChecked(!checkBox.isChecked());

        long id = ((FormsAdapter.FormHolder) holder).getForm().getIndex();
        if (selectedForms.contains(id)) {
            selectedForms.remove(id);
        } else {
            selectedForms.add(id);
        }

        sendButton.setEnabled(selectedForms.size() > 0);

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
        Long[] arr = selectedForms.toArray(new Long[selectedForms.size()]);
        long[] a = ArrayUtils.toPrimitive(arr);
        intent.putExtra(FORM_IDS, a);
        intent.putExtra(MODE, ApplicationConstants.SEND_BLANK_FORM_MODE);
        startActivity(intent);
        getActivity().finish();
    }

    @OnClick(R.id.toggle_button)
    public void toggle() {
        boolean newState = formAdapter.getItemCount() > selectedForms.size();
        sendButton.setEnabled(newState);

        if (newState) {
            Cursor cursor = formAdapter.getCursor();
            if (cursor.moveToFirst()) {
                do {
                    selectedForms.add(cursor.getLong(cursor.getColumnIndex(FormsProviderAPI.FormsColumns._ID)));
                } while (cursor.moveToNext());
            }
        } else {
            selectedForms.clear();
        }

        formAdapter.notifyDataSetChanged();
        toggleButtonLabel();
    }

    private void toggleButtonLabel() {
        if (selectedForms.size() == formAdapter.getItemCount()) {
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
}