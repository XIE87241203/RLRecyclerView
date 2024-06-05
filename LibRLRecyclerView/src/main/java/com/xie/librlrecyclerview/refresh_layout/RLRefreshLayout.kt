package com.xie.librlrecyclerview.refresh_layout

import android.animation.Animator
import android.animation.ValueAnimator
import android.content.Context
import android.os.Build
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.view.ViewGroup
import android.widget.AbsListView
import android.widget.ListView
import androidx.core.view.NestedScrollingChild
import androidx.core.view.NestedScrollingChildHelper
import androidx.core.view.NestedScrollingParent
import androidx.core.view.NestedScrollingParentHelper
import androidx.core.view.ViewCompat
import androidx.core.widget.ListViewCompat
import com.xie.librlrecyclerview.recycler_view.other.OnRefreshListener
import com.xie.librlrecyclerview.recycler_view.model.RefreshState
import kotlin.math.abs

/**
 * @Author XIE
 * @Date 2024/6/3
 * @Description 带有刷新头部的布局
 */
class RLRefreshLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    detStyleAttr: Int = 0
) : ViewGroup(context, attrs, detStyleAttr), NestedScrollingParent, NestedScrollingChild {

    private var mTarget: View? = null // the target of the gesture

    /**
     * 判定滚动的阈值，超过此阈值被判定为滚动，以像素为单位
     */
    private var mTouchSlop = 0

    // If nested scrolling is enabled, the total amount that needed to be
    // consumed by this as the nested scrolling parent is used in place of the
    // overscroll determined by MOVE events in the onTouch handler
    private var mTotalUnconsumed = 0f
    private var mNestedScrollingParentHelper: NestedScrollingParentHelper
    private var mNestedScrollingChildHelper: NestedScrollingChildHelper
    private val mParentScrollConsumed = IntArray(2)
    private val mParentOffsetInWindow = IntArray(2)
    private var mNestedScrollInProgress = false

    private var mInitialMotionY = 0f
    private var mInitialDownY = 0f
    private var mIsBeingDragged = false
    private var mActivePointerId = INVALID_POINTER
    private val mRefreshUIView: SimpleRefreshHeader by lazy { SimpleRefreshHeader(context) }
    private var mChildScrollUpCallback: OnChildScrollUpCallback? = null
    private var mCircleViewIndex = -1

    private var refreshState = RefreshState.REFRESH_NORMAL
    private var releaseAnimator: ValueAnimator? = null
    private var isAnimatorCancel = false
    private var onRefreshListener: OnRefreshListener? = null
    private var mNotify = true

    companion object {
        private val LOG_TAG: String = RLRefreshLayout::class.java.getSimpleName()
        private const val INVALID_POINTER = -1
        private const val REFRESH_HEIGHT_FACTOR: Float = 0.9f//下拉高度超过90%就判定为需要刷新
        private const val MOVE_RESISTANCE_FACTOR: Float = 0.6f //头部滑动的阻力系数
    }

    init {
        //获取判定滚动的阈值
        mTouchSlop = ViewConfiguration.get(context).scaledTouchSlop
        //设置不需要绘制
        setWillNotDraw(false)
        isChildrenDrawingOrderEnabled = true
        //嵌套滑动相关
        mNestedScrollingParentHelper = NestedScrollingParentHelper(this)
        mNestedScrollingChildHelper = NestedScrollingChildHelper(this)
        isNestedScrollingEnabled = true
        addView(mRefreshUIView, 0)
        reset()
    }


    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        val width = measuredWidth
        val height = measuredHeight
        if (childCount == 0) {
            return
        }
        if (mTarget == null) {
            ensureTarget()
        }
        if (mTarget == null) {
            return
        }
        val child: View? = mTarget
        val childLeft = paddingLeft
        val childTop = paddingTop
        val childWidth = width - paddingLeft - paddingRight
        val childHeight = height - paddingTop - paddingBottom
        child?.layout(childLeft, childTop, childLeft + childWidth, childTop + childHeight)
        //刷新指示器在列表头顶，隐藏在屏幕顶部
        val refreshUITop = paddingTop
        mRefreshUIView.layout(
            paddingLeft, refreshUITop - childHeight,
            childLeft + childWidth, refreshUITop
        )
    }

    public override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        if (mTarget == null) {
            ensureTarget()
        }
        if (mTarget == null) {
            return
        }

        mTarget?.measure(
            MeasureSpec.makeMeasureSpec(
                measuredWidth - paddingLeft - paddingRight,
                MeasureSpec.EXACTLY
            ), MeasureSpec.makeMeasureSpec(
                measuredHeight - paddingTop - paddingBottom, MeasureSpec.EXACTLY
            )
        )
        mRefreshUIView.measure(
            MeasureSpec.makeMeasureSpec(
                measuredWidth - paddingLeft - paddingRight,
                MeasureSpec.EXACTLY
            ),
            MeasureSpec.makeMeasureSpec(
                measuredHeight - paddingTop - paddingBottom,
                MeasureSpec.EXACTLY
            )
        )

        // TODO: 添加头部view的时候要从新定位位置
        mCircleViewIndex = -1

        // Get the index of the circleview.
        for (index in 0 until childCount) {
            if (getChildAt(index) == mRefreshUIView) {
                mCircleViewIndex = index
                break
            }
        }
    }

    override fun getChildDrawingOrder(childCount: Int, drawingPosition: Int): Int {
        //让刷新view在最开始绘制
        return if (mCircleViewIndex < 0) {
            drawingPosition
        } else if (drawingPosition == 0) {
            // Draw the selected child last
            mCircleViewIndex
        } else if (drawingPosition <= mCircleViewIndex) {
            // Move the children after the selected child earlier one
            drawingPosition - 1
        } else {
            // Keep the children before the selected child the same
            drawingPosition
        }
    }


    fun reset() {
        mRefreshUIView.visibility = GONE
        scrollTo(0, 0)
        updateState(RefreshState.REFRESH_NORMAL)
        releaseAnimator?.cancel()
    }

    private fun updateState(state : RefreshState){
        refreshState = state
        mRefreshUIView.updateHeaderState(refreshState)
    }

    override fun setEnabled(enabled: Boolean) {
        super.setEnabled(enabled)
        if (!enabled) {
            reset()
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        reset()
    }

    /**
     * Set the listener to be notified when a refresh is triggered via the swipe
     * gesture.
     */
    fun setOnRefreshListener(listener: OnRefreshListener?) {
        onRefreshListener = listener
    }


    /**
     * Notify the widget that refresh state has changed. Do not call this when
     * refresh is triggered by a swipe gesture.
     *
     * @param refreshing Whether or not the view should show refresh progress.
     */
    fun setRefreshing(refreshing: Boolean) {
        if (refreshing) {
            if (refreshState != RefreshState.REFRESHING) {
                if (mRefreshUIView.visibility != VISIBLE) {
                    mRefreshUIView.visibility = VISIBLE
                }
                //开始刷新
                releaseAnimator?.cancel()
                mNotify = false
                updateState(RefreshState.REFRESH_PREPARE)
                finishSpinner()
            }
        } else {
            if (refreshState != RefreshState.REFRESH_NORMAL) {
                releaseAnimator?.cancel()
                mNotify = false
                updateState(RefreshState.REFRESH_FINISH)
                finishSpinner()
            }
        }
    }

    /**
     * @return Whether the SwipeRefreshWidget is actively showing refresh
     * progress.
     */
    fun isRefreshing(): Boolean {
        return mRefreshUIView.isRefresh()
    }

    private fun ensureTarget() {
        // Don't bother getting the parent height if the parent hasn't been laid
        // out yet.
        if (mTarget == null) {
            for (i in 0 until childCount) {
                val child = getChildAt(i)
                if (child != mRefreshUIView) {
                    mTarget = child
                    break
                }
            }
        }
    }


    /**
     * @return Whether it is possible for the child view of this layout to
     * scroll up. Override this if the child view is a custom view.
     */
    fun canChildScrollUp(): Boolean {
        val childScrollUpCallback = mChildScrollUpCallback
        if (childScrollUpCallback != null) {
            return childScrollUpCallback.canChildScrollUp(this, mTarget)
        }
        val target = mTarget ?: return false
        if (target is ListView) {
            return ListViewCompat.canScrollList(target, -1)
        }
        return target.canScrollVertically(-1)
    }

    /**
     * 设置一个回调覆盖以 [RLRefreshLayout.canChildScrollUp] 逻辑. 非空的回调会忽略内部逻辑.
     * @param callback 呼叫[RLRefreshLayout.canChildScrollUp]时的回调
     */
    fun setOnChildScrollUpCallback(callback: OnChildScrollUpCallback?) {
        mChildScrollUpCallback = callback
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        ensureTarget()

        val action = ev.actionMasked
        val pointerIndex: Int


        if (!isEnabled || canChildScrollUp() || mNestedScrollInProgress
        ) {
            //如果我們不處於可以滑動的狀態，則返回
            return false
        }

        when (action) {
            MotionEvent.ACTION_DOWN -> {
                mActivePointerId = ev.getPointerId(0)
                mIsBeingDragged = false

                pointerIndex = ev.findPointerIndex(mActivePointerId)
                if (pointerIndex < 0) {
                    //过滤指针不存在情况
                    return false
                }
                mInitialDownY = ev.getY(pointerIndex)
            }

            MotionEvent.ACTION_MOVE -> {
                if (mActivePointerId == INVALID_POINTER) {
                    //没有活动指标id
                    Log.e(LOG_TAG, "Got ACTION_MOVE event but don't have an active pointer id.")
                    return false
                }

                pointerIndex = ev.findPointerIndex(mActivePointerId)
                if (pointerIndex < 0) {
                    //过滤指针不存在情况
                    return false
                }
                val y = ev.getY(pointerIndex)
                startDragging(y)
            }

            MotionEvent.ACTION_POINTER_UP -> onSecondaryPointerUp(ev)
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                mIsBeingDragged = false
                mActivePointerId = INVALID_POINTER
            }
        }
        Log.i(LOG_TAG, "onInterceptTouchEvent: $mIsBeingDragged")
        return mIsBeingDragged
    }

    override fun requestDisallowInterceptTouchEvent(b: Boolean) {
        // if this is a List < L or another view that doesn't support nested
        // scrolling, ignore this request so that the vertical scroll event
        // isn't stolen
        if ((Build.VERSION.SDK_INT < 21 && mTarget is AbsListView)
            || (mTarget != null && !ViewCompat.isNestedScrollingEnabled(mTarget!!))
        ) {
            // Nope.
        } else {
            super.requestDisallowInterceptTouchEvent(b)
        }
    }


    // NestedScrollingParent

    override fun onStartNestedScroll(child: View, target: View, nestedScrollAxes: Int): Boolean {
        return isEnabled && (nestedScrollAxes and ViewCompat.SCROLL_AXIS_VERTICAL) != 0
    }

    override fun onNestedScrollAccepted(child: View, target: View, axes: Int) {
        // Reset the counter of how much leftover scroll needs to be consumed.
        mNestedScrollingParentHelper.onNestedScrollAccepted(child, target, axes)
        // Dispatch up to the nested parent
        startNestedScroll(axes and ViewCompat.SCROLL_AXIS_VERTICAL)
        mTotalUnconsumed = 0f
        mNestedScrollInProgress = true
    }

    override fun getNestedScrollAxes(): Int {
        return mNestedScrollingParentHelper.nestedScrollAxes
    }

    override fun onStopNestedScroll(target: View) {
        mNestedScrollingParentHelper.onStopNestedScroll(target)
        mNestedScrollInProgress = false
        // Finish the spinner for nested scrolling if we ever consumed any
        // unconsumed nested scroll
        if (mTotalUnconsumed > 0) {
            mNotify = true
            finishSpinner()
            mTotalUnconsumed = 0f
        }
        // Dispatch up our nested parent
        stopNestedScroll()
    }

    override fun onNestedScroll(
        target: View,
        dxConsumed: Int,
        dyConsumed: Int,
        dxUnconsumed: Int,
        dyUnconsumed: Int
    ) {
        // Dispatch up to the nested parent first
        dispatchNestedScroll(
            dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed,
            mParentOffsetInWindow
        )

        // This is a bit of a hack. Nested scrolling works from the bottom up, and as we are
        // sometimes between two nested scrolling views, we need a way to be able to know when any
        // nested scrolling parent has stopped handling events. We do that by using the
        // 'offset in window 'functionality to see if we have been moved from the event.
        // This is a decent indication of whether we should take over the event stream or not.
        val dy = dyUnconsumed + mParentOffsetInWindow[1]
        if (!isBusy() && dy < 0 && !canChildScrollUp()) {
            //列表往上滚并且子view不能往上滚
            mTotalUnconsumed += abs(dy)
            moveSpinner(mTotalUnconsumed)
        }
    }

    override fun onNestedPreScroll(target: View, dx: Int, dy: Int, consumed: IntArray) {
        // If we are in the middle of consuming, a scroll, then we want to move the spinner back up
        // before allowing the list to scroll
        Log.i(LOG_TAG, "onNestedPreScroll dy:" + dy)
        //列表往下滚并且未刷新UI有高度
        //繁忙时不能做滑动动作
        if (!isBusy() && dy > 0 && mTotalUnconsumed > 0) {
            if (dy > mTotalUnconsumed) {
                //下滑大小比高度大
                //消耗掉对应的高度
                consumed[1] = dy - mTotalUnconsumed.toInt()
                mTotalUnconsumed = 0f
            } else {
                //消耗掉对应的高度
                mTotalUnconsumed -= dy.toFloat()
                consumed[1] = dy
            }
            moveSpinner(mTotalUnconsumed)
        }

        //让父级消耗掉剩下的东西
        // Now let our nested parent consume the leftovers
        val parentConsumed = mParentScrollConsumed
        if (dispatchNestedPreScroll(dx - consumed[0], dy - consumed[1], parentConsumed, null)) {
            consumed[0] += parentConsumed[0]
            consumed[1] += parentConsumed[1]
        }
    }

    // NestedScrollingChild
    override fun setNestedScrollingEnabled(enabled: Boolean) {
        mNestedScrollingChildHelper.isNestedScrollingEnabled = enabled
    }

    override fun isNestedScrollingEnabled(): Boolean {
        return mNestedScrollingChildHelper.isNestedScrollingEnabled
    }

    override fun startNestedScroll(axes: Int): Boolean {
        return mNestedScrollingChildHelper.startNestedScroll(axes)
    }

    override fun stopNestedScroll() {
        mNestedScrollingChildHelper.stopNestedScroll()
    }

    override fun hasNestedScrollingParent(): Boolean {
        return mNestedScrollingChildHelper.hasNestedScrollingParent()
    }

    override fun dispatchNestedScroll(
        dxConsumed: Int, dyConsumed: Int, dxUnconsumed: Int,
        dyUnconsumed: Int, offsetInWindow: IntArray?
    ): Boolean {
        return mNestedScrollingChildHelper.dispatchNestedScroll(
            dxConsumed, dyConsumed,
            dxUnconsumed, dyUnconsumed, offsetInWindow
        )
    }

    override fun dispatchNestedPreScroll(
        dx: Int,
        dy: Int,
        consumed: IntArray?,
        offsetInWindow: IntArray?
    ): Boolean {
        return mNestedScrollingChildHelper.dispatchNestedPreScroll(
            dx, dy, consumed, offsetInWindow
        )
    }

    override fun onNestedPreFling(
        target: View, velocityX: Float,
        velocityY: Float
    ): Boolean {
        return dispatchNestedPreFling(velocityX, velocityY)
    }

    override fun onNestedFling(
        target: View, velocityX: Float, velocityY: Float,
        consumed: Boolean
    ): Boolean {
        return dispatchNestedFling(velocityX, velocityY, consumed)
    }

    override fun dispatchNestedFling(
        velocityX: Float,
        velocityY: Float,
        consumed: Boolean
    ): Boolean {
        return mNestedScrollingChildHelper.dispatchNestedFling(velocityX, velocityY, consumed)
    }

    override fun dispatchNestedPreFling(velocityX: Float, velocityY: Float): Boolean {
        return mNestedScrollingChildHelper.dispatchNestedPreFling(velocityX, velocityY)
    }


    private fun moveSpinner(overscrollTop: Float) {
        //滑动指示器
        if (mRefreshUIView.visibility != VISIBLE) {
            mRefreshUIView.visibility = VISIBLE
        }
        //刷新中，刷新完成归位时不能拖动
        if (isBusy()) return
        releaseAnimator?.let { if (it.isStarted) it.cancel() }

        var tempHeight: Int = (overscrollTop * MOVE_RESISTANCE_FACTOR).toInt()
        val refreshMaxHeight = mRefreshUIView.getMaxHeight()
        if (refreshMaxHeight != -1 && tempHeight > refreshMaxHeight) {
            tempHeight = refreshMaxHeight
        }
        scrollTo(0, -tempHeight)
        Log.i(LOG_TAG, "onDragMove: refreshState:$refreshState allOffset:$tempHeight");
        if (tempHeight >= mRefreshUIView.getRefreshingContentHeight() * REFRESH_HEIGHT_FACTOR) {
            //达到刷新需要高度（完全展示内容）
            if (refreshState != RefreshState.REFRESH_PREPARE) {
                updateState(RefreshState.REFRESH_PREPARE)
            }
        } else {
            //没达到刷新需要高度
            if (refreshState != RefreshState.REFRESH_NORMAL) {
                updateState(RefreshState.REFRESH_NORMAL)
            }
        }
    }

    private fun finishSpinner() {
        val offset = -scrollY
        Log.i(LOG_TAG, "finishSpinner: $offset")
        if (refreshState == RefreshState.REFRESHING) return
        //判断是否可以开始刷新
        if (refreshState == RefreshState.REFRESH_PREPARE) {
            //开始刷新
            val startHeight = offset
            val endHeight = mRefreshUIView.getRefreshingContentHeight()
            updateState(RefreshState.REFRESH_PREPARE)
            showScrollAnimator(startHeight, endHeight)
        } else {
            //不到可以刷新的高度
            val mOffset = offset
            if (mOffset == BaseRefreshHeader.MIN_HEIGHT) {
                //不需要播放动画
                updateState(RefreshState.REFRESH_NORMAL)
            } else {
                //播放动画
                showScrollAnimator(mOffset, BaseRefreshHeader.MIN_HEIGHT)
            }
        }
    }


    /**
     * 播放滚动的动画
     *
     * @param startHeight startHeight
     * @param endHeight   endHeight
     */
    private fun showScrollAnimator(startHeight: Int, endHeight: Int) {
        releaseAnimator?.cancel()
        releaseAnimator = ValueAnimator.ofInt(startHeight, endHeight)
        releaseAnimator?.let {
            it.duration = 200
            it.addUpdateListener { animation: ValueAnimator ->
                val value = animation.animatedValue as Int
                scrollTo(0, -value)
            }
            it.addListener(object : Animator.AnimatorListener {
                override fun onAnimationStart(animation: Animator) {}
                override fun onAnimationEnd(animation: Animator) {
                    if (!isAnimatorCancel) {
                        when (refreshState) {
                            RefreshState.REFRESH_PREPARE -> {
                                updateState(RefreshState.REFRESHING)
                                if(mNotify){
                                    onRefreshListener?.onRefresh()
                                }
                            }

                            RefreshState.REFRESH_FINISH ->{
                                updateState(RefreshState.REFRESH_NORMAL)
                            }

                            else -> {}
                        }
                    } else {
                        isAnimatorCancel = false
                    }
                }

                override fun onAnimationCancel(animation: Animator) {
                    isAnimatorCancel = true
                }

                override fun onAnimationRepeat(animation: Animator) {}
            })
            //开启
            it.start()
        }
    }

    private fun isBusy(): Boolean {
        return refreshState == RefreshState.REFRESHING || refreshState == RefreshState.REFRESH_FINISH
    }


    override fun onTouchEvent(ev: MotionEvent): Boolean {
        val action = ev.actionMasked
        var pointerIndex = -1

        if (!isEnabled || canChildScrollUp() || mNestedScrollInProgress
        ) {
            // Fail fast if we're not in a state where a swipe is possible
            return false
        }

        when (action) {
            MotionEvent.ACTION_DOWN -> {
                mActivePointerId = ev.getPointerId(0)
                mIsBeingDragged = false
            }

            MotionEvent.ACTION_MOVE -> {
                Log.i(LOG_TAG, "ACTION_MOVE")
                pointerIndex = ev.findPointerIndex(mActivePointerId)
                if (pointerIndex < 0) {
                    Log.e(LOG_TAG, "Got ACTION_MOVE event but have an invalid active pointer id.")
                    return false
                }

                val y = ev.getY(pointerIndex)
                startDragging(y)

                if (mIsBeingDragged) {
                    val overscrollTop = (y - mInitialMotionY)
                    if (overscrollTop > 0) {
                        Log.i(LOG_TAG, "onTouchEvent: " + mTotalUnconsumed)
                        moveSpinner(overscrollTop)
                    } else {
                        return false
                    }
                }
            }

            MotionEvent.ACTION_POINTER_DOWN -> {
                pointerIndex = ev.actionIndex
                if (pointerIndex < 0) {
                    Log.e(
                        LOG_TAG,
                        "Got ACTION_POINTER_DOWN event but have an invalid action index."
                    )
                    return false
                }
                mActivePointerId = ev.getPointerId(pointerIndex)
            }

            MotionEvent.ACTION_POINTER_UP -> onSecondaryPointerUp(ev)
            MotionEvent.ACTION_UP -> {
                pointerIndex = ev.findPointerIndex(mActivePointerId)
                if (pointerIndex < 0) {
                    Log.e(LOG_TAG, "Got ACTION_UP event but don't have an active pointer id.")
                    return false
                }

                if (mIsBeingDragged) {
                    val y = ev.getY(pointerIndex)
                    val overscrollTop = (y - mInitialMotionY)
                    mIsBeingDragged = false
                    mNotify = true
                    finishSpinner()
                }
                mActivePointerId = INVALID_POINTER
                return false
            }

            MotionEvent.ACTION_CANCEL -> return false
        }
        Log.i(LOG_TAG, "onTouchEvent: $mIsBeingDragged")
        return true
    }

    private fun startDragging(y: Float) {
        val yDiff = y - mInitialDownY
        if (yDiff > mTouchSlop && !mIsBeingDragged) {
            mInitialMotionY = mInitialDownY + mTouchSlop
            mIsBeingDragged = true
        }
    }

    private fun onSecondaryPointerUp(ev: MotionEvent) {
        val pointerIndex = ev.actionIndex
        val pointerId = ev.getPointerId(pointerIndex)
        if (pointerId == mActivePointerId) {
            // This was our active pointer going up. Choose a new
            // active pointer and adjust accordingly.
            val newPointerIndex = if (pointerIndex == 0) 1 else 0
            mActivePointerId = ev.getPointerId(newPointerIndex)
        }
    }


    /**
     * Classes that wish to override [RLRefreshLayout.canChildScrollUp] method
     * behavior should implement this interface.
     */
    interface OnChildScrollUpCallback {
        /**
         * Callback that will be called when [RLRefreshLayout.canChildScrollUp] method
         * is called to allow the implementer to override its behavior.
         *
         * @param parent RLRefreshLayout that this callback is overriding.
         * @param child The child view of RLRefreshLayout.
         *
         * @return Whether it is possible for the child view of parent layout to scroll up.
         */
        fun canChildScrollUp(
            parent: RLRefreshLayout,
            child: View?
        ): Boolean
    }
}