package com.android.hcframe.view.selector.file;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
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
import com.android.hcframe.TopBarView;
import com.android.hcframe.pull.PullToRefreshBase;
import com.android.hcframe.pull.PullToRefreshListView;
import com.android.hcframe.view.selector.DepInfo;
import com.android.hcframe.view.selector.HcChooseHomeView;
import com.android.hcframe.view.selector.ItemInfo;
import com.android.hcframe.view.selector.PersonnelChooseAdapter;
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
 * Created by jrjin on 16-12-8 16:24.
 */

/**文件选择类
 * 注意:多选时可以跨级选择,但是传递的时候要过滤掉相同的userId的数据
 */
public class HcFileChooseHomeView extends AbstractPage implements AdapterView.OnItemClickListener {

    private static final String TAG = "HcFileChooseHomeView";

    private PullToRefreshListView mListView;

    private TextView mDep;

    private LinearLayout mParent;

    private RecyclerView mRecyclerView;

    private TextView mOkBtn;

    private TopBarView mTopBarView;

    private final boolean mMultipled;

    private OnOperatorListener mListener;

    /** 部门的层级关系 */
    private Stack<ItemInfo> mStack = new Stack<ItemInfo>(); // 其实都是DepInfo

    /** 选中的列表,为了在Intent里面传递,不用List<> */
    private ArrayList<ItemInfo> mSelected = new ArrayList<ItemInfo>();

    /** 当前页面的显示列表,包括部门 */
    private List<ItemInfo> mCurrent = new ArrayList<ItemInfo>();

    private FileChooseAdapter mAdapter;

    public static final int REQUEST_CODE = HcChooseHomeView.REQUEST_CODE + 1;

    /** 单选的时候点击列表 */
    public static final String ITEM_KEY = "click_info";

    /** 多选的时候点击确定 */
    public static final String CLICK_KEY = "click_infos";

    private /*final*/ String mRoot;

    private ChooseRecycleAdapter mRecycleAdapter;

    /** 数据的缓存 */
    private Map<String, ItemInfo> mCache = new HashMap<String, ItemInfo>();

    private final String mTitle;

    /** 刷新的时候,不能操作 */
    private boolean mCanOperator = true;

    /** 之前选中的项 */
    private Map<String, List<ItemInfo>> mOldSelected = new HashMap<String, List<ItemInfo>>();

    /** 全部选中的列表项 */
    private ArrayList<ItemInfo> mAllSeleceted = new ArrayList<ItemInfo>();

    private NoDataView mNoDataView;

    /**
     * 人员选择界面
     * @param context
     * @param group
     * @param multipled 是否多选
     * @param listener 操作的监听器
     * @param title 页面标题
     * @param root 根目录名字,要是为null,则不可刷新数据
     */
    public HcFileChooseHomeView(Activity context, ViewGroup group, boolean multipled,
                                OnOperatorListener listener, String title, String root) {
        super(context, group);
        mMultipled = multipled;
        mListener = listener;
        mRoot = root;
        mTitle = title;
    }

    @Override
    public void initialized() {
        if (isFirst) {
            isFirst = !isFirst;
            mTopBarView.setTitle(mTitle);
            mDep.setText(mRoot);
            ItemInfo info = new DepInfo();
            info.setItemValue(mRoot);
            info.setItemId("");
            mStack.push(info);
            mCache.put(info.getItemId(), info);
            if (mMultipled) {
                mTopBarView.setRightBtnVisiable(View.VISIBLE);
                mTopBarView.setRightText("全选");
                mTopBarView.setRightViewListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (!mCanOperator) return;
                        /**
                        ItemInfo item = mStack.peek(); // 这个肯定是部门
                        List<ItemInfo> childs = item.getChilds();
                        mSelected.clear();
                        if (item.isSelected()) { // 全选--->全不选
                            mParent.setVisibility(View.GONE);
                            for (ItemInfo itemInfo : childs) {
                                itemInfo.setSelected(false);
                            }
                            mTopBarView.setTitle(mTitle);
                            mTopBarView.setRightText("全选");

                        } else {

                            for (ItemInfo itemInfo : childs) {
                                itemInfo.setSelected(true);
                            }
                            if (item.isSelected()) { // 因为这里万一都是DepInfo
                                mSelected.addAll(item.getChilds());
                                mTopBarView.setTitle(mTitle + "(" + childs.size() + ")");
                                mTopBarView.setRightText("全不选");
                                mParent.setVisibility(View.VISIBLE);
                            }

                        }*/

                        ItemInfo item = mStack.peek(); // 这个肯定是部门
                        List<ItemInfo> childs = item.getChilds(); // 员工列表
                        if (childs.size() == 0) return;
                        int offset = childs.size() - mSelected.size();

                        if (offset <= 0) { // （当前已经全部选中）全选--->全不选
                            mAllSeleceted.removeAll(mSelected);
                            mSelected.clear();
                            int size = mAllSeleceted.size();
                            if (size == 0) {
                                mParent.setVisibility(View.GONE);
                                mTopBarView.setTitle(mTitle);
                            } else {
                                mTopBarView.setTitle(mTitle + "(" + size + ")");
                            }

                            for (ItemInfo itemInfo : childs) {
                                itemInfo.setSelected(false);
                            }

                            mTopBarView.setRightText("全选");
                        } else {
                            for (ItemInfo itemInfo : childs) {
                                itemInfo.setSelected(true);
                            }
                            mAllSeleceted.removeAll(mSelected);
                            mSelected.addAll(item.getChilds());
                            mAllSeleceted.addAll(mSelected);
                            mTopBarView.setTitle(mTitle + "(" + mAllSeleceted.size() + ")");
                            mTopBarView.setRightText("全不选");
                            mParent.setVisibility(View.VISIBLE);
                        }


                        mAdapter.notifyDataSetChanged();
                        mRecycleAdapter.notifyDataSetChanged();
                    }
                });
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
            mView = mInflater.inflate(R.layout.activity_choose_personnel, null);
            mTopBarView = (TopBarView) mView.findViewById(R.id.choose_personnel_top_bar);
            mDep = (TextView) mView.findViewById(R.id.choose_personnel_dep_name);
            mListView = (PullToRefreshListView) mView.findViewById(R.id.choose_personnel_listview);
            mOkBtn = (TextView) mView.findViewById(R.id.choose_personnel_ok_btn);
            mParent = (LinearLayout) mView.findViewById(R.id.choose_personnel_recycler_parent);
            mRecyclerView = (RecyclerView) mView.findViewById(R.id.choose_personnel_recycler);
            mNoDataView = (NoDataView) mView.findViewById(R.id.choose_personnel_no_data);
            mListView.setEmptyView(mNoDataView);

            mOkBtn.setOnClickListener(this);

            mListView.setOnItemClickListener(this);
            mListView.setScrollingWhileRefreshingEnabled(false);
            if (mRoot == null) {
                mListView.setMode(PullToRefreshBase.Mode.DISABLED);
                mDep.setVisibility(View.GONE);
                mRoot = "";
            } else {
                mListView.setMode(PullToRefreshBase.Mode.PULL_FROM_START);
            }


            mListView.setOnRefreshBothListener(new PullToRefreshBase.OnRefreshBothListener<ListView>() {
                @Override
                public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {
                    if (mListener != null) {
                        mCanOperator = false;
                        /**放到返回来处理
                        int size = mSelected.size();
                        if (mMultipled && size > 0) {
                            mAllSeleceted.removeAll(mSelected);
                            for (ItemInfo info : mSelected) {
                            	info.setSelected(false);
                            }
                            mSelected.clear();
                            if (size > 0) {
                                mTopBarView.setTitle(mTitle + "(" + size + ")");
                            } else {
                                if (mParent.getVisibility() != View.GONE) {
                                    mParent.setVisibility(View.GONE);
                                }
                                mTopBarView.setTitle(mTitle);
                            }

                            mTopBarView.setRightText("全选");
                            mRecycleAdapter.notifyDataSetChanged();
                        }*/
                        mListener.onRefresh(mStack.peek());
                    }

                }

                @Override
                public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {

                }
            });

            mTopBarView.setReturnViewListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
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
                            for (ItemInfo itemInfo : mSelected) {
                                itemInfo.setSelected(false);
                            }

                            if (mParent.getVisibility() != View.GONE) {
                                mParent.setVisibility(View.GONE);
                                mSelected.clear();
                                mRecycleAdapter.notifyDataSetChanged();
                            }*/

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
                            if (offset < 0) {
                                mTopBarView.setRightText("全选");
                            } else {
                                mTopBarView.setRightText("全不选");
                            }
                        }

                        mCurrent.addAll(item.getAllChilds());
                        mAdapter.notifyDataSetChanged();
                        if (!mMultipled) {
                            mTopBarView.setTitle(mTitle);
                            mTopBarView.setRightText("");
                        }

                        String text = mDep.getText().toString();
                        if (TextUtils.isEmpty(text)) {
                            mDep.setText("");
                        } else {
                            String[] strings = text.split(">");
                            StringBuilder builder = new StringBuilder();
                            for (int i = 0, size = strings.length; i < size -1; i++) {
                                builder.append(strings[i] + ">");
                            }
                            text = builder.toString();
                            if (TextUtils.isEmpty(text)) {
                                mDep.setText("");
                            } else {
                                mDep.setText(text.substring(0, text.length() - 1));
                            }

                        }
                    } else {

                        mContext.finish();
                    }

                }
            });
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

                    mTopBarView.setRightText("全选");
                }

                mCurrent.clear();

                mStack.push(info); // 要是有缓存的话这个里面已经就有数据了,不需要重新添加.
                mDep.setText(mDep.getText() + ">" + info.getItemValue());

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
                        if (oldSize >= cacheSize) {
                            mTopBarView.setRightText("全不选");
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
                    ((CheckBox) view.findViewById(R.id.choose_file_item_checkbox)).setChecked(false);
                } else {
                    mAllSeleceted.add(info);
                    mSelected.add(info);
                    ((CheckBox) view.findViewById(R.id.choose_file_item_checkbox)).setChecked(true);
                }
                info.setSelected(!info.isSelected());
                int size = mSelected.size();
                if (mAllSeleceted.size() > 0) {
                    if (mParent.getVisibility() != View.VISIBLE)
                        mParent.setVisibility(View.VISIBLE);
                    mTopBarView.setTitle(mTitle + "(" + mAllSeleceted.size() + ")");
                    HcLog.D(TAG + "#onItemClick current size = "+size + " all child size = "+mStack.peek().getChilds().size());
                    if (size == mStack.peek().getChilds().size()) { // 说明全部选中了
                        mTopBarView.setRightText("全不选");
                    } else {
                        mTopBarView.setRightText("全选");
                    }
                } else {
                    if (mParent.getVisibility() != View.GONE)
                        mParent.setVisibility(View.GONE);
                    mTopBarView.setTitle(mTitle);
                    mTopBarView.setRightText("全选");
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
                        mTopBarView.setTitle(mTitle + "(" + mAllSeleceted.size() + ")");
                        if (size == mStack.peek().getChilds().size()) {
                            mTopBarView.setRightText("全不选");
                        } else {
                            mTopBarView.setRightText("全选");
                        }
                    } else {
                        mParent.setVisibility(View.GONE);
                        mTopBarView.setTitle(mTitle);
                        mTopBarView.setRightText("全选");
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

    private ChooseStatus mStatus;

    public enum ChooseStatus {
        SINGLE,
        MULTIPLE_UNSELETED,
        MULTIPLE_SELETED,
        MULTIPLE_ALL_SELETED
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

    private void setViews() {
        switch (mStatus) {
            case SINGLE: // 单选模式
                mTopBarView.setTitle(mTitle);

                break;
            case MULTIPLE_UNSELETED:
                mTopBarView.setTitle(mTitle);
                mTopBarView.setRightText("全选");
                mParent.setVisibility(View.GONE);
                mSelected.clear();
                mCurrent.clear();
                mAdapter.notifyDataSetChanged();
                mRecycleAdapter.notifyDataSetChanged();
                break;
            case MULTIPLE_ALL_SELETED:
                mTopBarView.setTitle(mTitle + "(" + mSelected.size() + ")");
                mTopBarView.setRightText("全选");
                if (mParent.getVisibility() != View.VISIBLE)
                    mParent.setVisibility(View.VISIBLE);

                mAdapter.notifyDataSetChanged();
                mRecycleAdapter.notifyDataSetChanged();
                break;
            case MULTIPLE_SELETED:
                mTopBarView.setTitle(mTitle + "(" + mSelected.size() + ")");
                mTopBarView.setRightText("全不选");
                if (mParent.getVisibility() != View.VISIBLE)
                    mParent.setVisibility(View.VISIBLE);

                mAdapter.notifyDataSetChanged();
                mRecycleAdapter.notifyDataSetChanged();
                break;

            default:
                break;
        }
    }

    private void test() {
        ItemInfo info = new DepInfo();
        info.setIconUrl("drawable://" + R.drawable.default_photo);
        info.setItemValue("研发中心");
        info.setItemId("1");
        mStack.peek().addChild(info);
        mCurrent.add(info);
        info = new StaffInfo();
        info.setItemId("2");
        info.setItemValue("张四");
        info.setIconUrl("");
        info.setMultipled(true);
        mCurrent.add(info);
        mStack.peek().addChild(info);
        info = new StaffInfo();
        info.setItemId("3");
        info.setItemValue("王五");
        info.setIconUrl("");
        info.setMultipled(true);
        mCurrent.add(info);
        mStack.peek().addChild(info);
        for (int i = 0; i < 10; i++) {
            info = new StaffInfo();
            info.setItemId("3" + i);
            info.setItemValue("王五" + i);
            info.setIconUrl("");
            info.setMultipled(true);
            mCurrent.add(info);
            mStack.peek().addChild(info);
        }

    }
}
