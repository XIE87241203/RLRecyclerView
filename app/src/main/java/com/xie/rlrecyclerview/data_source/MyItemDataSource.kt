package com.xie.rlrecyclerview.data_source

import kotlinx.coroutines.delay
import java.util.concurrent.atomic.AtomicLong

/**
 * @Author XIE
 * @Date 2024/6/3
 * @Description
 */
class MyItemDataSource {
    private val itemIdCreator = AtomicLong()
    suspend fun getNewItems(isRefresh:Boolean ,pageSize: Int): List<MyItem> {
        delay(2000)
        val resultList = ArrayList<MyItem>()
        if(isRefresh) itemIdCreator.set(0)
        for (i in 0 until pageSize) {
            val id = itemIdCreator.incrementAndGet()
            resultList.add(MyItem(id, "item:$id"))
        }
        return resultList
    }
}