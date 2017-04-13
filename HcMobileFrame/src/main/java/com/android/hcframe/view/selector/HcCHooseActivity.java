package com.android.hcframe.view.selector;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.hcframe.HcBaseActivity;
import com.android.hcframe.HcLog;
import com.android.hcframe.HcUtil;
import com.android.hcframe.R;
import com.android.hcframe.TopBarView;
import com.android.hcframe.http.AbstractHttpRequest;
import com.android.hcframe.http.AbstractHttpResponse;
import com.android.hcframe.http.RequestCategory;
import com.android.hcframe.pull.PullToRefreshListView;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

/**
 * @Company 浙 江 鸿 程 计 算 机 系 统 有 限 公 司
 * @URL http://www.zjhcsoft.com
 * @Address 杭州滨江区伟业路1号
 * @Email jinjr@zjhcsoft.com
 * Created by jrjin on 16-7-26 15:53.
 */
public class HcCHooseActivity extends HcBaseActivity implements AdapterView.OnItemClickListener {

    private static final String TAG = "HcCHooseActivity";

    /** 部门的层级关系 */
    private Stack<ItemInfo> mStack = new Stack<ItemInfo>(); // 其实都是DepInfo

    private List<ItemInfo> mCurrent = new ArrayList<ItemInfo>();

    private List<ItemInfo> mSelected = new ArrayList<ItemInfo>();

    private PersonnelChooseAdapter mAdapter;

    private PullToRefreshListView mListView;

    private TextView mDep;

    private LinearLayout mParent;

    private RecyclerView mRecyclerView;

    private TextView mOkBtn;

    private TopBarView mTopBarView;

    private boolean mMultipled = true;

    public static final String CHOOSE_KEY = "multiple";

    private ChooseRecycleAdapter mRecycleAdapter;

    private Handler mHandler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_personnel);
        Intent intent = getIntent();
        if (intent != null && intent.getExtras() != null) {
            mMultipled = intent.getBooleanExtra(CHOOSE_KEY, false);
        }
        initViews();
        initData();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void initViews() {
        mTopBarView = (TopBarView) findViewById(R.id.choose_personnel_top_bar);
        mDep = (TextView) findViewById(R.id.choose_personnel_dep_name);
        mListView = (PullToRefreshListView) findViewById(R.id.choose_personnel_listview);
        mOkBtn = (TextView) findViewById(R.id.choose_personnel_ok_btn);
        mParent = (LinearLayout) findViewById(R.id.choose_personnel_recycler_parent);
        mRecyclerView = (RecyclerView) findViewById(R.id.choose_personnel_recycler);

        mListView.setOnItemClickListener(this);
    }

    private void initData() {
        test();
        mTopBarView.setTitle("组员选择");
        if (mMultipled) {
            mTopBarView.setRightBtnVisiable(View.VISIBLE);
        }
        mAdapter = new PersonnelChooseAdapter(this, mCurrent);
        mListView.setAdapter(mAdapter);

        mRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        mRecycleAdapter = new ChooseRecycleAdapter();
        mRecyclerView.setAdapter(mRecycleAdapter);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

    }

    private class ChooseRecycleAdapter extends RecyclerView.Adapter<ViewHolder> {

        @Override
        public int getItemCount() {
            return mSelected.size();
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
            HcLog.D(TAG + " $ChooseRecycleAdapter#onCreateViewHolder!!!!!");
            return new ViewHolder(LayoutInflater.from(HcCHooseActivity.this).inflate(R.layout.choose_personnel_choosed_item_layout, viewGroup, false));
        }

        @Override
        public void onBindViewHolder(ViewHolder viewHolder, int position) {
            ItemInfo info = mSelected.get(position);
            viewHolder.mName.setText(info.getItemValue());
            ImageLoader.getInstance().displayImage(info.getIconUrl(), viewHolder.mIcon, HcUtil.getAccountImageOptions());
        }
    }

    private static class ViewHolder extends RecyclerView.ViewHolder {

        TextView mName;

        ImageView mIcon;

        public ViewHolder(View itemView) {
            super(itemView);

            mName = (TextView) itemView.findViewById(R.id.choose_personnel_choosed_item_name);
            mIcon = (ImageView) itemView.findViewById(R.id.choose_personnel_choosed_item_icon);
        }
    }

    private void test() {
        ItemInfo info = new DepInfo();
        info.setIconUrl("drawable://" + R.drawable.default_photo);
        info.setItemValue("研发中心");
        info.setItemId("1");
        mCurrent.add(info);
        info = new StaffInfo();
        info.setItemId("2");
        info.setItemValue("张四");
        info.setIconUrl("");
        info.setSelected(true);
        info.setMultipled(true);
        mCurrent.add(info);
        mSelected.add(info);
        info = new StaffInfo();
        info.setItemId("3");
        info.setItemValue("王五");
        info.setIconUrl("");
        info.setSelected(false);
        info.setMultipled(true);
        mCurrent.add(info);
        mSelected.add(info);
        mCurrent.add(info);
        mSelected.add(info);
        mCurrent.add(info);
        mSelected.add(info);
        mCurrent.add(info);
        mSelected.add(info);
        mCurrent.add(info);
        mSelected.add(info);
        mCurrent.add(info);
        mSelected.add(info);
        mCurrent.add(info);
        mSelected.add(info);
        mCurrent.add(info);
        mSelected.add(info);
        mCurrent.add(info);
        mSelected.add(info);
        mCurrent.add(info);
        mSelected.add(info);
        mCurrent.add(info);
        mSelected.add(info);
        mCurrent.add(info);
        mSelected.add(info);

    }

    private class StaffRequest extends AbstractHttpRequest {

        @Override
        public String getParameterUrl() {
            return null;
        }

        @Override
        public String getRequestMethod() {
            return null;
        }
    }


    private class StaffResponse extends AbstractHttpResponse {

        @Override
        public String getTag() {
            return null;
        }

        @Override
        public void onSuccess(Object data, RequestCategory request) {

        }

        @Override
        public void onAccountExcluded(String data, String msg, RequestCategory category) {

        }

        @Override
        public void notifyRequestMd5Url(RequestCategory request, String md5Url) {

        }
    }


}
