package com.xie.librlrecyclerview.other

/**
 * @Author XJA87
 * @Date 2022/4/27 16:13
 */
class ListIdUtils {
    var lastId: Long = 0

    fun getId(): Long {
        lastId += 1
        return lastId
    }
}