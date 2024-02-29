package com.statuses.statussaver.recycler;

import android.view.View;

public interface RecyclerClickListener {

    /**
     * Interface for Recycler View Click listener
     **/

    void onClick(View view, int position);

    void onLongClick(View view, int position);
}