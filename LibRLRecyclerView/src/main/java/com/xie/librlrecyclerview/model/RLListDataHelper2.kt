package com.xie.librlrecyclerview.model

import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListUpdateCallback
import com.xie.librlrecyclerview.base.RLRecyclerAdapter
import com.xie.librlrecyclerview.other.UpdateType

/**
 * Created by Anthony on 2020/9/7.
 * Describe:
 */
class RLListDataHelper2<T>(val listAdapter: RLRecyclerAdapter<T>) {

    interface DataUpdatedListener<T> {
        /**
         * 在内容Update成功后回调
         */
        fun onDataUpdated(changedData: UpdateList<T>)
    }

    var dataUpdatedListener: DataUpdatedListener<T>? = null

    /**
     * 除去头部和脚部的数据
     */
    val listData: MutableList<T> = ArrayList()

    val adapterCallBack = object : ListUpdateCallback {
        override fun onInserted(position: Int, count: Int) {
            listAdapter.notifyItemRangeInserted(position + listAdapter.getHeadersCount(), count)
        }

        override fun onRemoved(position: Int, count: Int) {
            listAdapter.notifyItemRangeRemoved(position + listAdapter.getHeadersCount(), count)
        }

        override fun onMoved(fromPosition: Int, toPosition: Int) {
            listAdapter.notifyItemMoved(
                fromPosition + listAdapter.getHeadersCount(),
                toPosition + listAdapter.getHeadersCount()
            )
        }

        override fun onChanged(position: Int, count: Int, payload: Any?) {
            listAdapter.notifyItemRangeChanged(
                position + listAdapter.getHeadersCount(),
                count,
                payload
            )
        }
    }

    fun setUpdateList(updateList: UpdateList<T>) {
        when (updateList.updateType) {
            UpdateType.REFRESH_LIST -> {
                //刷新整个列表
                listData.clear()
                listData.addAll(updateList.listData)
                listAdapter.notifyDataSetChanged()
            }
            UpdateType.INSERT_DATA, UpdateType.CHANGE_LIST -> {
                val result = DiffUtil.calculateDiff(DiffItemCallBack(listData, updateList.listData))
                listData.clear()
                listData.addAll(updateList.listData)
                result.dispatchUpdatesTo(adapterCallBack)
            }
        }

        dataUpdatedListener?.onDataUpdated(updateList)
    }
}