package com.xie.rlrecyclerview

import android.view.ViewGroup
import android.widget.TextView
import com.xie.librlrecyclerview.base.BaseRecyclerViewHolder
import com.xie.librlrecyclerview.base.RLRecyclerAdapter
import com.xie.librlrecyclerview.model.UpdateList

/**
 * Created by Anthony on 2020/9/4.
 * Describe:
 */
//String为列表数据的类型，可根据自己的需要传入不同的类
class MyAdapter : RLRecyclerAdapter<String>() {

    override fun onCreateViewHolderNew(parent: ViewGroup, viewType: Int): BaseRecyclerViewHolder {
        return BaseRecyclerViewHolder.Companion.createViewHolder(
            parent.context,
            parent,
            R.layout.item_list
        )
    }

    override fun onBindViewHolderNew(holder: BaseRecyclerViewHolder, position: Int) {
        //通过getListData()获取列表数据，在这个例子里getListData()返回ArrayList<String>
        (holder.contentView as TextView).text = getListData()[position]
        holder.contentView.setOnClickListener {
            val newList = ArrayList(getListData())
            newList[position] = "已点击" + newList[position]
            updateList(UpdateList(listData = newList))
        }
        holder.contentView.setOnLongClickListener {
            val newList = ArrayList(getListData())
            newList.removeAt(position)
            updateList(UpdateList(listData = newList))
            true
        }
    }

    override fun getItemViewTypeNew(position: Int): Int {
        return 0
    }
}