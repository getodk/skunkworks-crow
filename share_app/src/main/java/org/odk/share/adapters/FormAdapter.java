package org.odk.share.adapters;

import android.content.Context;
import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.odk.share.R;
import org.odk.share.provider.FormsProviderAPI;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by laksh on 6/21/2018.
 */

public class FormAdapter extends RecyclerView.Adapter<FormAdapter.FormHolder> {

    private Cursor cursor;
    private Context context;
    private final FormAdapter.OnItemClickListener listener;

    public FormAdapter(Context context, Cursor cursor, FormAdapter.OnItemClickListener listener) {
        this.context = context;
        this.cursor = cursor;
        this.listener = listener;
    }

    @NonNull
    @Override
    public FormAdapter.FormHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.two_item_list, null);
        return new FormAdapter.FormHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FormAdapter.FormHolder holder, int position) {
        cursor.moveToPosition(holder.getAdapterPosition());
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onItemClick(v, holder.getAdapterPosition());
            }
        });
        holder.title.setText(cursor.getString(cursor.getColumnIndex(FormsProviderAPI.FormsColumns.DISPLAY_NAME)));
        String version = cursor.getString(cursor.getColumnIndex(FormsProviderAPI.FormsColumns.JR_VERSION));
        String id = cursor.getString(cursor.getColumnIndex(FormsProviderAPI.FormsColumns.JR_FORM_ID));
        String subtitle = "";

        if (version != null) {
            subtitle = context.getString(R.string.version) + " " + version + " ";
        }
        subtitle += context.getString(R.string.id) + " " + id;
        holder.subtitle.setText(subtitle);
    }

    @Override
    public int getItemCount() {
        return cursor.getCount();
    }



    public Cursor getCursor() {
        return cursor;
    }

    public interface OnItemClickListener {
        void onItemClick(View v, int position);
    }

    static class FormHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.tvTitle) public TextView title;
        @BindView(R.id.tvSubtitle) public TextView subtitle;

        FormHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }
}
