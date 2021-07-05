package com.xie.librlrecyclerview.model

import com.xie.librlrecyclerview.base.RLRecyclerAdapter
import com.xie.librlrecyclerview.other.UpdateType

/**
 * Created by Anthony on 2020/9/7.
 * Describe:
 */
class RLListDataHelper<T> {
    val listData: MutableList<T> = ArrayList()

    fun setUpdateList(listAdapter: RLRecyclerAdapter<T>, updateList: UpdateList<T>) {
        when (updateList.updateType) {
            UpdateType.REFRESH_LIST -> {
                //刷新整个列表
                listData.clear()
                listData.addAll(updateList.listData)
                listAdapter.notifyDataSetChanged()
            }
            UpdateType.INSERT_DATA -> {
                if (listData.isEmpty()) {
                    //从0插入
                    listData.addAll(updateList.listData)
                    listAdapter.notifyItemRangeInserted(
                        listAdapter.getHeadersCount(),
                        listData.size
                    )
                } else {
                    val startIndex = checkData(updateList.listData)
                    //排除列表完全相等的情况
                    if (startIndex == updateList.listData.size - 1) return
                    if (startIndex >= 0) {
                        val insertStart = listData.size
                        listData.addAll(
                            updateList.listData.subList(
                                startIndex + 1,
                                updateList.listData.size
                            )
                        )
                        //只刷新插入的部分
                        listAdapter.notifyItemRangeInserted(
                            insertStart + listAdapter.getHeadersCount(),
                            listData.size - insertStart
                        )
                    } else {
                        //数据异常，刷新整个列表
                        listAdapter.notifyDataSetChanged()
                    }
                }
            }
        }
    }

    /**
     * 检查是否满足插入数据,不满足返回-1，满足的话返回旧数据最后一个数据在新数据的序号
     */
    private fun checkData(updateData: MutableList<T>): Int {
        if (updateData.size < listData.size) {
            //新列表比旧列表短
            return -1
        }
        //只判定旧列表最后一个元素时候和新列表对应位置的元素相等
        val endIndex = listData.size - 1
        if (listData[endIndex] !== updateData[endIndex]) {
            //旧列表和新列表不匹配
            return -1
        }
        return endIndex
    }
}