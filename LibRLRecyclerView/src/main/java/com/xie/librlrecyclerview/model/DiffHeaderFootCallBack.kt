package com.xie.librlrecyclerview.model

import android.annotation.SuppressLint
import android.view.View
import androidx.collection.SparseArrayCompat
import androidx.recyclerview.widget.DiffUtil

/**
 * @Author XJA87
 * @Date 2022/4/27 15:53
 */
class DiffHeaderFootCallBack(private val oldList: SparseArrayCompat<View>, private val newList: SparseArrayCompat<View>) :
    DiffUtil.Callback() {

    override fun getOldListSize(): Int {
        return oldList.size()
    }

    override fun getNewListSize(): Int {
        return newList.size()
    }

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition] == newList[newItemPosition]
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition] == newList[newItemPosition]
    }
}
