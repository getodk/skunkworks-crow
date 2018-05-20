package org.odk.share.adapters;

import android.content.Context;
import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import org.odk.share.R;
import org.odk.share.provider.InstanceProviderAPI;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by laksh on 5/20/2018.
 */

public class InstanceAdapter extends RecyclerView.Adapter<InstanceAdapter.InstanceHolder> {

    private Cursor cursor;
    private Context context;

    public InstanceAdapter(Context context, Cursor cursor) {
        this.context = context;
        this.cursor = cursor;
    }

    @Override
    public InstanceHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.list_item_checkbox, null);
        return new InstanceHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull InstanceHolder holder, int position) {
        cursor.moveToPosition(position);
        holder.title.setText(cursor.getString(cursor.getColumnIndex(InstanceProviderAPI.InstanceColumns.DISPLAY_NAME)));
        holder.subtitle.setText(cursor.getString(cursor.getColumnIndex(InstanceProviderAPI.InstanceColumns.DISPLAY_SUBTEXT)));
    }

    @Override
    public int getItemCount() {
        return cursor.getCount();
    }

    class InstanceHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.tvTitle) TextView title;
        @BindView(R.id.tvSubtitle) TextView subtitle;
        @BindView(R.id.checkbox) CheckBox checkBox;

        InstanceHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }
}
