package com.xie.librlrecyclerview.other

import androidx.recyclerview.widget.DiffUtil

/**
 * @Author XJA87
 * @Date 2022/4/27 15:53
 */
class DiffItemCallBack<T>(private val oldList: MutableList<T>, private val newList: MutableList<T>) :
    DiffUtil.Callback() {

    override fun getOldListSize(): Int {
        return oldList.size
    }

    override fun getNewListSize(): Int {
        return newList.size
    }

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition] == newList[newItemPosition]
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition] == newList[newItemPosition]
    }
}
