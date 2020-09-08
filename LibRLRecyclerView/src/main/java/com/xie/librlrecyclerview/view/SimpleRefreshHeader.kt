package com.xie.librlrecyclerview.view

import android.content.Context
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import com.xie.librlrecyclerview.R
import com.xie.librlrecyclerview.base.BaseRefreshHeader

/**
 * Created by Anthony on 2020/9/4.
 * Describe:
 */
class SimpleRefreshHeader(context: Context) : BaseRefreshHeader(context) {
    lateinit var textView: TextView
    override fun getRefreshingContentHeight(): Int = 200

    override fun getMaxHeight(): Int = -1

    override fun getContentView(context: Context): View {
        val linearLayout = LinearLayout(context)
        linearLayout.gravity = Gravity.BOTTOM
        textView = TextView(context)
        val layoutParams =
            LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, getRefreshingContentHeight())
        textView.textSize = 20f
        textView.gravity = Gravity.CENTER
        textView.text = "继续下拉刷新"
        linearLayout.addView(textView, layoutParams)
        return linearLayout
    }

    override fun onRefreshing() {
        textView.setText(R.string.refresh_header_refreshing)
    }

    override fun onRefreshFinish() {
        textView.setText(R.string.refresh_header_refresh_finish)
    }

    override fun onRefreshPrepare() {
        textView.setText(R.string.refresh_header_loosen_refresh)
    }

    override fun onRefreshNormal() {
        textView.setText(R.string.refresh_header_continue_drop_tips)
    }

}