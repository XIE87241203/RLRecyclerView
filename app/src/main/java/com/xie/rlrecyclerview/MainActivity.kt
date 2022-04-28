package com.xie.rlrecyclerview

import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.xie.librlrecyclerview.model.UpdateList
import com.xie.librlrecyclerview.other.*
import com.xie.rlrecyclerview.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    lateinit var adapter: MyAdapter
    var page = 1
    val listData = ArrayList<String>()
    var testType = TestType.TEST_NORMAL
    var showTestResult = false
    lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.rgTest.setOnCheckedChangeListener { group, checkedId ->
            when (checkedId) {
                R.id.rb_test_normal -> {
                    testType = TestType.TEST_NORMAL
                    binding.rlRv.startRefresh()
                }
                R.id.rb_test_load_more_error -> {
                    testType = TestType.TEST_LOAD_MORE_ERROR
                    binding.rlRv.startRefresh()
                }
                R.id.rb_test_last_page -> {
                    testType = TestType.TEST_LOAD_MORE_LAST_PAGE
                    binding.rlRv.startRefresh()
                }
            }
        }

        binding.rlRv.layoutManager = StaggeredGridLayoutManager(2, RecyclerView.VERTICAL)
//        binding.rlRv.layoutManager = LinearLayoutManager(this)
        adapter = MyAdapter()
        adapter.onItemClickListener = object : MyAdapter.OnItemClickListener {

            override fun onLongClick(info: String) {
                listData.remove(info)
                adapter.updateList(UpdateList(UpdateType.CHANGE_LIST, listData))
            }

            override fun onClick(info: String) {
                val index = listData.indexOf(info)
                if (index != -1) {
                    listData[index] = "已点击" + listData[index]
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
        binding.rlRv.refreshEnable = true
        //设置下拉刷新回调
        binding.rlRv.onRefreshListener = object : OnRefreshListener {
            override fun onRefresh() {
                //刷新
                Handler().postDelayed({
                    page = 1
                    listData.clear()
                    listData.addAll(getListData(page, 30))
                    //用替换数据的方式刷新列表
                    adapter.updateList(UpdateList(UpdateType.REFRESH_LIST, listData))
                    //刷新或加载完成，隐藏刷新和加载UI
                    binding.rlRv.setRLState(RLRecyclerState.NORMAL)
                    showTestResult = false
                }, 2000)
            }
        }
        //打开自动加载开关
        //第二个参数为剩下多少个item未展示时触发下一页的加载
        binding.rlRv.setAutoLoadMoreEnable(true, 2)
        binding.rlRv.onLoadMoreListener = object : OnLoadMoreListener {
            override fun onLoadMore() {
                //加载更多
                Handler().postDelayed({
                    //测试状态只会显示在第一次加载下一页
                    if (!showTestResult && testType != TestType.TEST_NORMAL) {
                        when (testType) {
                            TestType.TEST_LOAD_MORE_ERROR -> {
                                binding.rlRv.setRLState(RLRecyclerState.LOAD_MORE_ERROR)
                            }
                            TestType.TEST_LOAD_MORE_LAST_PAGE -> {
                                loadNextPage()
                                binding.rlRv.setRLState(RLRecyclerState.LOAD_MORE_LAST_PAGE)
                            }
                        }
                        showTestResult = true
                    } else {
                        loadNextPage()
                        //刷新或加载完成，隐藏刷新和加载UI
                        binding.rlRv.setRLState(RLRecyclerState.NORMAL)
                    }
                }, 2000)
            }
        }
        binding.rlRv.startRefresh()
    }

    fun loadNextPage() {
        page++
        listData.addAll(getListData(page, 30))
        //用插入数据的方式刷新列表
        adapter.updateList(UpdateList(UpdateType.INSERT_DATA, listData))
    }

    fun getListData(page: Int, size: Int): ArrayList<String> {
        val start = ((page - 1) * size) + 1
        val result = ArrayList<String>()
        for (i in start..size * page) {
            result.add("第${i}个")
        }
        return result
    }
}