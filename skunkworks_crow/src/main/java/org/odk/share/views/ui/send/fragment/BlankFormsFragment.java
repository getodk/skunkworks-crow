

package org.odk.share.views.ui.send.fragment;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
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
import org.odk.share.views.ui.main.FormsAdapter;
import org.odk.share.views.ui.common.basecursor.BaseCursorViewHolder;
import org.odk.share.views.listeners.ItemClickListener;
import org.odk.share.dao.TransferDao;
import org.odk.share.utilities.ApplicationConstants;
import org.odk.share.utilities.ArrayUtils;
import org.odk.share.views.ui.send.SendActivity;

import java.util.LinkedHashSet;

import javax.inject.Inject;

import androidx.annotation.NonNull;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.Loader;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static org.odk.share.views.ui.instance.fragment.ReviewedInstancesFragment.MODE;

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
    private LinkedHashSet<Long> selectedForms;


    public BlankFormsFragment() {
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        View view = inflater.inflate(R.layout.fragment_forms, container, false);
        ButterKnife.bind(this, view);

        selectedForms = new LinkedHashSet<>();

        LinearLayoutManager llm = new LinearLayoutManager(getActivity());
        llm.setOrientation(RecyclerView.VERTICAL);
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
        return formsDao.getFormsCursorLoader(getFilterText(), getSortingOrder());
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor cursor) {
        if (cursor != null) {
            cursor.moveToFirst();
            formAdapter = new FormsAdapter(getActivity(), cursor, this, selectedForms, instancesDao, transferDao);
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
        if (selectedForms.isEmpty()) {
            sendButton.setText(getString(R.string.send_forms));
        } else {
            sendButton.setText(String.format(getString(R.string.send_count), selectedForms.size()));
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