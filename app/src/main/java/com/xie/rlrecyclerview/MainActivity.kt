package com.xie.rlrecyclerview

import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.xie.librlrecyclerview.model.UpdateList
import com.xie.librlrecyclerview.other.*
import com.xie.rlrecyclerview.data_source.MyItem
import com.xie.rlrecyclerview.data_source.MyItemDataSource
import com.xie.rlrecyclerview.databinding.ActivityMainBinding
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private lateinit var adapter: MyAdapter
    private val listData = ArrayList<MyItem>()
    private var testType = TestType.TEST_NORMAL
    private lateinit var binding: ActivityMainBinding
    private val dataSource = MyItemDataSource()

    companion object {
        const val PAGE_SIZE = 30
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.rgTest.setOnCheckedChangeListener { group, checkedId ->
            when (checkedId) {
                R.id.rb_test_normal -> {
                    testType = TestType.TEST_NORMAL
                    binding.rlRefreshLayout.setRefreshing(true)
                    refreshData()
                }

                R.id.rb_test_load_more_error -> {
                    testType = TestType.TEST_LOAD_MORE_ERROR
                    binding.rlRefreshLayout.setRefreshing(true)
                    refreshData()
                }

                R.id.rb_test_last_page -> {
                    testType = TestType.TEST_LOAD_MORE_LAST_PAGE
                    binding.rlRefreshLayout.setRefreshing(true)
                    refreshData()
                }
            }
        }

        binding.rlRv.layoutManager = StaggeredGridLayoutManager(2, RecyclerView.VERTICAL)
        adapter = MyAdapter()
        adapter.onItemClickListener = object : MyAdapter.OnItemClickListener {

            override fun onLongClick(info: MyItem) {
                listData.remove(info)
                adapter.updateList(UpdateList(UpdateType.CHANGE_LIST, listData))
            }

            override fun onClick(info: MyItem) {
                val index = listData.indexOf(info)
                if (index != -1) {
                    //需要复制新对象，否则改动会影响旧的info，导致对比不出差别
                    listData[index] = info.copy()
                    listData[index].name = "已点击${listData[index].id}"
                    adapter.updateList(UpdateList(UpdateType.CHANGE_LIST, listData))
                }
            }
        }
        binding.rlRv.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                LogUtil.i("canScrollVertically top ->" + binding.rlRv.canScrollVertically(-1))
            }
        })

        binding.rlRv.adapter = adapter
        //打开下拉刷新开关
        binding.rlRefreshLayout.setOnRefreshListener(object : OnRefreshListener {
            override fun onRefresh() {
                refreshData()
            }
        })
        //打开自动加载开关
        //第二个参数为剩下多少个item未展示时触发下一页的加载
        binding.rlRv.setAutoLoadMoreEnable(true, 2)
        binding.rlRv.onLoadMoreListener = object : OnLoadMoreListener {
            override fun onLoadMore() {
                lifecycleScope.launch {

                    when (testType) {
                        TestType.TEST_LOAD_MORE_ERROR -> {
                            delay(2000)
                            binding.rlRv.setRLState(RLRecyclerState.LOAD_MORE_ERROR)
                        }

                        TestType.TEST_LOAD_MORE_LAST_PAGE -> {
                            val newList = getListInfo(false)
                            listData.addAll(newList)
                            //用替换数据的方式刷新列表
                            adapter.updateList(UpdateList(UpdateType.CHANGE_LIST, listData))
                            binding.rlRv.setRLState(RLRecyclerState.LOAD_MORE_LAST_PAGE)
                        }

                        else -> {
                            val newList = getListInfo(false)
                            listData.addAll(newList)
                            //用替换数据的方式刷新列表
                            adapter.updateList(UpdateList(UpdateType.CHANGE_LIST, listData))
                            //刷新或加载完成，隐藏刷新和加载UI
                            binding.rlRv.setRLState(RLRecyclerState.NORMAL)
                        }
                    }
                }
            }
        }
        binding.rlRefreshLayout.setRefreshing(true)
        refreshData()
    }

    private fun refreshData() {
        //刷新
        lifecycleScope.launch {
            val newList = getListInfo(true)
            listData.clear()
            listData.addAll(newList)
            //用替换数据的方式刷新列表
            adapter.updateList(UpdateList(UpdateType.REFRESH_LIST, listData))
            //刷新或加载完成，隐藏刷新和加载UI
            binding.rlRv.setRLState(RLRecyclerState.NORMAL)
            binding.rlRefreshLayout.setRefreshing(false)
        }
    }

    private suspend fun getListInfo(isRefresh: Boolean): List<MyItem> {
        return dataSource.getNewItems(isRefresh, PAGE_SIZE)
    }
}