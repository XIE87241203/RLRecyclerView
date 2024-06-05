package com.xie.librlrecyclerview.view

import android.content.Context
import android.util.AttributeSet
import androidx.recyclerview.widget.RecyclerView
import com.xie.librlrecyclerview.base.BaseLoadMoreFooter
import com.xie.librlrecyclerview.base.RLRecyclerAdapter
import com.xie.librlrecyclerview.other.OnLoadMoreListener
import com.xie.librlrecyclerview.other.RLRecyclerState

/**
 * Created by Anthony on 2020/9/4.
 * Describe:
 */
open class RLRecyclerView : RecyclerView {
    private var rlAdapter: RLRecyclerAdapter<*>? = null

    /**
     * 加载开关
     */
    private var autoLoadMoreEnable = false

    /**
     * 剩下多少个开始自动加载
     */
    private var loadMoreKey = 1

    var loadMoreFooter: BaseLoadMoreFooter //加载尾部
        set(value) {
            field = value
            initLoadMoreFooter(value)
        }

    /**
     * 加载回调
     */
    var onLoadMoreListener: OnLoadMoreListener? = null

    init {
        loadMoreFooter = SimpleLoadMoreFooter(context)
    }

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet?, detStyleAttr: Int) : super(
        context,
        attrs,
        detStyleAttr
    )

    /**
     * 根据状态刷新列表头部，尾部显示UI
     */
    fun setRLState(state: RLRecyclerState) {
        when (state) {
            RLRecyclerState.NORMAL -> {
                loadMoreFooter.finishLoadMore()
            }

            RLRecyclerState.LOAD_MORE_ERROR -> {
                loadMoreFooter.showLoadMoreError()
            }

            RLRecyclerState.LOAD_MORE_LAST_PAGE -> {
                loadMoreFooter.showLastPage()
            }

            RLRecyclerState.LOAD_MORE_LOADING -> {
                loadMoreFooter.showLoadMoreLoading()
            }
        }
    }

    override fun setAdapter(adapter: Adapter<*>?) {
        super.setAdapter(adapter)
        if (adapter is RLRecyclerAdapter<*>) {
            rlAdapter = adapter
            initAutoLoadMore()
        }
    }

    fun setAutoLoadMoreEnable(enable: Boolean, key: Int) {
        loadMoreKey = key
        autoLoadMoreEnable = enable
        initAutoLoadMore()
    }

    private fun initAutoLoadMore() {
        rlAdapter?.let {
            if (autoLoadMoreEnable) {
                it.loadMoreKey = loadMoreKey
                it.setLoadMoreFooter(loadMoreFooter)
            } else {
                it.loadMoreKey = -1
                it.setLoadMoreFooter(null)
            }
        }
    }

    private fun initLoadMoreFooter(footer: BaseLoadMoreFooter) {
        //添加刷新监听
        footer.onLoadMoreListener = object : OnLoadMoreListener {
            override fun onLoadMore() {
                onLoadMoreListener?.onLoadMore()
            }
        }
        rlAdapter?.setLoadMoreFooter(footer)
    }

    fun showLoadMoreLoading() {
        loadMoreFooter.showLoadMoreLoading()
    }

    fun startLoadMore() {
        loadMoreFooter.startLoadMore()
    }

    fun finishLoadMore() {
        loadMoreFooter.finishLoadMore()
    }

    fun showLoadMoreError() {
        loadMoreFooter.showLoadMoreError()
    }

    fun showLoadMoreLastPage() {
        loadMoreFooter.showLastPage()
    }

    fun isLoadMoreLoading(): Boolean {
        return loadMoreFooter.isLoadMoreLoading()
    }

    fun isNoMore(): Boolean {
        return loadMoreFooter.isNoMore()
    }

    fun isLoadMoreError(): Boolean {
        return loadMoreFooter.isLoadMoreError()
    }

    fun isLoadMoreFinish(): Boolean {
        return loadMoreFooter.isLoadMoreFinish()
    }
}