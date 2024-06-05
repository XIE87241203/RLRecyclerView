RLRecyclerView
=====
本库包含RLRecyclerView和RLRefreshLayout两个控件

其中

1、RLRefreshLayout是一个带有下拉刷新功能的ViewGroup

2、RLRecyclerView 是一个带有自动加载功能的RecyclerView

两者可组合使用

使用方法
======

1、RLRefreshLayout：
====
1、添加RLRefreshLayout
```xml
    <com.xie.librlrecyclerview.refresh_layout.RLRefreshLayout
        android:id="@+id/rl_refresh_layout"
        android:layout_width="match_parent"
        android:layout_height="match_parent">
        <!--    加入其他控件，这里使用RLRecyclerView做例子    -->
        <com.xie.librlrecyclerview.recycler_view.RLRecyclerView
            android:id="@+id/rl_rv"
            android:layout_width="match_parent"
            android:layout_height="match_parent"/>

    </com.xie.librlrecyclerview.refresh_layout.RLRefreshLayout>
```
或者使用代码生成对象
```kotlin
//或者      
//代码生成对象
val newRLRefreshLayout = RLRefreshLayout(context)
```
RLRefreshLayout内能且只能有第一个View作为目标，执行下拉刷新

2、为RLRefreshLayout设置刷新指示控件

RLRefreshLayout的刷新指示控件必须继承BaseRefreshHeader并实现所有方法。

然后使用RLRefreshLayout.setRefreshView(refreshHeader: BaseRefreshHeader)应用到RLRefreshLayout中。


2、RLRefreshLayout：
====
1、布局文件中或通过代码动态使用RLRecyclerView
-----
```xml
<com.xie.librlrecyclerview.view.RLRecyclerView
        android:id="@+id/rl_rv"
        android:layout_width="match_parent"
        android:layout_height="wrap_content" />
```
```kotlin
//或者      
//代码生成对象
val rlRecyclerView = RLRecyclerView(this)
```

2、继承RLRecyclerAdapter
-----
继承RLRecyclerAdapter来替代RecyclerView.Adapter,其中**CreateViewHolderNew**，**onBindViewHolderNew**，**getItemViewTypeNew**分别对应RecyclerView的**onCreateViewHolder**，**onBindViewHolder**，**getItemViewType**。
使用BaseRecyclerViewHolder对列表item的ViewHolder进行初始化
```kotlin
//String为列表数据的类型，可根据自己的需要传入不同的类
class MyAdapter : RLRecyclerAdapter<String>() {

    override fun onCreateViewHolderNew(parent: ViewGroup, viewType: Int): BaseRecyclerViewHolder {
        return BaseRecyclerViewHolder.Companion.createViewHolder(
            parent.context,
            parent,
            R.layout.item_list
        )
    }

    override fun onBindViewHolderNew(holder: BaseRecyclerViewHolder, position: Int) {
        //通过getListData()获取列表数据，在这个例子里getListData()返回ArrayList<String>
        (holder.contentView as TextView).text = getListData()[position]
    }

    override fun getItemViewTypeNew(position: Int): Int {
        return 0
    }
}
```

3、为RLRecyclerView设置Adapter并初始化对应设置
-----
```kotlin
 rl_rv.layoutManager = StaggeredGridLayoutManager(2, RecyclerView.VERTICAL)
 rl_rv.adapter = MyAdapter()
 
  //打开自动加载开关
  //第二个参数为剩下多少个item未展示时触发下一页的加载
  rl_rv.setAutoLoadMoreEnable(true,2)
  rl_rv.onLoadMoreListener = object : OnLoadMoreListener {
      override fun onLoadMore() {
          //加载更多
          ...
          //用插入数据的方式刷新列表，listData为整个列表数据的Arraylist
          adapter.updateList(UpdateList(UpdateType.INSERT_DATA, listData))
          //刷新或加载完成，隐藏刷新和加载UI
          rl_rv.setRLState(RLRecyclerState.NORMAL)
      }
  }
```

4、添加Header和Footer
-----
注意**在RLRecyclerView设置Adapter之后**添加Header或Footer需要调用 **notifyItemInserted(position:Int)** 或 **notifyDataSetChanged()** 来通知列表更新
```kotlin
  //添加Header
  adapter.addHeaderView(TextView(this))
  //添加Footer
  adapter.addFooterView(TextView(this))
```
