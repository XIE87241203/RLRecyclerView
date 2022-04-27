package com.xie.librlrecyclerview.other

import android.view.View
import com.xie.librlrecyclerview.base.BaseRefreshHeader
import com.xie.librlrecyclerview.base.RLRecyclerAdapter
import com.xie.librlrecyclerview.model.ListFooterInfo
import com.xie.librlrecyclerview.model.ListHeaderInfo

/**
 * @Author XJA87
 * @Date 2022/4/27 16:53
 */
class HeaderAndFooterHelper {
    companion object {
        const val ID_REFRESH_HEADER = -1
        const val ID_LOAD_MORE_FOOTER = -1
    }

    internal var refreshHeaderInfo: ListHeaderInfo? = null
    internal var refreshHeaderView: BaseRefreshHeader? = null

    internal val headerInfoList: List<ListHeaderInfo> by lazy { ArrayList() }
    internal val headerViewList: List<View> by lazy { ArrayList() }

    internal val footerInfoList: List<ListFooterInfo> by lazy { ArrayList() }
    internal val footerViewList: List<View> by lazy { ArrayList() }

    var loadMoreFooterInfo: ListFooterInfo? = null
    var loadMoreFooterView: View? = null

    /**
     * 设置刷新头部
     *
     * @param refreshHeader refreshHeader
     */
    fun setRefreshHeader(refreshHeader: BaseRefreshHeader?) {
        //去除旧的header
        refreshHeaderInfo = null
        refreshHeaderView = null
        if (refreshHeader != null) {
            refreshHeaderInfo = ListHeaderInfo(ID_REFRESH_HEADER)
            refreshHeaderView = refreshHeader
        }
    }

    fun getAllHeaderInfo(): List<ListHeaderInfo> {
        val result = ArrayList<ListHeaderInfo>()
        refreshHeaderInfo?.let {
            result.add(it)
        }
        result.addAll(headerInfoList)
        return result
    }

    fun getAllFooterInfo(): List<ListFooterInfo> {
        val result = ArrayList<ListFooterInfo>()
        result.addAll(footerInfoList)
        loadMoreFooterInfo?.let {
            result.add(it)
        }
        return result
    }
}