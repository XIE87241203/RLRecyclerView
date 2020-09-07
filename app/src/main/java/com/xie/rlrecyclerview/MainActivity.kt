package com.xie.rlrecyclerview

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.widget.RadioGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.xie.librlrecyclerview.model.UpdateList
import com.xie.librlrecyclerview.other.OnLoadMoreListener
import com.xie.librlrecyclerview.other.OnRefreshListener
import com.xie.librlrecyclerview.other.RLRecyclerState
import com.xie.librlrecyclerview.other.UpdateType
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    lateinit var adapter: MyAdapter
    var page = 1
    val listData = ArrayList<String>()
    var testType = TestType.TEST_NORMAL
    var showTestResult = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        rg_test.setOnCheckedChangeListener { group, checkedId ->
            when (checkedId) {
                R.id.rb_test_normal -> {
                    testType = TestType.TEST_NORMAL
                    rl_rv.startRefresh()
                }
                R.id.rb_test_load_more_error -> {
                    testType = TestType.TEST_LOAD_MORE_ERROR
                    rl_rv.startRefresh()
                }
                R.id.rb_test_last_page -> {
                    testType = TestType.TEST_LOAD_MORE_LAST_PAGE
                    rl_rv.startRefresh()
                }
            }
        }

        rl_rv.layoutManager = StaggeredGridLayoutManager(2, RecyclerView.VERTICAL)
        adapter = MyAdapter()
        rl_rv.adapter = adapter
        //打开下拉刷新开关
        rl_rv.refreshEnable = true
        //设置下拉刷新回调
        rl_rv.onRefreshListener = object : OnRefreshListener {
            override fun onRefresh() {
                //刷新
                Handler().postDelayed({
                    page = 1
                    listData.clear()
                    listData.addAll(getListData(page, 30))
                    //用替换数据的方式刷新列表
                    adapter.updateList(UpdateList(UpdateType.REFRESH_LIST, listData))
                    //刷新或加载完成，隐藏刷新和加载UI
                    rl_rv.setRLState(RLRecyclerState.NORMAL)
                    showTestResult = false
                }, 2000)
            }
        }
        //打开自动加载开关
        //第二个参数为剩下多少个item未展示时触发下一页的加载
        rl_rv.setAutoLoadMoreEnable(true,2)
        rl_rv.onLoadMoreListener = object : OnLoadMoreListener {
            override fun onLoadMore() {
                //加载更多
                Handler().postDelayed({
                    //测试状态只会显示在第一次加载下一页
                    if (!showTestResult && testType != TestType.TEST_NORMAL) {
                        when (testType) {
                            TestType.TEST_LOAD_MORE_ERROR -> {
                                rl_rv.setRLState(RLRecyclerState.LOAD_MORE_ERROR)
                            }
                            TestType.TEST_LOAD_MORE_LAST_PAGE -> {
                                loadNextPage()
                                rl_rv.setRLState(RLRecyclerState.LOAD_MORE_LAST_PAGE)
                            }
                        }
                        showTestResult = true
                    } else {
                        loadNextPage()
                        //刷新或加载完成，隐藏刷新和加载UI
                        rl_rv.setRLState(RLRecyclerState.NORMAL)
                    }
                }, 2000)
            }
        }
        rl_rv.startRefresh()
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