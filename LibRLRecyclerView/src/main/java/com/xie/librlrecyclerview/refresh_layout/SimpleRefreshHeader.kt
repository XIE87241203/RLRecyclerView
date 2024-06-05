package com.xie.librlrecyclerview.refresh_layout

import android.content.Context
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.widget.TextView
import com.xie.librlrecyclerview.R

/**
 * Created by Anthony on 2020/9/4.
 * Describe:
 */
class SimpleRefreshHeader(context: Context) : BaseRefreshHeader(context) {
    private lateinit var textView: TextView
    override fun getRefreshingContentHeight(): Int{
        return dpToPx(context,60f).toInt()
    }

    override fun getMaxHeight(): Int = -1

    override fun getContentView(context: Context): View {
        textView = TextView(context)
        textView.textSize = 20f
        textView.gravity = Gravity.CENTER
        textView.text = "继续下拉刷新"
        return textView
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