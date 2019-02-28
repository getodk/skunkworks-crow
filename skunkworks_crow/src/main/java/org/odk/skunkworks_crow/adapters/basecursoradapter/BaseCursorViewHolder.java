package org.odk.skunkworks_crow.adapters.basecursoradapter;

import android.support.v7.widget.RecyclerView;
import android.view.View;


public class BaseCursorViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    private ItemClickListener listener;

    public BaseCursorViewHolder(View itemView) {
        super(itemView);
        itemView.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (listener != null) {
            listener.onItemClick(this, getAdapterPosition());
        }
    }

    void setListener(ItemClickListener listener) {
        this.listener = listener;
    }
}