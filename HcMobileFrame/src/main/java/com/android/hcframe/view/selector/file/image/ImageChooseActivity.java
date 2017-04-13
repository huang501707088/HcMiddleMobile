package com.android.hcframe.view.selector.file.image;

import android.content.Intent;
import android.os.Bundle;
import android.widget.FrameLayout;

import com.android.hcframe.HcBaseActivity;
import com.android.hcframe.R;
import com.android.hcframe.TopBarView;

/**
 * @Company 浙 江 鸿 程 计 算 机 系 统 有 限 公 司
 * @URL http://www.zjhcsoft.com
 * @Address 杭州滨江区伟业路1号
 * @Email jinjr@zjhcsoft.com
 * Created by jrjin on 16-11-28 16:35.
 */

public class ImageChooseActivity extends HcBaseActivity {

    private static final String TAG = "ImageChooseActivity";

    private FrameLayout mParent;

    public static final String SELECT_MULTIPLED = "multipled";

    public static final String SCOP = "crop";

    public static final String SELECT_COUNT = "count";

    private ImageChooseView mChooseView;

    private TopBarView mTopBarView;

    private String mData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        boolean multipled = getIntent().getBooleanExtra(SELECT_MULTIPLED, true);
        boolean crop = getIntent().getBooleanExtra(SCOP, false);
        int count = getIntent().getIntExtra(SELECT_COUNT, 0);
        mData = getIntent().getStringExtra("data");
        if (mData == null)
            mData = "";
        setContentView(R.layout.activity_choose_image);
        mTopBarView = (TopBarView) findViewById(R.id.image_choose_top_bar);
        mParent = (FrameLayout) findViewById(R.id.image_choose_parent);
        mTopBarView.setTitle("图片选择");
        mChooseView = new ImageChooseView(this, mParent, mData, multipled, crop, count);
        mChooseView.changePages();
    }

    @Override
    protected void onDestroy() {
        if (mChooseView != null) {
            mChooseView.onDestory();
            mChooseView = null;
        }
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (mChooseView != null) {
            mChooseView.onActivityResult(requestCode, resultCode, data);
        }
    }
}
