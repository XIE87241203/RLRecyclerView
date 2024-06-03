package com.xie.librlrecyclerview.base

import android.animation.Animator
import android.animation.ValueAnimator
import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.recyclerview.widget.RecyclerView
import com.xie.librlrecyclerview.other.RefreshHeaderState
import com.xie.librlrecyclerview.other.LogUtil
import com.xie.librlrecyclerview.other.OnRefreshListener

/**
 * Created by Anthony on 2020/9/4.
 * Describe:
 */
abstract class BaseRefreshHeader : LinearLayout {
    companion object {
        const val MIN_HEIGHT = 0//最小高度
        const val REFRESH_HEIGHT_FACTOR: Float = 0.9f//下拉高度超过90%就判定为需要刷新
        const val MOVE_RESISTANCE_FACTOR: Float = 2.5f //头部滑动的阻力系数
    }

    private var releaseAnimator: ValueAnimator? = null
    internal var state = RefreshHeaderState.REFRESH_NORMAL
    var allOffset: Float = 0.0f//当前总位移
    private var isAnimatorCancel = false
    var onRefreshListener: OnRefreshListener? = null


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
        val myLayoutParams = RecyclerView.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        val lp = LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        lp.gravity = Gravity.BOTTOM
        myLayoutParams.setMargins(0,-MIN_HEIGHT,0,0)
        layoutParams = myLayoutParams
        val contentView = getContentView(context)
        addView(contentView, lp)
        setVisibleHeight(MIN_HEIGHT.toFloat())
    }

    /**
     * 触发刷新UI显示
     */
    internal fun startRefresh() {
        val startHeight = layoutParams.height
        val endHeight = getRefreshingContentHeight()
        updateHeaderState(RefreshHeaderState.REFRESH_PREPARE)
        showHeightAnimator(startHeight.toFloat(), endHeight.toFloat())
    }

    internal fun finishRefresh() {
        updateHeaderState(RefreshHeaderState.REFRESH_FINISH)
        onRelease()
    }

    /**
     * 设置Header的显示高度
     */
    open fun setVisibleHeight(height: Float) {
        var visibleHeight = height
        if (visibleHeight < MIN_HEIGHT) visibleHeight = MIN_HEIGHT.toFloat()
        val lp = layoutParams
        lp.height = visibleHeight.toInt()
        requestLayout()
        allOffset = visibleHeight
    }

    /**
     * 下拉移动
     *  @param offset 单次移动的位移
     */
    fun onDragMove(offset: Float) {
        if (state == RefreshHeaderState.REFRESHING || state == RefreshHeaderState.REFRESH_FINISH) return
        releaseAnimator?.let {
            if (it.isStarted) it.cancel()
        }
        var tempHeight: Float = offset + allOffset
        if (getMaxHeight() != -1 && tempHeight > getMaxHeight()) {
            tempHeight = getMaxHeight().toFloat()
        }
        setVisibleHeight(tempHeight)
        if (allOffset >= getRefreshingContentHeight() * REFRESH_HEIGHT_FACTOR) {
            //达到刷新需要高度（完全展示内容）
            LogUtil.i("onDragMove: state:$state allOffset:$allOffset");
            if (state != RefreshHeaderState.REFRESH_PREPARE) {
                updateHeaderState(RefreshHeaderState.REFRESH_PREPARE)
            }
        } else {
            //没达到刷新需要高度
            LogUtil.i("onDragMove: state:$state allOffset:$allOffset");
            if (state != RefreshHeaderState.REFRESH_NORMAL) {
                updateHeaderState(RefreshHeaderState.REFRESH_NORMAL)
            }
        }
    }

    /**
     * 下拉松开
     */
    fun onRelease() {
        //刷新中不处理
        if (state == RefreshHeaderState.REFRESHING) return
        //判断是否可以开始刷新
        if (state == RefreshHeaderState.REFRESH_PREPARE) {
            //开始刷新
            startRefresh()
        } else {
            //不到可以刷新的高度
            val height = layoutParams.height
            if (height == MIN_HEIGHT) {
                //不需要播放动画
                updateHeaderState(RefreshHeaderState.REFRESH_NORMAL)
                return
            } else {
                //播放动画
                showHeightAnimator(height.toFloat(), MIN_HEIGHT.toFloat())
            }
        }
    }

    /**
     * 播放改变高度的动画
     *
     * @param startHeight startHeight
     * @param endHeight   endHeight
     */
    private fun showHeightAnimator(startHeight: Float, endHeight: Float) {
        releaseAnimator?.cancel()
        //不刷新，隐藏
        releaseAnimator = ValueAnimator.ofFloat(startHeight, endHeight)
        releaseAnimator?.let {
            it.duration = 200
            it.addUpdateListener { animation: ValueAnimator ->
                val value = animation.animatedValue as Float
                setVisibleHeight(value)
            }
            it.addListener(object : Animator.AnimatorListener {
                override fun onAnimationStart(animation: Animator) {}
                override fun onAnimationEnd(animation: Animator) {
                    if (!isAnimatorCancel) {
                        when (state) {
                            RefreshHeaderState.REFRESH_PREPARE -> updateHeaderState(
                                RefreshHeaderState.REFRESHING
                            )
                            RefreshHeaderState.REFRESH_FINISH -> updateHeaderState(
                                RefreshHeaderState.REFRESH_NORMAL
                            )
                            else ->{}
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

    open fun getVisibleHeight(): Float {
        return allOffset
    }

    protected open fun updateHeaderState(state: RefreshHeaderState) {
        this.state = state
        when (state) {
            RefreshHeaderState.REFRESH_NORMAL -> onRefreshNormal()
            RefreshHeaderState.REFRESH_PREPARE -> onRefreshPrepare()
            RefreshHeaderState.REFRESH_FINISH -> onRefreshFinish()
            RefreshHeaderState.REFRESHING -> {
                onRefreshListener?.onRefresh()
                onRefreshing()
            }
        }
    }

    abstract fun onRefreshing()
    abstract fun onRefreshFinish()
    abstract fun onRefreshPrepare()
    abstract fun onRefreshNormal()

    /**
     * 获取刷新时布局内容的高度
     */
    abstract fun getRefreshingContentHeight(): Int

    /**
     * 获取刷新布局最大下拉高度，-1为无限大
     *
     * @return 刷新布局最大下拉高度
     */
    abstract fun getMaxHeight(): Int
    abstract fun getContentView(context: Context): View

    fun getState(): RefreshHeaderState {
        return state
    }
}