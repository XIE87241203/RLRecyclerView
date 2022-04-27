package com.xie.librlrecyclerview.model

import androidx.recyclerview.widget.DiffUtil
import com.xie.librlrecyclerview.base.RLRecyclerAdapter
import com.xie.librlrecyclerview.other.UpdateType

/**
 * Created by Anthony on 2020/9/7.
 * Describe:
 */
class RLListDataHelper2<T> {

    interface DataUpdatedListener<T> {
        /**
         * 在内容Update成功后回调
         */
        fun onDataUpdated(changedData: UpdateList<T>)
    }

    var dataUpdatedListener: DataUpdatedListener<T>? = null

    /**
     * 包括头部和脚部的整个列表数据
     */
    val allListData: MutableList<T> = ArrayList()

    /**
     * 除去头部和脚部的数据
     */
    val listData: MutableList<T> = ArrayList()

    var refreshHeaderInfo:ListHeaderInfo? = null
    var loadMoreFooterInfo:ListFooterInfo? = null

    val headerListData: MutableList<T> = ArrayList()

    val footListData: MutableList<T> = ArrayList()

    fun setUpdateList(listAdapter: RLRecyclerAdapter<T>, updateList: UpdateList<T>) {
        when (updateList.updateType) {
            UpdateType.REFRESH_LIST -> {
                //刷新整个列表
                listData.clear()
                listData.addAll(updateList.listData)
                listAdapter.notifyDataSetChanged()
            }
            UpdateType.INSERT_DATA -> {
                val result = DiffUtil.calculateDiff(DiffCallBack(listData, updateList.listData))
                listData.clear()
                listData.addAll(updateList.listData)
                result.dispatchUpdatesTo(listAdapter)
            }
        }
        dataUpdatedListener?.onDataUpdated(updateList)
    }

    fun getNewListData():MutableList<Any>{
        val newList = ArrayList<Any>()
        refreshHeaderInfo?.let {
            newList.add(it)
        }

    }

}