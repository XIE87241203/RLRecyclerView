package com.xie.librlrecyclerview.base

import android.view.View
import android.view.ViewGroup
import androidx.collection.SparseArrayCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.GridLayoutManager.SpanSizeLookup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.xie.librlrecyclerview.model.RLListDataHelper
import com.xie.librlrecyclerview.model.UpdateList

/**
 * Created by Anthony on 2020/9/4.
 * Describe:
 */
abstract class RLRecyclerAdapter<T> : RecyclerView.Adapter<BaseRecyclerViewHolder>() {
    companion object {
        const val BASE_ITEM_TYPE_HEADER = 100001
        const val SPECIAL_ITEM_TYPE_REFRESH_HEADER = 100000
        const val BASE_ITEM_TYPE_FOOTER = 200000
        const val SPECIAL_ITEM_TYPE_LOAD_FOOTER = 1000000
    }

    //容器
    val mHeaderViews = SparseArrayCompat<View>()
    val mFootViews = SparseArrayCompat<View>()

    private val dataHelper = RLListDataHelper<T>()

    abstract fun onCreateViewHolderNew(parent: ViewGroup, viewType: Int): BaseRecyclerViewHolder
    abstract fun onBindViewHolderNew(holder: BaseRecyclerViewHolder, position: Int)
    abstract fun getItemViewTypeNew(position: Int): Int

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseRecyclerViewHolder {
        val headerView = mHeaderViews[viewType]
        if (headerView != null) {
            //头部
            return BaseRecyclerViewHolder.createViewHolder(headerView)
        } else {
            val footerView = mFootViews[viewType]
            if (footerView != null) {
                //尾部
                return BaseRecyclerViewHolder.createViewHolder(footerView)
            }
        }
        //内容部分
        return onCreateViewHolderNew(parent, viewType)
    }

    override fun getItemViewType(position: Int): Int {
        if (isHeaderViewPos(position)) {
            return mHeaderViews.keyAt(position)
        } else if (isFooterViewPos(position)) {
            return mFootViews.keyAt(position - getHeadersCount() - getRealItemCount())
        }
        return getItemViewTypeNew(position - getHeadersCount())
    }

    override fun onBindViewHolder(holder: BaseRecyclerViewHolder, position: Int) {
        if (isHeaderViewPos(position)) {
            return
        }
        if (isFooterViewPos(position)) {
            return
        }
        onBindViewHolderNew(holder, position - getHeadersCount())
    }

    //获取内容Item数量
    fun getRealItemCount(): Int {
        return dataHelper.listData.size
    }

    fun upDateList(updateList: UpdateList<T>) {
        dataHelper.setUpdateList(this, updateList)
    }

    override fun getItemCount(): Int {
        return getHeadersCount() + getFootersCount() + getRealItemCount()
    }

    override fun onViewAttachedToWindow(holder: BaseRecyclerViewHolder) {
        //处理StaggeredGridLayout类型
        var position = holder.adapterPosition
        if (position == RecyclerView.NO_POSITION) {
            position = holder.layoutPosition
        }
        if (isHeaderViewPos(position) || isFooterViewPos(position)) {
            val lp = holder.itemView.layoutParams
            if (lp is StaggeredGridLayoutManager.LayoutParams) {
                lp.isFullSpan = true
            }
        }
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        //处理gridLayout类型
        val layoutManager = recyclerView.layoutManager
        if (layoutManager is GridLayoutManager) {
            val spanSizeLookup = layoutManager.spanSizeLookup
            layoutManager.spanSizeLookup = object : SpanSizeLookup() {
                override fun getSpanSize(position: Int): Int {
                    val viewType = getItemViewType(position)
                    if (mHeaderViews[viewType] != null) {
                        return layoutManager.spanCount
                    } else if (mFootViews[viewType] != null) {
                        return layoutManager.spanCount
                    }
                    return spanSizeLookup?.getSpanSize(position) ?: 1
                }
            }
        }
    }

    fun getListData(): ArrayList<T> {
        return dataHelper.listData
    }

    /**
     * 设置刷新头部
     *
     * @param refreshHeader refreshHeader
     */
    open fun setRefreshHeader(refreshHeader: BaseRefreshHeader) {
        mHeaderViews.put(SPECIAL_ITEM_TYPE_REFRESH_HEADER, refreshHeader)
    }

    /**
     * 设置加载footer
     *
     * @param loadMoreFooter loadMoreFooter
     */
    internal open fun setLoadMoreFooter(loadMoreFooter: BaseLoadMoreFooter) {
        mFootViews.put(
            SPECIAL_ITEM_TYPE_LOAD_FOOTER + BASE_ITEM_TYPE_FOOTER, loadMoreFooter
        )
    }

    /**
     * 添加Header
     *
     * @param view view
     */
    open fun addHeaderView(view: View?) {
        mHeaderViews.put(
            mHeaderViews.size() + BASE_ITEM_TYPE_HEADER,
            view
        )
    }

    /**
     * 删除Header
     *
     * @param view view
     */
    open fun removeHeaderView(view: View?) {
        val index = mHeaderViews.indexOfValue(view)
        if (index != -1) {
            mHeaderViews.removeAt(index)
        }
    }

    open fun removeAllHeaderView() {
        mHeaderViews.clear()
    }

    /**
     * 添加Footer
     *
     * @param view view
     */
    open fun addFooterView(view: View?) {
        mFootViews.put(
            mFootViews.size() + BASE_ITEM_TYPE_FOOTER,
            view
        )
    }

    /**
     * 删除Footer
     *
     * @param view view
     */
    open fun removeFooterView(view: View?) {
        val index = mFootViews.indexOfValue(view)
        if (index != -1) {
            mFootViews.removeAt(index)
        }
    }

    open fun removeAllFooterView() {
        mFootViews.clear()
    }


    /**
     * 判断是不是Header
     *
     * @param position position
     * @return boolean
     */
    open fun isHeaderViewPos(position: Int): Boolean {
        return position < getHeadersCount()
    }

    /**
     * 判断是不是Footer
     *
     * @param position position
     * @return boolean
     */
    open fun isFooterViewPos(position: Int): Boolean {
        return position >= getHeadersCount() + getRealItemCount()
    }

    /**
     * 获取Header Item数量
     *
     * @return int
     */
    open fun getHeadersCount(): Int {
        return mHeaderViews.size()
    }

    /**
     * 获取Footer Item数量
     *
     * @return int
     */
    open fun getFootersCount(): Int {
        return mFootViews.size()
    }
}