package com.xie.librlrecyclerview.view

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.xie.librlrecyclerview.base.BaseLoadMoreFooter
import com.xie.librlrecyclerview.base.BaseRefreshHeader
import com.xie.librlrecyclerview.base.RLRecyclerAdapter
import com.xie.librlrecyclerview.other.*

/**
 * Created by Anthony on 2020/9/4.
 * Describe:
 */
class RLRecyclerView : RecyclerView {
    var rlAdapter: RLRecyclerAdapter? = null
    var refreshHeader: BaseRefreshHeader //刷新头部
        set(value) {
            field = value
            initRefreshHeader(value)
        }

    /**
     * 刷新回调
     */
    var onRefreshListener: OnRefreshListener? = null

    /**
     * 刷新开关
     */
    var refreshEnable = false

    /**
     * 加载开关
     */
    var autoLoadMoreEnable = false

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
        refreshHeader = SimpleRefreshHeader(context)
        loadMoreFooter = SimpleLoadMoreFooter(context)
    }

    constructor(context: Context) : super(context) {
        initView(context)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        initView(context)
    }

    constructor(context: Context, attrs: AttributeSet?, detStyleAttr: Int) : super(
        context,
        attrs,
        detStyleAttr
    ) {
        initView(context)
    }

    private fun initView(context: Context) {
        //设置滚动监听
        addOnScrollListener(object : OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                rlAdapter?.let {
                    if (autoLoadMoreEnable && dy > 0) {
                        //上滑操作
                        checkTheBottomLoadMore(it)
                    }
                }

            }
        })
    }

    override fun setAdapter(adapter: Adapter<*>?) {
        super.setAdapter(adapter)
        if (adapter is RLRecyclerAdapter) {
            rlAdapter = adapter
            adapter.setRefreshHeader(refreshHeader)
            adapter.setLoadMoreFooter(loadMoreFooter)
        }
    }

    fun setAutoLoadMoreEnable(enable: Boolean, key: Int) {
        loadMoreKey = key
        autoLoadMoreEnable = enable
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

    /**
     * 检测是否需要自动加载
     * 当滑动到底部的时候开始自动加载更多
     */
    private fun checkTheBottomLoadMore(adapter: RLRecyclerAdapter) {
        if (layoutManager == null) return
        val startLoadIndex: Int =
            adapter.getRealItemCount() - loadMoreKey
        val num = adapter.getRealItemCount()
        //判断是否滚动到底部
        if (isLoadMoreFinish() && adapter.getRealItemCount() > 0) {
            var visibleIndex = 0
            if (layoutManager is StaggeredGridLayoutManager) {
                visibleIndex =
                    (layoutManager as StaggeredGridLayoutManager?)!!.findLastVisibleItemPositions(
                        null
                    )[0] - adapter.getHeadersCount()
            } else if (layoutManager is LinearLayoutManager) {
                visibleIndex =
                    (layoutManager as LinearLayoutManager?)!!.findLastVisibleItemPosition() - adapter.getHeadersCount()
            }
            //自动加载
            if (visibleIndex >= startLoadIndex) startLoadMore()
        }
    }

    fun isLoadMoreLoading(): Boolean {
        return loadMoreFooter.getState() == LoadMoreFooterState.LOAD_MORE_LOADING
    }

    fun isNoMore(): Boolean {
        return loadMoreFooter.getState() == LoadMoreFooterState.LOAD_MORE_LAST_PAGE
    }

    fun isLoadMoreError(): Boolean {
        return loadMoreFooter.getState() == LoadMoreFooterState.LOAD_MORE_ERROR
    }

    fun isLoadMoreFinish(): Boolean {
        return loadMoreFooter.getState() == LoadMoreFooterState.NORMAL
    }

    /*刷新部分*/

    /**
     * 触发刷新
     */
    fun startRefresh() {
        refreshHeader.startRefresh()
    }

    fun finishRefresh() {
        refreshHeader.finishRefresh()
    }

    fun isRefreshing(): Boolean {
        return refreshHeader.getState() == RefreshHeaderState.REFRESH_FINISH
    }

    private fun initRefreshHeader(header: BaseRefreshHeader) {
        //添加刷新监听
        header.onRefreshListener = object : OnRefreshListener {
            override fun onRefresh() {
                onRefreshListener?.onRefresh()
            }
        }
        rlAdapter?.setRefreshHeader(header)
    }

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        if (rlAdapter != null && refreshEnable) {
            setStartData(ev)
        }
        return super.dispatchTouchEvent(ev)
    }

    /**
     * 防止disposeRefresh没法拿到初始位置
     * @param e
     */
    fun setStartData(e: MotionEvent) {
        when (e.action) {
            MotionEvent.ACTION_DOWN -> {
                //                LogUtil.i("testMsg", "disposeRefresh: ACTION_DOWN");
                //记录与上一次移动的位移
                startY = e.rawY
                startX = e.rawX
                //记录开始点的位移
                allStartX = e.rawX
                allStartY = e.rawY
                isTouch = true
                isDispose = false
            }
        }
    }

    override fun onTouchEvent(e: MotionEvent): Boolean {
        var isRefreshDrag = false
        if (rlAdapter != null && refreshEnable) {
            isRefreshDrag = disposeRefresh(e)
        }
        //正在拖拽刷新时，消耗滑动事件
        return if (isRefreshDrag) {
            true
        } else {
            super.onTouchEvent(e)
        }
    }

    private var startY = -1F
    private var startX = -1F
    private var allStartY = -1f
    private var allStartX = -1f
    private var isTouch = false //防止惯性滑动触发刷新用
    private var isDispose = false //是否正在下拉刷新头部

    private fun disposeRefresh(e: MotionEvent): Boolean {
        when (e.action) {
            MotionEvent.ACTION_DOWN -> {
                LogUtil.i("disposeRefresh: ACTION_DOWN")
                //记录与上一次移动的位移
                startX = e.rawX
                startY = e.rawY
                //记录开始点的位移
                //记录开始点的位移
                allStartX = e.rawX
                allStartY = e.rawY
                isTouch = true
                isDispose = false
            }
            MotionEvent.ACTION_MOVE -> {
                LogUtil.i("disposeRefresh: ACTION_MOVE")
                val deltaY = e.rawY - startY
                val deltaX = e.rawX - startX
                //距离开始点的位移
                val offsetX = e.rawX - allStartX
                val offsetY = e.rawY - allStartY
                startY = e.rawY
                startX = e.rawX
                if (isTouch) {
                    if ((deltaY > 0 && checkOnTop()) || refreshHeader.getVisibleHeight() > BaseRefreshHeader.MIN_HEIGHT) {
                        //防止异常回弹(需要根据屏幕密度判断)
//                      if(Math.abs(deltaY)<100){
                        refreshHeader.onDragMove(deltaY / BaseRefreshHeader.MOVE_RESISTANCE_FACTOR)
//                      }
                        //recycleview不能滑动之后拦截滑动事件
                        isDispose = !canScrollVertically(-1)
                    } else {
                        isDispose = false
                    }
                }
                return isDispose
            }
            MotionEvent.ACTION_UP -> {
                LogUtil.i("disposeRefresh: ACTION_UP")
                refreshHeader.onRelease()
                isTouch = false
                startY = -1f
                allStartX = -1f
                allStartY = -1f
                //如果有下拉刷新触摸的话，不分发触摸事件
                return if (isDispose) {
                    isDispose = false
                    true
                } else {
                    false
                }
            }
        }
        return false
    }

    private fun checkOnTop(): Boolean {
        var index = -1
        var isFirstViewOnTop = false
        if (layoutManager is StaggeredGridLayoutManager) {
            index =
                (layoutManager as StaggeredGridLayoutManager?)!!.findFirstVisibleItemPositions(null)[0]
        } else if (layoutManager is LinearLayoutManager) {
            val linearLayoutManager = layoutManager as LinearLayoutManager?
            index = linearLayoutManager!!.findFirstVisibleItemPosition()
            if (index == 1) {
                val topView = linearLayoutManager.findViewByPosition(1)
                if (topView != null) {
                    isFirstViewOnTop = getViewTopWithOutMarginPadding(topView)
                }
            }
        }
        //分别是第一个看到的item为0；是否能向下滑动（有view隐藏时有bug，比如刷新头部）；除刷新头部外第一个看到的view的top是否为0
        return index == 0 || !canScrollVertically(-1) || isFirstViewOnTop
    }

    /**
     * 获取View的top减去边距是否为0
     *
     * @param view view
     * @return boolean
     */
    private fun getViewTopWithOutMarginPadding(view: View): Boolean {
        var marginTop = 0
        if (view.layoutParams != null) {
            marginTop = (view.layoutParams as LayoutParams).topMargin
        }
        return view.top - marginTop == 0
    }
}