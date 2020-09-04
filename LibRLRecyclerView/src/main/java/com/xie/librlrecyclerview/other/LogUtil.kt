package com.xie.librlrecyclerview.other

import android.util.Log

/**
 * 日志打印工具
 * Created by  carr on 16/04/08.
 */
object LogUtil {
    /**
     * isPrint: print switch, true will print. false not print
     */
    private const val isPrint: Boolean = true
    private var defaultTag = "RLRecyclerView"
    fun setTag(tag: String) {
        defaultTag = tag
    }

    fun i(o: Any?): Int {
        return if (isPrint && o != null) Log.i(defaultTag, o.toString()) else -1
    }

    fun i(m: String?): Int {
        return if (isPrint && m != null) Log.i(defaultTag, m) else -1
    }

    fun e(m: String?): Int {
        return if (isPrint && m != null) Log.e(defaultTag, m) else -1
    }

    /**
     * ******************** Log **************************
     */
    fun v(tag: String?, msg: String?): Int {
        return if (isPrint && msg != null) Log.v(tag, msg) else -1
    }

    fun d(tag: String?, msg: String?): Int {
        return if (isPrint && msg != null) Log.d(tag, msg) else -1
    }

    fun i(tag: String?, msg: String?): Int {
        return if (isPrint && msg != null) Log.i(tag, msg) else -1
    }

    fun w(tag: String?, msg: String?): Int {
        return if (isPrint && msg != null) Log.w(tag, msg) else -1
    }

    fun e(tag: String?, msg: String?): Int {
        return if (isPrint && msg != null) Log.e(tag, msg) else -1
    }

    /**
     * ******************** Log with Throwable **************************
     */
    fun v(tag: String?, msg: String?, tr: Throwable?): Int {
        return if (isPrint && msg != null) Log.v(tag, msg, tr) else -1
    }

    fun d(tag: String?, msg: String?, tr: Throwable?): Int {
        return if (isPrint && msg != null) Log.d(tag, msg, tr) else -1
    }

    fun i(tag: String?, msg: String?, tr: Throwable?): Int {
        return if (isPrint && msg != null) Log.i(tag, msg, tr) else -1
    }

    fun w(tag: String?, msg: String?, tr: Throwable?): Int {
        return if (isPrint && msg != null) Log.w(tag, msg, tr) else -1
    }

    fun e(tag: String?, msg: String?, tr: Throwable?): Int {
        return if (isPrint && msg != null) Log.e(tag, msg, tr) else -1
    }

    /**
     * ******************** TAG use Object Tag **************************
     */
    fun v(tag: Any, msg: String?): Int {
        return if (isPrint) Log.v(tag.javaClass.simpleName, msg) else -1
    }

    fun d(tag: Any, msg: String?): Int {
        return if (isPrint) Log.d(tag.javaClass.simpleName, msg) else -1
    }

    fun i(tag: Any, msg: String?): Int {
        return if (isPrint) Log.i(tag.javaClass.simpleName, msg) else -1
    }

    fun w(tag: Any, msg: String?): Int {
        return if (isPrint) Log.w(tag.javaClass.simpleName, msg) else -1
    }

    fun e(tag: Any, msg: String?): Int {
        return if (isPrint) Log.e(tag.javaClass.simpleName, msg) else -1
    }
}