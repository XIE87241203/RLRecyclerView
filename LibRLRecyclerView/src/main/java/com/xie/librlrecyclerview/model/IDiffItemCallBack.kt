package com.xie.librlrecyclerview.model

/**
 * @Author XIE
 * @Date 2024/6/3
 * @Description
 */
interface IDiffItemCallBack<T>{
    fun areItemTheSame(oldItem: T, newItem: T): Boolean

    fun areContentTheSame(oldItem: T, newItem: T): Boolean
}