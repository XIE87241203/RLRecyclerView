package com.xie.librlrecyclerview.refresh_layout

import android.content.Context
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import com.xie.librlrecyclerview.recycler_view.model.RefreshState

/**
 * Created by Anthony on 2020/9/4.
 * Describe:
 */
abstract class BaseRefreshHeader : LinearLayout {
    companion object {
        const val MIN_HEIGHT = 0//最小高度
    }

    internal var state = RefreshState.REFRESH_NORMAL
    private var contentHeight:Int = 0

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

    open fun initView(context: Context) {
        val contentView = getContentView(context)
        contentHeight = getRefreshingContentHeight()
        val lp = LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            contentHeight
        )
        lp.gravity = Gravity.BOTTOM
        addView(contentView, lp)
    }

    fun isRefresh():Boolean{
        return state == RefreshState.REFRESHING
    }

    fun updateHeaderState(state: RefreshState) {
        this.state = state
        when (state) {
            RefreshState.REFRESH_NORMAL -> onRefreshNormal()
            RefreshState.REFRESH_PREPARE -> onRefreshPrepare()
            RefreshState.REFRESH_FINISH -> onRefreshFinish()
            RefreshState.REFRESHING -> onRefreshing()
        }
    }

    abstract fun onRefreshing()
    abstract fun onRefreshFinish()
    abstract fun onRefreshPrepare()
    abstract fun onRefreshNormal()

    /**
     * 获取刷新时布局内容的高度
     */
    protected abstract fun getRefreshingContentHeight(): Int

    fun getContentHeight():Int{
        return contentHeight
    }

    /**
     * 获取刷新布局最大下拉高度，-1为无限大
     *
     * @return 刷新布局最大下拉高度
     */
    abstract fun getMaxHeight(): Int
    abstract fun getContentView(context: Context): View

    fun getState(): RefreshState {
        return state
    }

    fun dpToPx(context: Context, valueInDp: Float): Float {
        val metrics = context.resources.displayMetrics
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, valueInDp, metrics)
    }
}