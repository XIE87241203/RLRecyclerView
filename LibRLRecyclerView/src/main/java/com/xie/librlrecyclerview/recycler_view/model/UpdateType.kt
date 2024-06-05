package com.xie.librlrecyclerview.recycler_view.model

/**
 * Created by Anthony on 2020/9/7.
 * Describe:更新类型
 */
enum class UpdateType {
    /**
     * 初始化整个列表数据
     */
    REFRESH_LIST,

    /**
     * 列表被插入数据
     */
    @Deprecated("已废弃，请使用CHANGE_LIST")
    INSERT_DATA,

    /**
     * 列表数据内被改变
     */
    CHANGE_LIST
}