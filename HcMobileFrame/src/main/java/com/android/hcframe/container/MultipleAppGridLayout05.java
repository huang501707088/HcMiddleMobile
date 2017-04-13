package com.android.hcframe.container;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.hcframe.DraggableGridViewPager;
import com.android.hcframe.HcUtil;
import com.android.hcframe.R;
import com.android.hcframe.adapter.HcBaseAdapter;
import com.android.hcframe.container.data.MultipleAppAdapter05;
import com.android.hcframe.container.data.ViewInfo;

import java.util.List;

/**
 * @Company 浙 江 鸿 程 计 算 机 系 统 有 限 公 司
 * @URL http://www.zjhcsoft.com
 * @Address 杭州滨江区伟业路1号
 * @Email jinjr@zjhcsoft.com
 * Created by jrjin on 16-6-6 14:16.
 */
public class MultipleAppGridLayout05 extends AbstractMultipleAppLayout {

    private final int mColumn;

    private int mRow;

    private final boolean mShowDivider;

    public MultipleAppGridLayout05() {
        mLayoutId = R.layout.container_multiple_grid_layout05;
        mColumn = 3;
        mShowDivider = true;
    }

    @Override
    public View createAppView(Context context, ViewGroup parent, ViewInfo info) {
        // TODO Auto-generated method stub
        mContext = context;
        if (mLayoutId != 0) {
            ViewGroup layout = (ViewGroup) ((LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).
                    inflate(mLayoutId, parent, false);
            List<ViewInfo> apps = info.getViewInfos();
            int size = apps.size();
            if (!apps.isEmpty()) {
                mAppViewInfo = info;
                if (mAdapter == null) {
                    mAdapter = getAdapter(context, apps);
                }

                mRow = (size + mColumn - 1) / mColumn;

                DraggableGridViewPager gridview = (DraggableGridViewPager) layout;
                if (mShowDivider)
                    gridview.setShowDividers(mShowDivider, mShowDivider);
                int itemHeight = (int) (90 * HcUtil.getScreenDensity());

                ViewGroup.LayoutParams params = layout.getLayoutParams();
                params.height = mRow * itemHeight;
                gridview.setLayoutParams(params);

                gridview.setRowCount(mRow);
                gridview.setColCount(mColumn);
                gridview.setAdapter(mAdapter);
                gridview.setOnItemClickListener(this);

            }
            mParent = layout;

            return layout;
        }
        return null;
    }

    @Override
    public HcBaseAdapter<?> getAdapter(Context context, List<ViewInfo> infos) {
        // TODO Auto-generated method stub
        return new MultipleAppAdapter05(context, infos);
    }
}
