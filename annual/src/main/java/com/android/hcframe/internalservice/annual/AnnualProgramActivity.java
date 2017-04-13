/*
* @Company 浙 江 鸿 程 计 算 机 系 统 有 限 公 司
* @URL http://www.zjhcsoft.com
* @Address 杭州滨江区伟业路1号
* @Email jinjr@zjhcsoft.com 
* @author jinjr
* @data 2016-1-11 下午5:37:50
*/
package com.android.hcframe.internalservice.annual;

import android.graphics.drawable.LevelListDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.android.hcframe.HcBaseActivity;
import com.android.hcframe.HcDialog;
import com.android.hcframe.HcLog;
import com.android.hcframe.HcUtil;
import com.android.hcframe.adapter.ViewHolderBase;
import com.android.hcframe.adapter.ViewHolderFactory;
import com.android.hcframe.http.HcHttpRequest;
import com.android.hcframe.http.IHttpResponse;
import com.android.hcframe.http.RequestCategory;
import com.android.hcframe.http.ResponseCategory;
import com.android.hcframe.sql.SettingHelper;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class AnnualProgramActivity extends HcBaseActivity implements IHttpResponse,
        OnItemClickListener {

    private static final String TAG = "AnnualProgramActivity";

    private ListView mListView;

    private List<AnnualProgramInfo> mProgramInfos = new ArrayList<AnnualProgramInfo>();

    private AnnualProgramAdapter mAdapter;

    private String mAnnualId;

    private SparseArray<String> mMd5Urls = new SparseArray<String>();
    /**
     * 提交打分的节目
     */
    private AnnualProgramInfo mProgramInfo;

    /**
     * 滑动的节目
     */
    private AnnualProgramInfo mSeekbarInfo;

    private Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            // TODO Auto-generated method stub
            switch (msg.what) {
                case 1001:
                    HcDialog.deleteProgressDialog();
                    break;

                default:
                    break;
            }
        }

    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_annual_program);
        mListView = (ListView) findViewById(R.id.annual_program_listview);
        initData();
    }

    @Override
    public void notify(Object data, RequestCategory request,
                       ResponseCategory category) {
        // TODO Auto-generated method stub
        mMd5Urls.delete(request.ordinal());
        switch (request) {
            case ANNUAL_PROGRAM:
                HcDialog.deleteProgressDialog();
                switch (category) {
                    case NETWORK_ERROR:
                    case SESSION_TIMEOUT:
                        HcUtil.toastNetworkError(this);
                        break;
                    case SYSTEM_ERROR:
                        HcUtil.toastSystemError(this, data);
                        break;
                    case SUCCESS:
                        if (data != null && data instanceof String) {
                            HcLog.D(TAG + " #notify parse SUCCESS data = " + data);
                            try {
                                JSONObject object = new JSONObject((String) data);
                                int code = object.getInt("code");
                                if (code == 0) { // 解析列表
                                    object = object.getJSONObject("body");
                                    if (HcUtil.hasValue(object, "showList")) {
                                        JSONArray array = object.getJSONArray("showList");
                                        int size = array.length();
                                        mProgramInfos.clear();
                                        AnnualProgramInfo info = null;
                                        for (int i = 0; i < size; i++) {
                                            info = new AnnualProgramInfo();
                                            info.setAnnualId(mAnnualId);
                                            info.setShowContent(false);
                                            object = array.getJSONObject(i);
                                            if (HcUtil.hasValue(object, "show_id")) {
                                                info.setProgramId(object.getString("show_id"));
                                            }

                                            if (HcUtil.hasValue(object, "show_type")) {
                                                info.setProgramType(object.getInt("show_type"));
                                            }
                                            if (HcUtil.hasValue(object, "show_title")) {
                                                info.setProgramTitle(object.getString("show_title"));
                                            }
                                            if (HcUtil.hasValue(object, "show_memo")) {
                                                info.setProgramContent(object.getString("show_memo"));
                                            }
                                            if (HcUtil.hasValue(object, "show_score")) {
                                                info.setProgramScore(object.getInt("show_score"));
                                            }

                                            mProgramInfos.add(info);
                                        }
                                        // for 结束
                                        if (mAdapter != null) {
                                            mAdapter.notifyDataSetChanged();
                                        }

                                        // 插入数据
                                        AnnualDatabaseOperate.insertAnnualProagrams(this, mProgramInfos, mAnnualId);
                                    }
                                } else {
                                    if (HcUtil.hasValue(object, "msg")) {
                                        String msg = object.getString("msg");
                                        /**
                                         * czx
                                         * 2016.4.13
                                         */
                                        if (HcHttpRequest.REQUEST_ACCOUT_EXCLUDED == code
                                                || HcHttpRequest.REQUEST_TOKEN_FAILED == code
                                                ) {
                                            if (HcUtil.hasValue(object, "body")) {
                                                HcUtil.reLogining(object.getJSONObject("body").toString(), this, msg);
                                            }
                                        } else {
                                            HcUtil.showToast(this, msg);
                                        }
                                    }
                                }
                            } catch (Exception e) {
                                // TODO: handle exception
                                HcLog.D(TAG + " #notify parse Error e = " + e);
                                HcUtil.toastDataError(this);
                            }

                        }
                        // error
                        break;

                    default:
                        break;
                }
                break;
            case ANNUAL_SCORE:
                switch (category) {
                    case NETWORK_ERROR:
                    case SESSION_TIMEOUT:
                        HcDialog.deleteProgressDialog();
                        HcUtil.toastNetworkError(this);
                        break;
                    case SYSTEM_ERROR:
                        HcDialog.deleteProgressDialog();
                        HcUtil.toastSystemError(this, data);
                        break;
                    case SUCCESS:
                        if (data != null && data instanceof String) {
                            HcLog.D(TAG + " #notify parse SUCCESS data = " + data);
                            try {
                                JSONObject object = new JSONObject((String) data);
                                int code = object.getInt("code");
                                if (code == 0) {
                                    // 更新数据
                                    if (mSeekbarInfo == null && mProgramInfo != null) {
                                        int score = mProgramInfo.getProgramScore();
                                        if (score == 0) {
                                            mProgramInfo.setProgramScore(1);
                                            mAdapter.notifyDataSetChanged();
                                            AnnualDatabaseOperate.updateAnnualProgram(mProgramInfo, this);
                                        }
                                    } else if (mSeekbarInfo != null && mProgramInfo != null) {
                                        if (mSeekbarInfo.getProgramId().equals(mProgramInfo.getProgramId())) {
                                            mProgramInfo.setProgramScore(mSeekbarInfo.getProgramScore());
                                            mAdapter.notifyDataSetChanged();
                                            AnnualDatabaseOperate.updateAnnualProgram(mProgramInfo, this);
                                        } else {
                                            int score = mProgramInfo.getProgramScore();
                                            if (score == 0) {
                                                mProgramInfo.setProgramScore(1);
                                                mAdapter.notifyDataSetChanged();
                                                AnnualDatabaseOperate.updateAnnualProgram(mProgramInfo, this);
                                            }
                                        }
                                    }

                                    HcDialog.showSuccessDialog(this, "评分成功");
                                    mHandler.sendEmptyMessageDelayed(1001, 1500);

                                } else {
                                    HcDialog.deleteProgressDialog();
                                    if (HcUtil.hasValue(object, "msg")) {
                                        String msg = object.getString("msg");
                                        /**
                                         * czx
                                         * 2016.4.13
                                         */
                                        if (HcHttpRequest.REQUEST_ACCOUT_EXCLUDED == code
                                                || HcHttpRequest.REQUEST_TOKEN_FAILED == code
                                                ) {
                                            if (HcUtil.hasValue(object, "body")) {
                                                HcUtil.reLogining(object.getJSONObject("body").toString(), this, msg);
                                            }
                                        } else {
                                            HcUtil.showToast(this, msg);
                                        }
                                    }
                                }
                            } catch (Exception e) {
                                // TODO: handle exception
                                HcLog.D(TAG + " #notify parse Error e = " + e);
                                HcDialog.deleteProgressDialog();
                                HcUtil.toastDataError(this);
                            }
                        }
                        break;

                    default:
                        HcDialog.deleteProgressDialog();
                        break;
                }

                break;

            default:
                break;
        }


    }

    @Override
    public void notifyRequestMd5Url(RequestCategory request, String md5Url) {
        // TODO Auto-generated method stub
        mMd5Urls.append(request.ordinal(), md5Url);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position,
                            long id) {
        // TODO Auto-generated method stub

    }

    private void test() {
        AnnualProgramInfo info = null;
        for (int i = 0; i < 5; i++) {
            info = new AnnualProgramInfo();
            info.setProgramContent("浙江鸿程计算机系统有限公司浙江鸿程计算机系统有限公司浙江鸿程计算机系统有限公司浙江鸿程计算机系统有限公司");
            info.setProgramScore(i);
            info.setProgramTitle("浙江鸿程");
            info.setShowContent(false);
            info.setProgramType((i + 2) / 2);
            mProgramInfos.add(info);
        }
    }

    private void initData() {
        String annualId = parseAnnualId(SettingHelper.getAnnualInfo(this));
        if (TextUtils.isEmpty(annualId)) { // 按道理不会出现的，在AnnualHomeView里面获取
            finish();
            return;
        } else {
            mAnnualId = annualId;
            mProgramInfos.addAll(AnnualDatabaseOperate.getAnnualProgramInfos(this, annualId));
        }

        /**
         * test
         */
//		test();

        if (mAdapter == null) {
            mAdapter = new AnnualProgramAdapter(this, mProgramInfos, new AnnualViewHoderFactory());
            mListView.setAdapter(mAdapter);
        } else {
            mAdapter.notifyDataSetChanged();
        }

        if (mProgramInfos.size() == 0 && HcUtil.isNetWorkAvailable(this)) { // 去服务端获取数据
            HcDialog.showProgressDialog(this, R.string.dialog_title_get_data);
            HcHttpRequest.getRequest().sendAnnualProgramListCommand(annualId, this);
        }
    }

    private String parseAnnualId(String data) {
        try {
            JSONObject object = new JSONObject(data);
            if (HcUtil.hasValue(object, "annual_id")) {
                return object.getString("annual_id");
            }
        } catch (Exception e) {
            // TODO: handle exception
            HcLog.D(TAG + " #parseAnnualId data = " + data + " e = " + e);
        }
        return null;
    }

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        HcDialog.deleteProgressDialog();
        int size = mMd5Urls.size();
        for (int i = 0; i < size; i++) {
            HcHttpRequest.getRequest().cancelRequest(mMd5Urls.get(i));
        }
        mMd5Urls.clear();

        super.onDestroy();
    }

    private class AnnualViewHoderFactory implements ViewHolderFactory<AnnualProgramInfo> {

        @Override
        public ViewHolderBase<AnnualProgramInfo> createViewHolder() {
            // TODO Auto-generated method stub
            return new AnnualViewHoder();
        }

    }


    private class AnnualViewHoder implements ViewHolderBase<AnnualProgramInfo> {

        private static final String TAG = AnnualProgramActivity.TAG + "#AnnualViewHoder";

        private LinearLayout mParent;
        private ImageView mIcon;
        private TextView mTitle;
        private TextView mStars;

        private LinearLayout mContentParent;
        private TextView mContent;
        private TextView mStarsBtn;
        private SeekBar mSeekBar;
        private ImageView[] mDots = new ImageView[5];

        private SeekBar mSeekBarTemp;

        @Override
        public View createView(LayoutInflater inflater) {
            // TODO Auto-generated method stub
            View view = inflater.inflate(R.layout.annual_program_list_item, null);
            mParent = (LinearLayout) view.findViewById(R.id.annual_program_item_parent);
            mIcon = (ImageView) view.findViewById(R.id.annual_program_item_icon);
            mTitle = (TextView) view.findViewById(R.id.annual_program_item_title);
            mStars = (TextView) view.findViewById(R.id.annual_program_item_stars);

            mContentParent = (LinearLayout) view.findViewById(R.id.annual_program_item_content_parent);
            mContent = (TextView) view.findViewById(R.id.annual_program_item_content);
            mStarsBtn = (TextView) view.findViewById(R.id.annual_program_item_stars_btn);
            mSeekBar = (SeekBar) view.findViewById(R.id.annual_program_item_seekbar);

            mDots[0] = (ImageView) view.findViewById(R.id.annual_program_item_stars_01);
            mDots[1] = (ImageView) view.findViewById(R.id.annual_program_item_stars_02);
            mDots[2] = (ImageView) view.findViewById(R.id.annual_program_item_stars_03);
            mDots[3] = (ImageView) view.findViewById(R.id.annual_program_item_stars_04);
            mDots[4] = (ImageView) view.findViewById(R.id.annual_program_item_stars_05);

            mSeekBarTemp = (SeekBar) view.findViewById(R.id.annual_program_item_seekbar_temp);

            HcLog.D(TAG + " #createView thumb offset = " + mSeekBar.getThumbOffset());
            return view;
        }

        @Override
        public void setItemData(final int position, final AnnualProgramInfo data) {
            // TODO Auto-generated method stub
            switch (data.getProgramType()) {
                case AnnualProgramInfo.TYPE_DANCING:
                    mIcon.setImageDrawable(getResources().getDrawable(R.drawable.annual_program_dancing));
                    break;
                case AnnualProgramInfo.TYPE_MUSIC:
                    mIcon.setImageDrawable(getResources().getDrawable(R.drawable.annual_program_song));
                    break;
                case AnnualProgramInfo.TYPE_SKIT:
                    mIcon.setImageDrawable(getResources().getDrawable(R.drawable.annual_program_skit));
                    break;
                default:
                    mIcon.setImageBitmap(null);
                    break;
            }

            mTitle.setText(data.getProgramTitle());

            int score = data.getProgramScore();
            if (score == 0) {
                mStars.setText("待评分");
                mStars.setTextColor(getResources().getColor(R.color.annual_program_item_unstars));
                mSeekBar.setProgress(0);
                mStarsBtn.setText("评1星");
                setDotSrc(1, 5);
            } else {
                if (score > 5) score = 5;
                mStars.setText(score + "星");
                mStars.setTextColor(getResources().getColor(R.color.annual_program_item_stars));
                mSeekBar.setProgress(score - 1);
                mStarsBtn.setText("评" + score + "星");
                setDotSrc(score, 5);
            }

            mContent.setText(data.getProgramContent());


            if (data.getShowContent()) {
                if (mContentParent.getVisibility() != View.VISIBLE)
                    mContentParent.setVisibility(View.VISIBLE);
            } else {
                if (mContentParent.getVisibility() == View.VISIBLE)
                    mContentParent.setVisibility(View.GONE);
            }


            mParent.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    // TODO Auto-generated method stub
                    boolean show = data.getShowContent();
                    for (AnnualProgramInfo info : mProgramInfos) {
                        info.setShowContent(false);
                    }
                    data.setShowContent(!show);
                    if (mAdapter != null)
                        mAdapter.notifyDataSetChanged();
                }
            });

            mStarsBtn.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    // TODO Auto-generated method stub
                    if (HcUtil.isNetWorkAvailable(AnnualProgramActivity.this)) {
                        HcDialog.showProgressDialog(AnnualProgramActivity.this, R.string.dialog_title_post_data);
                        mProgramInfo = data;
                        if (mSeekbarInfo != null && mSeekbarInfo.getProgramId().equals(data.getProgramId())) {
                            HcHttpRequest.getRequest().sendAnnualProgramScoreCommand(mAnnualId, mSeekbarInfo.getProgramScore(),
                                    data.getProgramId(), AnnualProgramActivity.this);
                        } else {
                            HcHttpRequest.getRequest().sendAnnualProgramScoreCommand(mAnnualId, data.getProgramScore() == 0 ? 1 : data.getProgramScore(),
                                    data.getProgramId(), AnnualProgramActivity.this);
                        }

                    }
                }
            });

            mSeekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                    // TODO Auto-generated method stub

                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {
                    // TODO Auto-generated method stub

                }

                @Override
                public void onProgressChanged(SeekBar seekBar, int progress,
                                              boolean fromUser) {
                    // TODO Auto-generated method stub
                    HcLog.D(TAG + " #onProgressChanged progress = " + progress + " fromUser = " + fromUser);
                    if (fromUser) {
                        setDotSrc(progress, 5);
                        mStarsBtn.setText("评" + (progress + 1) + "星");

                        if (progress == 4) {
                            mSeekBarTemp.setProgress(10);
                        } else {
                            mSeekBarTemp.setProgress(5);
                        }

                        if (mSeekbarInfo == null) {
                            mSeekbarInfo = new AnnualProgramInfo(data);
                            mSeekbarInfo.setProgramScore(progress + 1);
                        } else {
                            mSeekbarInfo.setProgramId(data.getProgramId());
                            mSeekbarInfo.setProgramScore(progress + 1);
                            // 其他属性用不到
                        }
                    }

                }
            });
        }

        private void setDotSrc(int stars, int max) {
            for (int i = 0; i < stars; i++) {
                LevelListDrawable drawable = (LevelListDrawable) mDots[i].getDrawable();
                drawable.setLevel(1);
            }

            for (int i = stars; i < max; i++) {
                LevelListDrawable drawable = (LevelListDrawable) mDots[i].getDrawable();
                drawable.setLevel(0);
            }

            if (stars == 5) {
                mSeekBarTemp.setProgress(10);
            } else {
                mSeekBarTemp.setProgress(5);
            }
        }

    }
}
