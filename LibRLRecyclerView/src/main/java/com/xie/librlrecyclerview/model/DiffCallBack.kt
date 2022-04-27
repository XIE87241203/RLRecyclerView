package com.xie.librlrecyclerview.model

import android.annotation.SuppressLint
import androidx.recyclerview.widget.DiffUtil

/**
 * @Author XJA87
 * @Date 2022/4/27 15:53
 */
class DiffCallBack<T : Any> : DiffUtil.ItemCallback<T>() {

    override fun areItemsTheSame(oldItem: T, newItem: T): Boolean {
        return oldItem == newItem
    }

    @SuppressLint("DiffUtilEquals")
    override fun areContentsTheSame(oldItem: T, newItem: T): Boolean {
        return oldItem == newItem
    }
}
