package com.xie.librlrecyclerview.recycler_view.base

import android.content.Context
import android.util.SparseArray
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.recyclerview.widget.RecyclerView

/**
 * Created by Anthony on 2020/9/4.
 * Describe:
 */
class BaseRecyclerViewHolder(val contentView: View) : RecyclerView.ViewHolder(contentView) {
    private val mViews: SparseArray<View> = SparseArray()

    /**
     * 通过viewId获取控件
     *
     * @param viewId viewId
     * @return View
     */
    fun <T : View?> getView(viewId: Int): T {
        var view = mViews[viewId]
        if (view == null) {
            view = itemView.findViewById(viewId)
            mViews.put(viewId, view)
        }
        return view as T
    }

    companion object {
        fun createViewHolder(itemView: View): BaseRecyclerViewHolder {
            return BaseRecyclerViewHolder(itemView)
        }

        fun createViewHolder(
            context: Context,
            parent: ViewGroup,
            @LayoutRes layoutId: Int
        ): BaseRecyclerViewHolder {
            val itemView = LayoutInflater.from(context).inflate(layoutId, parent, false)
            return BaseRecyclerViewHolder(itemView)
        }
    }
}