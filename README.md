RLRecyclerView
=====
RLRecyclerView 是一个带有下拉刷新和自动加载，同时支持添加Header和Footer功能的RecyclerView

使用方法
=====
1、布局文件中或通过代码动态使用RLRecyclerView
-----
```xml
//布局文件中添加
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
  //打开下拉刷新开关
 rl_rv.refreshEnable = true
 //设置下拉刷新回调
 rl_rv.onRefreshListener = object : OnRefreshListener {
      override fun onRefresh() {
          //刷新列表
          
          ...
          //用替换数据的方式刷新列表，listData为整个列表数据的Arraylist
          adapter.updateList(UpdateList(UpdateType.REFRESH_LIST, listData))
          //刷新或加载完成，隐藏刷新和加载UI
          rl_rv.setRLState(RLRecyclerState.NORMAL)
      }
 }
 
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
