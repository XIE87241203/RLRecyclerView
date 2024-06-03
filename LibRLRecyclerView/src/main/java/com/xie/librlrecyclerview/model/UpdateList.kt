package com.xie.librlrecyclerview.model

import com.xie.librlrecyclerview.other.UpdateType

/**
 * Created by Anthony on 2020/9/7.
 * Describe:
 */
data class UpdateList<T>(
    /**
     * 更新类型
     */
    val updateType: UpdateType,
    /**
     * 列表的所有数据,用于替换掉adapter内数据
     */
    val listData: List<T>
)