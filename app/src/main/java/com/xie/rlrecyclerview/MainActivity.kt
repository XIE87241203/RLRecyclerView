package com.xie.rlrecyclerview

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.xie.librlrecyclerview.other.OnRefreshListener
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    lateinit var adapter: MyAdapter
    var page = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        rl_rv.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        adapter = MyAdapter()
        rl_rv.adapter = adapter
        rl_rv.refreshEnable = true
        rl_rv.onRefreshListener = object : OnRefreshListener {
            override fun onRefresh() {
                Handler().postDelayed({
                    adapter.setListData(getListData(1, 20))
                    adapter.notifyDataSetChanged()
                    rl_rv.finishRefresh()
                }, 2000)
            }
        }
        rl_rv.startRefresh()
    }

    fun getListData(page: Int, size: Int): ArrayList<String> {
        val start = (page - 1) * size
        val result = ArrayList<String>()
        for (i in start..size * page) {
            result.add("第${i}个")
        }
        return result
    }
}