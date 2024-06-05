package com.xie.librlrecyclerview.recycler_view

import android.content.Context
import com.xie.librlrecyclerview.R
import com.xie.librlrecyclerview.recycler_view.base.BaseLoadMoreFooter
import com.xie.librlrecyclerview.databinding.ViewSimpleLoadMoreFooterBinding
import com.xie.librlrecyclerview.recycler_view.model.LoadMoreFooterState

/**
 * Created by Anthony on 2020/9/4.
 * Describe:
 */
class SimpleLoadMoreFooter(context: Context) : BaseLoadMoreFooter(context) {
    lateinit var binding: ViewSimpleLoadMoreFooterBinding

    override fun init() {
        inflate(context, R.layout.view_simple_load_more_footer, this)
        binding = ViewSimpleLoadMoreFooterBinding.bind(getChildAt(0))
        binding.llLoadMore.setOnClickListener {
            if (state == LoadMoreFooterState.LOAD_MORE_ERROR) {
                //加载错误点击重新加载
                startLoadMore()
            }
        }
    }

    override fun onLoading() {
        binding.llLoadMore.visibility = VISIBLE
        showLoadMore()
    }

    override fun onLoadMoreFinish() {
        binding.llLoadMore.visibility = INVISIBLE
    }

    override fun onNoMore() {
        binding.llLoadMore.visibility = VISIBLE
        showTips(context.getString(R.string.load_more_no_more))
    }

    override fun onLoadMoreError() {
        binding.llLoadMore.visibility = VISIBLE
        showTips(context.getString(R.string.load_more_error_click))
    }

    private fun showTips(tips: String) {
        binding.progressBar.visibility = GONE
        binding.tvLoadMoreTips.text = tips
    }

    private fun showLoadMore() {
        binding.progressBar.visibility = VISIBLE
        binding.tvLoadMoreTips.text = context.getString(R.string.load_more_loading)
    }
}