package com.xie.rlrecyclerview

import android.content.Context
import android.graphics.Color
import android.view.Gravity
import android.view.View
import android.widget.TextView
import com.xie.librlrecyclerview.refresh_layout.BaseRefreshHeader

/**
 * @Author XIE
 * @Date 2024/6/5
 * @Description
 */
class TestRefreshView(context: Context):BaseRefreshHeader(context) {
    private lateinit var textView: TextView
    override fun onRefreshing() {
        textView.setText("Refreshing")
    }

    override fun onRefreshFinish() {
        textView.setText("RefreshFinish")
    }

    override fun onRefreshPrepare() {
        textView.setText("RefreshPrepare")
    }

    override fun onRefreshNormal() {
        textView.setText("RefreshNormal")
    }

    override fun getRefreshingContentHeight(): Int {
        return dpToPx(context,80f).toInt()
    }

    override fun getMaxHeight(): Int {
        return dpToPx(context,200f).toInt()
    }

    override fun getContentView(context: Context): View {
        textView = TextView(context)
        textView.setTextSize(18f)
        textView.gravity = Gravity.CENTER
        textView.setBackgroundColor(Color.GRAY)
        return textView
    }
}