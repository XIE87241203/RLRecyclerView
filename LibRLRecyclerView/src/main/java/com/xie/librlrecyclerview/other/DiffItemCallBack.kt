package com.xie.librlrecyclerview.other

import androidx.recyclerview.widget.DiffUtil
import com.xie.librlrecyclerview.model.IDiffItemCallBack

/**
 * @Author XJA87
 * @Date 2022/4/27 15:53
 */
class DiffItemCallBack<T>(
    private val oldList: List<T>,
    private val newList: List<T>,
    private var diffCallBack: IDiffItemCallBack<T>) : DiffUtil.Callback() {

    override fun getOldListSize(): Int {
        return oldList.size
    }

    override fun getNewListSize(): Int {
        return newList.size
    }

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return diffCallBack.areItemTheSame(oldList[oldItemPosition], newList[newItemPosition])
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return diffCallBack.areContentTheSame(oldList[oldItemPosition], newList[newItemPosition])
    }
}
