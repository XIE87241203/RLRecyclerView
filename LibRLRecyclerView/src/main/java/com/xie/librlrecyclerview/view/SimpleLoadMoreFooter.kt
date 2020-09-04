package com.xie.librlrecyclerview.view

import android.content.Context
import com.xie.librlrecyclerview.R
import com.xie.librlrecyclerview.base.BaseLoadMoreFooter
import com.xie.librlrecyclerview.other.LoadMoreFooterState
import kotlinx.android.synthetic.main.view_simple_load_more_footer.view.*

/**
 * Created by Anthony on 2020/9/4.
 * Describe:
 */
class SimpleLoadMoreFooter(context: Context) : BaseLoadMoreFooter(context) {
    override fun init() {
        inflate(context, R.layout.view_simple_load_more_footer, this)
        ll_load_more.setOnClickListener {
            if (state == LoadMoreFooterState.LOAD_MORE_ERROR) {
                //加载错误点击重新加载
                startLoadMore()
            }
        }
    }

    override fun onLoading() {
        ll_load_more.visibility = VISIBLE
        showLoadMore()
    }

    override fun onLoadMoreFinish() {
        ll_load_more.visibility = INVISIBLE
    }

    override fun onNoMore() {
        ll_load_more.visibility = VISIBLE
        showTips(context.getString(R.string.load_more_no_more))
    }

    override fun onLoadMoreError() {
        ll_load_more.visibility = VISIBLE
        showTips(context.getString(R.string.load_more_error_click))
    }

    private fun showTips(tips: String) {
        progress_bar.visibility = GONE
        tv_load_more_tips.text = tips
    }

    private fun showLoadMore() {
        progress_bar.visibility = VISIBLE
        tv_load_more_tips.text = context.getString(R.string.load_more_loading)
    }
}