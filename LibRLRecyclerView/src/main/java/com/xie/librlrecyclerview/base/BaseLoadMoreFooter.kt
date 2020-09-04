package com.xie.librlrecyclerview.base

import android.content.Context
import android.util.AttributeSet
import android.widget.RelativeLayout
import com.xie.librlrecyclerview.RLRecyclerState

/**
 * Created by Anthony on 2020/9/4.
 * Describe:
 */
abstract class BaseLoadMoreFooter : RelativeLayout {
     var state: RLRecyclerState = RLRecyclerState.NORMAL
        set(value) {
            setLoadMoreState(value)
            field = value
        }

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
        state = RLRecyclerState.NORMAL
    }

    /**
     * 设置状态
     *
     * @param state One of [.STATE_LOADING], [.STATE_LOAD_FINISH],[.STATE_LOAD_ERROR], or [.STATE_NO_MORE].
     */
    open fun setLoadMoreState(state: RLRecyclerState) {
        when (state) {
            RLRecyclerState.NORMAL -> showLoadMoreFinish()
            RLRecyclerState.LOAD_MORE_LOADING -> showLoading()
            RLRecyclerState.LOAD_MORE_ERROR -> showLoadMoreError()
            RLRecyclerState.LOAD_MORE_LAST_PAGE -> showNoMoreView()
        }
    }

    protected abstract fun init()

    protected abstract fun showLoading()

    protected abstract fun showLoadMoreFinish()

    protected abstract fun showNoMoreView()

    protected abstract fun showLoadMoreError()
}