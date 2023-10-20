package com.xie.librlrecyclerview.base

import android.content.Context
import android.util.AttributeSet
import android.widget.RelativeLayout
import com.xie.librlrecyclerview.other.LoadMoreFooterState
import com.xie.librlrecyclerview.other.OnLoadMoreListener

/**
 * Created by Anthony on 2020/9/4.
 * Describe:
 */
abstract class BaseLoadMoreFooter : RelativeLayout {
    var onLoadMoreListener: OnLoadMoreListener? = null
    internal var state: LoadMoreFooterState = LoadMoreFooterState.NORMAL

    constructor(context: Context) : super(context) {
        initView()
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        initView()
    }

    constructor(context: Context, attrs: AttributeSet?, detStyleAttr: Int) : super(
        context,
        attrs,
        detStyleAttr
    ) {
        initView()
    }

    private fun initView() {
        setPadding(0, 0, 0, 1)
        init()
        setLoadMoreState(LoadMoreFooterState.NORMAL)
    }

    /**
     * 设置状态
     *
     * @param state One of [.STATE_LOADING], [.STATE_LOAD_FINISH],[.STATE_LOAD_ERROR], or [.STATE_NO_MORE].
     */
    open fun setLoadMoreState(state: LoadMoreFooterState) {
        this.state = state
        when (state) {
            LoadMoreFooterState.NORMAL -> onLoadMoreFinish()
            LoadMoreFooterState.START_LOAD_MORE -> {
                onLoading()
                onLoadMoreListener?.onLoadMore()
            }
            LoadMoreFooterState.LOAD_MORE_LOADING -> {
                onLoading()
            }
            LoadMoreFooterState.LOAD_MORE_ERROR -> onLoadMoreError()
            LoadMoreFooterState.LOAD_MORE_LAST_PAGE -> onNoMore()
        }
    }

    fun startLoadMore() {
        setLoadMoreState(LoadMoreFooterState.START_LOAD_MORE)
    }

    fun showLoadMoreLoading(){
        setLoadMoreState(LoadMoreFooterState.LOAD_MORE_LOADING)
    }

    fun finishLoadMore() {
        setLoadMoreState(LoadMoreFooterState.NORMAL)
    }

    fun showLoadMoreError(){
        setLoadMoreState(LoadMoreFooterState.LOAD_MORE_ERROR)
    }

    fun showLastPage(){
        setLoadMoreState(LoadMoreFooterState.LOAD_MORE_LAST_PAGE)
    }

    protected abstract fun init()

    protected abstract fun onLoading()

    protected abstract fun onLoadMoreFinish()

    protected  abstract fun onNoMore()

    protected abstract fun onLoadMoreError()

    fun getState():LoadMoreFooterState{
        return state
    }

    fun isLoadMoreLoading(): Boolean {
        return getState() == LoadMoreFooterState.START_LOAD_MORE
    }

    fun isNoMore(): Boolean {
        return getState() == LoadMoreFooterState.LOAD_MORE_LAST_PAGE
    }

    fun isLoadMoreError(): Boolean {
        return getState() == LoadMoreFooterState.LOAD_MORE_ERROR
    }

    fun isLoadMoreFinish(): Boolean {
        return getState() == LoadMoreFooterState.NORMAL
    }
}