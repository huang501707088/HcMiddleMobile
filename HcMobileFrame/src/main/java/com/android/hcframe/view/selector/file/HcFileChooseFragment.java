package com.android.hcframe.view.selector.file;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.android.hcframe.AbstractPage;
import com.android.hcframe.HcLog;
import com.android.hcframe.HcUtil;
import com.android.hcframe.R;
import com.android.hcframe.pull.PullToRefreshBase;
import com.android.hcframe.pull.PullToRefreshListView;
import com.android.hcframe.view.selector.DepInfo;
import com.android.hcframe.view.selector.ItemInfo;
import com.android.hcframe.view.selector.StaffInfo;
import com.android.hcframe.view.toast.NoDataView;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Stack;

/**
 * @Company 浙 江 鸿 程 计 算 机 系 统 有 限 公 司
 * @URL http://www.zjhcsoft.com
 * @Address 杭州滨江区伟业路1号
 * @Email jinjr@zjhcsoft.com
 * Created by jrjin on 17-1-11 09:18.
 */
/**
 * 一个纯粹的选择文件的控件,可以进行单选和多选
 */
public class HcFileChooseFragment extends AbstractPage implements AdapterView.OnItemClickListener {

    private static final String TAG = "HcChooseFragment";

    private PullToRefreshListView mListView;

    private LinearLayout mParent;

    private RecyclerView mRecyclerView;

    private TextView mOkBtn;

    private /*final*/ boolean mMultipled;

    private OnOperatorListener mListener;

    /** 部门的层级关系 */
    private Stack<ItemInfo> mStack = new Stack<ItemInfo>(); // 其实都是DepInfo

    /** 选中的列表,为了在Intent里面传递,不用List<> */
    private ArrayList<ItemInfo> mSelected = new ArrayList<ItemInfo>();

    /** 当前页面的显示列表,包括部门 */
    private List<ItemInfo> mCurrent = new ArrayList<ItemInfo>();

    private FileChooseAdapter mAdapter;

    public static final int REQUEST_CODE = 1 << 4;

    /** 单选的时候点击列表 */
    public static final String ITEM_KEY = "click_info";

    /** 多选的时候点击确定 */
    public static final String CLICK_KEY = "click_infos";

    private ChooseRecycleAdapter mRecycleAdapter;

    /** 数据的缓存 */
    private Map<String, ItemInfo> mCache = new HashMap<String, ItemInfo>();

    /** 刷新的时候,不能操作 */
    private boolean mCanOperator = true;

    /** 之前选中的项 */
    private Map<String, List<ItemInfo>> mOldSelected = new HashMap<String, List<ItemInfo>>();

    /** 全部选中的列表项 */
    private ArrayList<ItemInfo> mAllSeleceted = new ArrayList<ItemInfo>();

    /** 是否显示底部的选中的选项 */
    private final boolean mShowSelected;

    private NoDataView mNoDataView;

    /**
     * 人员选择界面
     * @param context
     * @param group
     * @param multipled 是否多选
     * @param listener 操作的监听器
     * @param showSelected 是否显示底部的选中的选项
     */
    public HcFileChooseFragment(Activity context, ViewGroup group, boolean multipled,
                            OnOperatorListener listener, boolean showSelected) {
        super(context, group);
        mMultipled = multipled;
        mListener = listener;
        mShowSelected = showSelected;
    }

    @Override
    public void initialized() {
        if (isFirst) {
            isFirst = !isFirst;
            ItemInfo info = new DepInfo();
            info.setItemValue("");
            info.setItemId("");
            mStack.push(info);
            mCache.put(info.getItemId(), info);
            if (mMultipled) {
                if (mListener != null) {
                    mListener.setSelectedCount(0);
                    mListener.setSelectedStatus(SelectStatus.UNSELECTED);
                }

            }

            mAdapter = new FileChooseAdapter(mContext, mCurrent);
            mListView.setAdapter(mAdapter);

            mRecyclerView.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.HORIZONTAL, false));

            mRecycleAdapter = new ChooseRecycleAdapter();
            mRecyclerView.setAdapter(mRecycleAdapter);

            if (mListener != null) {
                mListener.onParentItemClick(info);
            }
        }
    }

    @Override
    public void setContentView() {
        if (mView == null) {
            mView = mInflater.inflate(R.layout.choose_personnel_fragment, null);
            mListView = (PullToRefreshListView) mView.findViewById(R.id.choose_fragment_personnel_listview);
            mOkBtn = (TextView) mView.findViewById(R.id.choose_fragment_personnel_ok_btn);
            mParent = (LinearLayout) mView.findViewById(R.id.choose_fragment_personnel_recycler_parent);
            mRecyclerView = (RecyclerView) mView.findViewById(R.id.choose_fragment_personnel_recycler);
            mNoDataView = (NoDataView) mView.findViewById(R.id.choose_fragment_personnel_no_data);
            mListView.setEmptyView(mNoDataView);

            mOkBtn.setOnClickListener(this);

            mListView.setOnItemClickListener(this);
            mListView.setScrollingWhileRefreshingEnabled(false);
            mListView.setMode(PullToRefreshBase.Mode.DISABLED);


            mListView.setOnRefreshBothListener(new PullToRefreshBase.OnRefreshBothListener<ListView>() {
                @Override
                public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {
                    if (mListener != null) {
                        mCanOperator = false;

                        mListener.onRefresh(mStack.peek());
                    }

                }

                @Override
                public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {

                }
            });

            if(!mShowSelected) {
                mParent.setVisibility(View.GONE);
            }

        }
    }

    @Override
    public void update(Observable observable, Object data) {
        if (data != null && data instanceof List<?>) {
            List<ItemInfo> datas = (List<ItemInfo>) data;
            mListView.onRefreshComplete();
            mCanOperator = true;
            mCurrent.clear();
            mCurrent.addAll(datas);
//            if (mCurrent.size() == 0)
//                mNoDataView.setDescription("暂无数据");
            ItemInfo dep = mStack.peek();
            dep.addAllChild(mCurrent, true); // 数据添加只在这里添加
            mCache.put(dep.getItemId(), dep);
//            HcLog.D(TAG + " #update info id = "+mStack.peek().getItemId() + " cache size = "+mCache.size() + " data size = "+datas.size());
            int size = mSelected.size();
            if (mMultipled && size > 0) {
                /**
                 * 1.把原先的选中的删除掉
                 * 2.添加新的选中的数据
                 */
                mAllSeleceted.removeAll(mSelected);
                List<ItemInfo> newSelected = new ArrayList<ItemInfo>();
                ItemInfo select;
                ItemInfo newInfo;
                Iterator<ItemInfo> iterator;
                for (int i = 0; i < size; i++) {
                    select = mSelected.get(i);
                    iterator = datas.iterator();
                    while (iterator.hasNext()) {
                        newInfo = iterator.next();
                        if (newInfo instanceof StaffInfo && select.getUserId().equals(newInfo.getUserId())) {
                            newInfo.setSelected(true);
                            newSelected.add(newInfo);
                            iterator.remove();
                            break;
                        }
                    }
                }
                mSelected.clear();
                mSelected.addAll(newSelected);
                mAllSeleceted.addAll(newSelected);
                newSelected.clear();
                mRecycleAdapter.notifyDataSetChanged();

            }
            datas.clear();
            mAdapter.notifyDataSetChanged();

        } else {
            mListView.onRefreshComplete();
        }


    }

    @Override
    public void onClick(View v) {
        if (!mCanOperator) return;
        int id = v.getId();
        if (id == R.id.choose_personnel_ok_btn) {
            Intent intent = new Intent();
            intent.putParcelableArrayListExtra(CLICK_KEY, mAllSeleceted);
            mContext.setResult(Activity.RESULT_OK, intent);
            mContext.finish();
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (!mCanOperator) return;
        ItemInfo info = (ItemInfo) parent.getItemAtPosition(position);
        if (info instanceof DepInfo) {
            // 1.更改选中状态为未选中
            if (mListener != null) {

                if (mMultipled) { // 多选
                    /**
                     *@author jinjr
                     *@date 16-10-21 上午10:04
                     */
                    if (mSelected.size() > 0) {
                        mOldSelected.put(mStack.peek().getItemId(), new ArrayList<ItemInfo>(mSelected));
                        mSelected.clear();
                    }
                    if (mListener != null) {
                        mListener.setSelectedStatus(SelectStatus.UNSELECTED);
                    }
                }

                mCurrent.clear();

                mStack.push(info); // 要是有缓存的话这个里面已经就有数据了,不需要重新添加.


                ItemInfo cache = mCache.get(info.getItemId());
                if (cache != null && cache.getAllChilds().size() > 0) {
                    mCurrent.addAll(cache.getAllChilds());
//                    HcLog.D(TAG + " #onItemClick id = "+info.getItemId() + " cache size = "+mCurrent.size() + " 员工列表 size = "+cache.getChilds().size());
//                    mStack.peek().addAllChild(cache.getAllChilds(), true); // 不需要添加,已经原先在update里面添加了
                    if (mMultipled) {
                        List<ItemInfo> old = mOldSelected.remove(info.getItemId());
                        int oldSize = old.size();
                        int cacheSize = cache.getChilds().size();
                        if (old != null && oldSize > 0) {
                            mSelected.addAll(old);
                        }
                        HcLog.D(TAG + " old selected size = "+oldSize +  " cache size = "+cache.getAllChilds() + " 员工列表 size = "+cacheSize);
                        if (mListener != null) {
                            if (oldSize >= cacheSize) {
                                mListener.setSelectedStatus(SelectStatus.ALLSELECTED);

                            } else if (oldSize > 0) {
                                mListener.setSelectedStatus(SelectStatus.SELECTED);
                            } // == 0的情况上面已经处理了
                        }



                    }
                    mAdapter.notifyDataSetChanged();
                } else {
                    mAdapter.notifyDataSetChanged();
                    mListener.onParentItemClick(info);
                }

            }
        } else {
            if (mMultipled) {
                if (info.isSelected()) { // 选中->不选中
                    mAllSeleceted.remove(info);
                    mSelected.remove(info);
                    ((CheckBox) view.findViewById(R.id.choose_personnel_item_checkbox)).setChecked(false);
                } else {
                    mAllSeleceted.add(info);
                    mSelected.add(info);
                    ((CheckBox) view.findViewById(R.id.choose_personnel_item_checkbox)).setChecked(true);
                }
                info.setSelected(!info.isSelected());
                int size = mSelected.size();
                if (mAllSeleceted.size() > 0) {
                    if (mShowSelected && mParent.getVisibility() != View.VISIBLE)
                        mParent.setVisibility(View.VISIBLE);
                    if (mListener != null) {
                        mListener.setSelectedCount(mAllSeleceted.size());
                        HcLog.D(TAG + "#onItemClick current size = "+size + " all child size = "+mStack.peek().getChilds().size());
                        if (size == mStack.peek().getChilds().size()) { // 说明全部选中了
                            mListener.setSelectedStatus(SelectStatus.ALLSELECTED);
                        } else if (size == 0) {
                            mListener.setSelectedStatus(SelectStatus.UNSELECTED);
                        } else {
                            mListener.setSelectedStatus(SelectStatus.SELECTED);
                        }
                    }

                } else {
                    if (mShowSelected && mParent.getVisibility() != View.GONE)
                        mParent.setVisibility(View.GONE);
                    if (mListener != null) {
                        mListener.setSelectedCount(0);
                        mListener.setSelectedStatus(SelectStatus.UNSELECTED);
                    }
                }
                mRecycleAdapter.notifyDataSetChanged();

            } else {
                // 单选
                Intent intent = new Intent();
                intent.putExtra(ITEM_KEY, info);
                mContext.setResult(Activity.RESULT_OK, intent);
                mContext.finish();
            }
        }
    }

    public interface OnOperatorListener {

        /**
         * 点击下级列表项
         * @param info
         */
        void onParentItemClick(ItemInfo info);

        /**
         * 刷新操作
         * @param info
         */
        void onRefresh(ItemInfo info);

        /**
         * 取消刷新
         * @param info
         */
        void onCanelRefreshRequest(ItemInfo info);

        /**
         * 设置选中的数量
         * @param count
         */
        void setSelectedCount(int count);

        /**
         * 设置当前的选中状态
         * @param status
         */
        void setSelectedStatus(SelectStatus status);
    }

    private class ChooseRecycleAdapter extends RecyclerView.Adapter<ViewHolder> {

        @Override
        public int getItemCount() {
            return mAllSeleceted.size();
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
            HcLog.D(TAG + " $ChooseRecycleAdapter#onCreateViewHolder!!!!!");
            return new ViewHolder(LayoutInflater.from(mContext).inflate(R.layout.choose_file_choosed_item_layout, viewGroup, false));
        }

        @Override
        public void onBindViewHolder(final ViewHolder viewHolder, final int position) {
            HcLog.D(TAG + " $ChooseRecycleAdapter#onBindViewHolder viewHolder="+viewHolder + " position = "+position);
            final ItemInfo info = mAllSeleceted.get(position);
            viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    HcLog.D(TAG + " $ChooseRecycleAdapter#onBindViewHolder#onClick position = " + position + " info name = " + info.getItemValue());
                    info.setSelected(false);
                    if (mSelected.contains(info)) {
                        mSelected.remove(info);
                    } else {
                        Iterator<List<ItemInfo>> iterator = mOldSelected.values().iterator();
                        List<ItemInfo> items;
                        while (iterator.hasNext()) {
                            items = iterator.next();
                            if (items.contains(info)) {
                                items.remove(info);
                                break;
                            }
                        }
                    }
                    mAllSeleceted.remove(info);
                    // 注意这里可能mOldSelected里面没有去除掉,需要去除.
                    // 方法有两中,一种这里处理,另一种添加的时候需要判断.
                    mRecycleAdapter.notifyItemRemoved(viewHolder.getPosition());
                    mAdapter.notifyDataSetChanged();
                    int size = mSelected.size();
                    if (mAllSeleceted.size() > 0) {
                        if (mListener != null) {
                            mListener.setSelectedCount(mAllSeleceted.size());
                            if (size == mStack.peek().getChilds().size()) {
                                mListener.setSelectedStatus(SelectStatus.ALLSELECTED);
                            } else if (size > 0){
                                mListener.setSelectedStatus(SelectStatus.SELECTED);
                            } else {
                                mListener.setSelectedStatus(SelectStatus.UNSELECTED);
                            }
                        }

                    } else {
                        mParent.setVisibility(View.GONE);
                        if (mListener != null) {
                            mListener.setSelectedCount(0);
                            mListener.setSelectedStatus(SelectStatus.UNSELECTED);
                        }
                    }
                }
            });
            viewHolder.mName.setText(info.getItemValue());
            ImageLoader.getInstance().displayImage(info.getIconUrl(), viewHolder.mIcon, HcUtil.getAccountImageOptions());
        }
    }

    private static class ViewHolder extends RecyclerView.ViewHolder {

        TextView mName;

        ImageView mIcon;

        public ViewHolder(View itemView) {
            super(itemView);

            mName = (TextView) itemView.findViewById(R.id.choose_file_choosed_item_name);
            mIcon = (ImageView) itemView.findViewById(R.id.choose_file_choosed_item_icon);
        }
    }

    @Override
    public void onDestory() {
        mSelected.clear();
        mCurrent.clear();
        mStack.clear();
        if (mAdapter != null)
            mAdapter.releaseAdatper();
        mContext = null;
    }


    /**
     * 点击返回按钮
     */
    public void onReturn() {
        if (!mCanOperator) { // 需要取消刷新
            mCanOperator = true;
            mListView.onRefreshComplete();
            if (mListener != null) {
                mListener.onCanelRefreshRequest(mStack.peek());
            }
        }
        ItemInfo old = mStack.pop();
        if (mStack.size() > 0) {
            mCurrent.clear();
            ItemInfo item = mStack.peek();
            if (mMultipled) { // 多选


                /**
                 * 1.添加现在的选中的数据到mOldSeleceted中
                 * 2.从mOldSeleceted中取出原先选中的,添加到mSelected中
                 */
                mOldSelected.put(old.getItemId(), new ArrayList<ItemInfo>(mSelected));
                List<ItemInfo> cache = mOldSelected.get(item.getItemId());
                mSelected.clear();
                if (cache != null && cache.size() > 0) {
                    mSelected.addAll(cache);
                }

                int offset = mSelected.size() - item.getChilds().size();
                if (mListener != null) {
                    if (offset < 0) {
                        if (mSelected.size() == 0)
                            mListener.setSelectedStatus(SelectStatus.UNSELECTED);
                        else {
                            mListener.setSelectedStatus(SelectStatus.SELECTED);
                        }
                    } else {
                        mListener.setSelectedStatus(SelectStatus.ALLSELECTED);
                    }
                }


            }

            mCurrent.addAll(item.getAllChilds());
            mAdapter.notifyDataSetChanged();

        } else {

            mContext.finish();
        }
    }

    /**
     * 操作全选和不全选
     */
    public void setAllSelected() {
        if (!mCanOperator) return;

        ItemInfo item = mStack.peek(); // 这个肯定是部门
        List<ItemInfo> childs = item.getChilds(); // 员工列表
        if (childs.size() == 0) return;
        int offset = childs.size() - mSelected.size();

        if (offset <= 0) { // （当前已经全部选中）全选--->全不选
            mAllSeleceted.removeAll(mSelected);
            mSelected.clear();
            int size = mAllSeleceted.size();
            if (size == 0) {
                if(mShowSelected)
                    mParent.setVisibility(View.GONE);

            }

            for (ItemInfo itemInfo : childs) {
                itemInfo.setSelected(false);
            }

            if (mListener != null) {
                mListener.setSelectedCount(size);
                mListener.setSelectedStatus(SelectStatus.UNSELECTED);
            }

        } else {
            for (ItemInfo itemInfo : childs) {
                itemInfo.setSelected(true);
            }
            mAllSeleceted.removeAll(mSelected);
            mSelected.addAll(item.getChilds());
            mAllSeleceted.addAll(mSelected);
            if (mShowSelected)
                mParent.setVisibility(View.VISIBLE);
            if (mListener != null) {
                mListener.setSelectedCount(mAllSeleceted.size());
                mListener.setSelectedStatus(SelectStatus.ALLSELECTED);
            }
        }


        mAdapter.notifyDataSetChanged();
        mRecycleAdapter.notifyDataSetChanged();
    }

    /**
     * 当前页面是否可以进行全选或者取消全选操作的状态
     */
    public enum SelectStatus {
        /** 未选中状态,可进行全选操作 */
        UNSELECTED,
        /** 未全选中状态,可进行全选操作 */
        SELECTED,
        /** 已经全部选中状态,可以进行全不选操作 */
        ALLSELECTED
    }

    public List<ItemInfo> getAllSelected() {
        return mAllSeleceted;
    }

    /**
     * 设置多选单选模式
     * @param multipled
     */
    public void setMultipled(boolean multipled) {
        if (mMultipled == multipled) return;
        mMultipled = multipled;
        if (mAllSeleceted.size() > 0) {
            for (ItemInfo info : mAllSeleceted) {
                info.setSelected(false);
            }
            mSelected.clear();
            mAllSeleceted.clear();
            mAdapter.notifyDataSetChanged();
            if (mShowSelected && mParent.getVisibility() == View.VISIBLE) {
                mParent.setVisibility(View.GONE);
                mRecycleAdapter.notifyDataSetChanged();
            }
        }

    }

    /**
     * 设置列表刷新的方式,只能不刷新或者下拉刷新
     * @param mode
     */
    public void setMode(PullToRefreshBase.Mode mode) {
        switch (mode) {
            case DISABLED:
                mListView.setMode(PullToRefreshBase.Mode.DISABLED);
                break;
            case BOTH:
            case PULL_FROM_START:
            case PULL_FROM_END:
                mListView.setMode(PullToRefreshBase.Mode.PULL_FROM_START);
                break;

            default:
                break;
        }
    }
}
