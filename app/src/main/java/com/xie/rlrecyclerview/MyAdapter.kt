package com.xie.rlrecyclerview

import android.view.ViewGroup
import android.widget.TextView
import com.xie.librlrecyclerview.base.BaseRecyclerViewHolder
import com.xie.librlrecyclerview.base.RLRecyclerAdapter

/**
 * Created by Anthony on 2020/9/4.
 * Describe:
 */
class MyAdapter : RLRecyclerAdapter<String>() {

    override fun onCreateViewHolderNew(parent: ViewGroup, viewType: Int): BaseRecyclerViewHolder {
        return BaseRecyclerViewHolder.Companion.createViewHolder(
            parent.context,
            parent,
            R.layout.item_list
        )
    }

    override fun onBindViewHolderNew(holder: BaseRecyclerViewHolder, position: Int) {
        (holder.contentView as TextView).text = getListData()[position]
    }

    override fun getItemViewTypeNew(position: Int): Int {
        return 0
    }
}