package com.xie.rlrecyclerview

import android.view.View
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
    var onItemClickListener: OnItemClickListener? = null

    interface OnItemClickListener {
        fun onLongClick(info: String)

        fun onClick(info: String)
    }

    override fun onCreateViewHolderNew(parent: ViewGroup, viewType: Int): BaseRecyclerViewHolder {
        return BaseRecyclerViewHolder.Companion.createViewHolder(
            parent.context,
            parent,
            R.layout.item_list
        )
    }

    override fun onBindViewHolderNew(holder: BaseRecyclerViewHolder, position: Int) {
        //通过getListData()获取列表数据，在这个例子里getListData()返回ArrayList<String>
        val info = getListData()[position]
        (holder.contentView as TextView).text = info
        //不要在回调中写入position，否则在删除的时候导致位置错乱
        //建议直接使用内容
        holder.contentView.setOnClickListener {
            onItemClickListener?.onClick(info)
        }
        holder.contentView.setOnLongClickListener(View.OnLongClickListener {
            onItemClickListener?.onLongClick(info)
            true
        })
    }

    override fun getItemViewTypeNew(position: Int): Int {
        return 0
    }
}