package com.xie.librlrecyclerview.recycler_view.base

import android.view.View
import android.view.ViewGroup
import androidx.collection.SparseArrayCompat
import androidx.recyclerview.widget.*
import androidx.recyclerview.widget.GridLayoutManager.SpanSizeLookup
import com.xie.librlrecyclerview.recycler_view.other.DiffHeaderFootCallBack
import com.xie.librlrecyclerview.recycler_view.other.IDiffItemCallBack
import com.xie.librlrecyclerview.recycler_view.model.RLListDataHelper2
import com.xie.librlrecyclerview.recycler_view.model.UpdateList
import com.xie.librlrecyclerview.refresh_layout.BaseRefreshHeader

/**
 * Created by Anthony on 2020/9/4.
 * Describe:
 */
abstract class RLRecyclerAdapter<T> : RecyclerView.Adapter<BaseRecyclerViewHolder>(),
    IDiffItemCallBack<T> {
    companion object {
        const val BASE_ITEM_TYPE_HEADER = 100001
        const val SPECIAL_ITEM_TYPE_REFRESH_HEADER = 100000
        const val BASE_ITEM_TYPE_FOOTER = 200000
        const val SPECIAL_ITEM_TYPE_LOAD_FOOTER = 1000000
    }

    //容器
    val mHeaderViews = SparseArrayCompat<View>()
    val mFootViews = SparseArrayCompat<View>()

    internal var loadMoreKey = -1

    private val dataHelper: RLListDataHelper2<T> by lazy { RLListDataHelper2(this , this) }

    private val adapterFootCallBack = object : ListUpdateCallback {
        override fun onInserted(position: Int, count: Int) {
            notifyItemRangeInserted(position + getHeadersCount() + getRealItemCount(), count)
        }

        override fun onRemoved(position: Int, count: Int) {
            notifyItemRangeRemoved(position + getHeadersCount() + getRealItemCount(), count)
        }

        override fun onMoved(fromPosition: Int, toPosition: Int) {
            notifyItemMoved(
                fromPosition + getHeadersCount() + getRealItemCount(),
                toPosition + getHeadersCount() + getRealItemCount()
            )
        }

        override fun onChanged(position: Int, count: Int, payload: Any?) {
            notifyItemRangeChanged(
                position + getHeadersCount() + getRealItemCount(),
                count,
                payload
            )
        }
    }

    /**
     * 判断是否是同一个item，有id的item需要判断id，否则不能正确调用itemChange
     */
    override fun areItemTheSame(oldItem: T, newItem: T): Boolean {
        return oldItem == newItem
    }

    override fun areContentTheSame(oldItem: T, newItem: T): Boolean {
        return oldItem == newItem
    }

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
        //检查加载更多
        getLoadMoreFooter()?.let {
            if (it.isLoadMoreFinish() && loadMoreKey != -1 && dataHelper.listData.isNotEmpty() && position >= dataHelper.listData.size - loadMoreKey) {
                it.startLoadMore()
            }
        }
    }

    //获取内容Item数量
    open fun getRealItemCount(): Int {
        return dataHelper.listData.size
    }

    open fun updateList(updateList: UpdateList<T>) {
        dataHelper.setUpdateList(updateList)
    }

    open override fun getItemCount(): Int {
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

    open fun getListData(): List<T> {
        return dataHelper.listData
    }

    /**
     * 设置刷新头部
     *
     * @param refreshHeader refreshHeader
     */
    internal open fun setRefreshHeader(refreshHeader: BaseRefreshHeader?) {
        val newHeaderViews = getNewHeaderViewList()
        if (refreshHeader == null) {
            newHeaderViews.remove(SPECIAL_ITEM_TYPE_REFRESH_HEADER)
        } else {
            newHeaderViews.put(SPECIAL_ITEM_TYPE_REFRESH_HEADER, refreshHeader)
        }
        updateHeader(newHeaderViews)
    }

    /**
     * 设置加载footer
     *
     * @param loadMoreFooter loadMoreFooter
     */
    internal open fun setLoadMoreFooter(loadMoreFooter: BaseLoadMoreFooter?) {
        val newFooterViews = getNewFooterViewList()
        if (loadMoreFooter == null) {
            newFooterViews.remove(SPECIAL_ITEM_TYPE_LOAD_FOOTER + BASE_ITEM_TYPE_FOOTER)
        } else {
            newFooterViews.put(
                SPECIAL_ITEM_TYPE_LOAD_FOOTER + BASE_ITEM_TYPE_FOOTER, loadMoreFooter
            )
        }
        updateFooter(newFooterViews)
    }

    internal open fun getLoadMoreFooter(): BaseLoadMoreFooter? {
        return mFootViews.get(SPECIAL_ITEM_TYPE_LOAD_FOOTER + BASE_ITEM_TYPE_FOOTER) as BaseLoadMoreFooter?
    }

    /**
     * 添加Header
     *
     * @param view view
     */
    open fun addHeaderView(view: View) {
        val newHeaderViews = getNewHeaderViewList()
        val index = newHeaderViews.indexOfValue(view)
        if (index != -1) {
            newHeaderViews.removeAt(index)
        }
        newHeaderViews.put(
            newHeaderViews.size() + BASE_ITEM_TYPE_HEADER,
            view
        )
        updateHeader(newHeaderViews)
    }

    /**
     * 删除Header
     *
     * @param view view
     */
    open fun removeHeaderView(view: View) {
        val newHeaderViews = getNewHeaderViewList()
        val index = newHeaderViews.indexOfValue(view)
        if (index != -1) {
            newHeaderViews.removeAt(index)
            updateHeader(newHeaderViews)
        }
    }

    /**
     * 添加Footer
     *
     * @param view view
     */
    open fun addFooterView(view: View?) {
        val newFooterViews = getNewFooterViewList()
        val index = newFooterViews.indexOfValue(view)
        if (index != -1) {
            newFooterViews.removeAt(index)
        }
        newFooterViews.put(
            newFooterViews.size() + BASE_ITEM_TYPE_FOOTER,
            view
        )
        updateFooter(newFooterViews)
    }

    /**
     * 删除Footer
     *
     * @param view view
     */
    open fun removeFooterView(view: View) {
        val newFooterViews = getNewFooterViewList()
        val index = newFooterViews.indexOfValue(view)
        if (index != -1) {
            newFooterViews.removeAt(index)
            updateFooter(newFooterViews)
        }
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

    fun setDataChangedListener(listener: RLListDataHelper2.DataUpdatedListener<T>?) {
        dataHelper.dataUpdatedListener = listener
    }

    private fun updateHeader(newHeadArray: SparseArrayCompat<View>) {
        val result = DiffUtil.calculateDiff(DiffHeaderFootCallBack(mHeaderViews, newHeadArray))
        mHeaderViews.clear()
        mHeaderViews.putAll(newHeadArray)
        result.dispatchUpdatesTo(this)
    }

    private fun updateFooter(newFootArray: SparseArrayCompat<View>) {
        val result = DiffUtil.calculateDiff(DiffHeaderFootCallBack(mFootViews, newFootArray))
        mFootViews.clear()
        mFootViews.putAll(newFootArray)
        result.dispatchUpdatesTo(adapterFootCallBack)
    }

    /**
     * 获取一个新的包含脚部数据的列表
     */
    fun getNewFooterViewList(): SparseArrayCompat<View> {
        val newFooterViews = SparseArrayCompat<View>()
        newFooterViews.putAll(mFootViews)
        return newFooterViews
    }

    /**
     * 获取一个新的包含头部数据的列表
     */
    fun getNewHeaderViewList(): SparseArrayCompat<View> {
        val newHeaderViews = SparseArrayCompat<View>()
        newHeaderViews.putAll(mHeaderViews)
        return newHeaderViews
    }
}