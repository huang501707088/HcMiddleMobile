package com.android.hcframe.schedule;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;

import com.android.hcframe.HcBaseActivity;
import com.android.hcframe.HcDialog;
import com.android.hcframe.HcLog;
import com.android.hcframe.HcUtil;
import com.android.hcframe.ModuleBridge;
import com.android.hcframe.TopBarView;
import com.android.hcframe.http.AbstractHttpRequest;
import com.android.hcframe.http.AbstractHttpResponse;
import com.android.hcframe.http.RequestCategory;
import com.android.hcframe.menu.DownloadPDFActivity;
import com.android.hcframe.pull.ScrollListView;
import com.android.hcframe.servicemarket.photoscan.ImageScanActivity;
import com.android.hcframe.sql.SettingHelper;
import com.android.hcframe.view.OneBtnAlterDialog;
import com.android.hcframe.view.gallery.GalleryView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.android.hcframe.schedule.ScheduleAddActivity.REQUEST_CODE_MODIFY;

/**
 * Created by zhujiabin on 2016/11/22.
 */

public class ScheduleDetailActivity extends HcBaseActivity implements View.OnClickListener, AdapterView.OnItemClickListener {
    private String TAG = "ScheduleDetailActivity";
    private TopBarView mTopBarView;
    private TextView mScheduleDetailTime;
    private TextView mScheduleDetailName;
    private TextView mScheduleDetailYear;
    private TextView mScheduleDetailDay;
    private TextView mScheduleDetailTheme;
    private LinearLayout mScheduleDetailTextLinear;
    private TextView mScheduleDetailText;
    private TextView mScheduleDetailNames;
    private TextView mScheduleDetailContent;
    private TextView mScheduleDetailTitle;
    private ImageView mScheduleDetailImg;
    private ScrollListView mScheduleDetailList;
    private ScrollView mScrollView;
    private GalleryView mGalleryView;
    private ScheduleInfo mScheduleInfo;
    private Handler mHandler = new Handler();
    private ScheduleDetailAdapter adapter;
    /**
     * 日程安排的Id
     */
    private String mId;

    /**
     * 日程开始时间
     */
    private String mStartTime;
    /**
     * 日程结束时间
     */
    private String mEndTime;
    /**
     * 日程的任务类型：外派，内部任务等
     */
    private String mTaskType;
    /**
     * 日程参与人员
     */
    private String mTaskMembers;
    /**
     * 日程主题
     */
    private String mTheme;
    /**
     * 创建者
     */
    private String mCreator;
    /**
     * 创建者id
     */
    private String mCreatorId;
    /**
     * 创建者名字
     */
    private String mCreatorName;
    /**
     * 发起人
     */
    private String mExecutor;

    /**
     * 是否为创建者
     */
    private String mCreatFlag;

    /**
     * 日程内容
     */
    private String mContent;

    /**
     * 是否是参与人员
     */

    private String isParticipanter;
    /**
     * 日程参与人员Id
     */
    private String participantId;
    /**
     * 日程参与人员名称
     */
    private String participantName;
    /**
     * 是否修改过
     */
    private boolean isModifyFlag = false;
    private ScheduleDetailsInfo info;
    private static final int MAX_IMAGE_SIZE = 320 * 480 / 4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.schedule_detail_layout);
        initView();
        initData();
    }

    private void initData() {
        Intent intent = getIntent();
        if (intent != null) {
            mScheduleInfo = intent.getParcelableExtra("scheduleInfo");
            if (mScheduleInfo != null) {
                info = new ScheduleDetailsInfo(mScheduleInfo);
                mId = mScheduleInfo.getId();
                mStartTime = mScheduleInfo.getStartTime();
                mEndTime = mScheduleInfo.getEndTime();
                mTaskType = mScheduleInfo.getTaskType();
                mTaskMembers = mScheduleInfo.getTaskMembers();
                mTheme = mScheduleInfo.getTheme();
                mCreator = mScheduleInfo.getCreator();
                mCreatFlag = mScheduleInfo.getCreatFlag();
                mContent = mScheduleInfo.getContent();
                if (mStartTime != null && mEndTime != null) {
                    HcLog.D("mStartTime=" + mStartTime + ",mEndTime = " + mEndTime);
                    if (ScheduleUtils.isToday(Long.parseLong(mStartTime), Long.parseLong(mEndTime))) {
                        mScheduleDetailTime.setText(ScheduleUtils.stampToTime(mStartTime) + "~" + ScheduleUtils.stampToTime(mEndTime));
                    } else {
                        mScheduleDetailTime.setText(ScheduleUtils.stampToHomeDate(mStartTime) + "~" + ScheduleUtils.stampToHomeDate(mEndTime));
                    }
                }
//                else if (mStartTime != null && mEndTime == null) {
//                    String[] startTimes = ScheduleUtils.stampToHomeDate(mStartTime).split("\\s");
//                    String mMonth = startTimes[0].split("/")[0];
//                    String mDay = startTimes[0].split("/")[1];
//                    Calendar mCalendar = Calendar.getInstance();
//                    int month = mCalendar.get(Calendar.MONTH);
//                    int day = mCalendar.get(Calendar.DAY_OF_MONTH);
//                    HcLog.D(" mMonth = " + mMonth + ",mDay = " + mDay + ",month = " + month + ",day=" + day);
//                    if ((String.valueOf(month + 1)).equals(mMonth) && (String.valueOf(day)).equals(mDay)) {
//                        mScheduleDetailTime.setText(ScheduleUtils.stampToHomeDate(mStartTime));
//                    }
//                } else if (mStartTime == null && mEndTime == null) {
//                    mScheduleDetailTime.setText("");
//                }
                mScheduleDetailName.setText(mCreator);
                String mDate = ScheduleUtils.stampToDate(mScheduleInfo.getDate());
                HcLog.D("mDate =" + mDate);
                if (mDate != null) {
                    String[] dates = mDate.split("\\.");
                    mScheduleDetailYear.setText(dates[0] + "." + dates[1]);
                    mScheduleDetailDay.setText(dates[2]);
                } else {
                    mScheduleDetailYear.setText("");
                    mScheduleDetailDay.setText("");
                }
                mScheduleDetailTheme.setText(mTheme);
                mExecutor = "";
                HcLog.D("mTaskMembers = " + mTaskMembers);
                if (mTaskMembers != null) {
                    String taskMembers = mTaskMembers.replaceAll(";", "、");
                    mScheduleDetailNames.setText(taskMembers);
                } else {
                    mScheduleDetailNames.setText("");
                }
                mScheduleDetailContent.setText(mContent);
                //推送的id
                String scheduleId = intent.getStringExtra("scheduleId");
                if (scheduleId != null) {
                    mId = scheduleId;
                }
                ScheduleDetailsRequest dRequest = new ScheduleDetailsRequest(mId);
                ScheduleDetailsResponse dResponse = new ScheduleDetailsResponse();
                HcDialog.showProgressDialog(this, "获取详情列表");
                dRequest.sendRequestCommand(RequestCategory.NONE, dResponse, false);
            }
        }
    }

    private void initView() {
        mTopBarView = (TopBarView) findViewById(R.id.schedule_detail_top_bar);
        mScheduleDetailTime = (TextView) findViewById(R.id.schedule_detail_time);
        mScheduleDetailName = (TextView) findViewById(R.id.schedule_detail_name);
        mScheduleDetailYear = (TextView) findViewById(R.id.schedule_detail_year);
        mScheduleDetailDay = (TextView) findViewById(R.id.schedule_detail_day);
        mScheduleDetailTheme = (TextView) findViewById(R.id.schedule_detail_theme);
        mScheduleDetailText = (TextView) findViewById(R.id.schedule_detail_text);
        mScheduleDetailTextLinear = (LinearLayout) findViewById(R.id.schedule_detail_text_linear);
        mScheduleDetailImg = (ImageView) findViewById(R.id.schedule_detail_img);
        mScheduleDetailNames = (TextView) findViewById(R.id.schedule_detail_names);
        mScheduleDetailContent = (TextView) findViewById(R.id.schedule_detail_content);
        mScheduleDetailList = (ScrollListView) findViewById(R.id.schedule_detail_list);
        mGalleryView = (GalleryView) findViewById(R.id.schedule_detail_galleryview);
        mScheduleDetailTitle = (TextView) findViewById(R.id.schedule_detail_title);
        mScrollView = (ScrollView) findViewById(R.id.schedule_detail_scrollview);
        mScheduleDetailTextLinear.setOnClickListener(this);
        mTopBarView.setTitle("日程详情");
        mTopBarView.setReturnViewListener(new View.OnClickListener() {
            public void onClick(View v) {
                //添加，跳转SheduleMenuHomePage
                Intent intent = new Intent();
                intent.putExtra("isModifyFlag", isModifyFlag);
                setResult(Activity.RESULT_OK, intent);
                finish();
            }
        });
        mTopBarView.setMenuListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isModifyFlag = true;
                Intent intent = new Intent(ScheduleDetailActivity.this, ScheduleAddActivity.class);
                intent.putExtra("schedule", info);
                startActivityForResult(intent, REQUEST_CODE_MODIFY);
            }
        });
        mScheduleDetailList.setOnItemClickListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mScrollView.smoothScrollTo(0, 0);
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.schedule_detail_text_linear) {
            HcLog.D(" mCreatorId = " + mCreatorId);
            //跳转到通讯录
            ModuleBridge.startContactDetailsActivity(this, mCreatorId);
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_CODE_MODIFY) {

                if (data != null && data.getExtras() != null) {
                    boolean failed = data.getBooleanExtra("failed", false);
                    if (failed) { // 附件上传失败了！
                        final OneBtnAlterDialog dialog = OneBtnAlterDialog.createDialog(ScheduleDetailActivity.this, "附件未全部上传!");
                        dialog.setCancelable(false);
                        dialog.btn_ok.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                dialog.dismiss();

                                mGalleryView.clearItems();
                                //重新刷新数据
                                ScheduleDetailsRequest dRequest = new ScheduleDetailsRequest(mId);
                                ScheduleDetailsResponse dResponse = new ScheduleDetailsResponse();
                                HcDialog.showProgressDialog(ScheduleDetailActivity.this, "获取详情列表");
                                dRequest.sendRequestCommand(RequestCategory.NONE, dResponse, false);
                            }
                        });
                        dialog.show();
                        return;
                    }
                }
                mGalleryView.clearItems();
                //重新刷新数据
                ScheduleDetailsRequest dRequest = new ScheduleDetailsRequest(mId);
                ScheduleDetailsResponse dResponse = new ScheduleDetailsResponse();
                HcDialog.showProgressDialog(this, "获取详情列表");
                dRequest.sendRequestCommand(RequestCategory.NONE, dResponse, false);
            }
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        ScheduleDetailAddition addition = (ScheduleDetailAddition) parent.getAdapter().getItem(position);
        String mAddId = addition.getmAdditionId();
        String mAddName = addition.getmAdditionName();
        if (mAddName.contains(".jpg")) {
            String jpgUrl = "";
//            startImageScanActivity(0, jpgUrl, mAddName);
        } else if (mAddName.contains(".pdf")) {
            String pdfUrl = "";
            startPDFActivity(pdfUrl, mAddName);
        }
    }

    private void startPDFActivity(String url, String title) {
        Intent intent = new Intent(this, DownloadPDFActivity.class);
        intent.putExtra("url", url);
        intent.putExtra("title", title);
        startActivity(intent);
        overridePendingTransition(0, 0);
    }

    private class ScheduleDetailsRequest extends AbstractHttpRequest {
        Map<String, String> httpparams = new HashMap<String, String>();

        public ScheduleDetailsRequest(String scheduleId) {
            httpparams.put("scheduleId", scheduleId);
        }

        @Override
        public String getRequestMethod() {
            return "getScheduleInfo";
        }

        @Override
        public String getParameterUrl() {
            String stuxx = "";
            try {
                stuxx = HcUtil.getGetParams(HcUtil.mapToList(httpparams));
            } catch (Exception e) {
                e.printStackTrace();
            }
            return stuxx;
        }
    }

    private class ScheduleDetailsResponse extends AbstractHttpResponse {

        @Override
        public void onSuccess(Object data, RequestCategory request) {
            HcLog.D(TAG + "   = " + data + " request = " + request);
            HcDialog.deleteProgressDialog();
            final StringBuilder sb = new StringBuilder();
            try {
                JSONObject object = new JSONObject((String) data);
                JSONObject jsonScheduleObj = object.getJSONObject("scheduleInfo");
                if (HcUtil.hasValue(jsonScheduleObj, "scheduleId")) {
                    mId = jsonScheduleObj.getString("scheduleId");
                    info.setId(mId);
                }
                if (HcUtil.hasValue(jsonScheduleObj, "startTime") && HcUtil.hasValue(jsonScheduleObj, "endTime")) {
                    mStartTime = jsonScheduleObj.getString("startTime");
                    info.setStartTime(mStartTime);
                    mEndTime = jsonScheduleObj.getString("endTime");
                    info.setEndTime(mEndTime);
                }
                if (HcUtil.hasValue(jsonScheduleObj, "theme")) {
                    mTheme = jsonScheduleObj.getString("theme");
                    info.setTheme(mTheme);
                }
                if (HcUtil.hasValue(jsonScheduleObj, "content")) {
                    mContent = jsonScheduleObj.getString("content");
                    info.setContent(mContent);
                }
                if (HcUtil.hasValue(jsonScheduleObj, "executorName")) {
                    mExecutor = jsonScheduleObj.getString("executorName");
                    info.setExecutor(mExecutor);
                }
                if (HcUtil.hasValue(jsonScheduleObj, "executorId")) {
                    String executorId = jsonScheduleObj.getString("executorId");
                    info.setExecutor(executorId + "_" + mExecutor);
                }
                StringBuilder cr = new StringBuilder();
                if (HcUtil.hasValue(jsonScheduleObj, "create_user_id") && HcUtil.hasValue(jsonScheduleObj, "creatorName")) {
                    cr.append(jsonScheduleObj.getString("create_user_id"));
                    cr.append("_");
                    cr.append(jsonScheduleObj.getString("creatorName"));
                    mCreator = cr.toString();
                    info.setCreator(mCreator);
                    HcLog.D(" mCreator = " + mCreator);
                }
                if (HcUtil.hasValue(jsonScheduleObj, "isParticipanter")) {
                    isParticipanter = jsonScheduleObj.getString("isParticipanter");
                    info.setCreatFlag(isParticipanter);
                }
                if (HcUtil.hasValue(jsonScheduleObj, "participantList")) {
                    JSONArray participantArray = jsonScheduleObj.getJSONArray("participantList");
                    int participantLength = participantArray.length();
                    StringBuilder tm = new StringBuilder();
                    if (participantLength > 0) {
                        for (int j = 0; j < participantLength; j++) {
                            JSONObject participantObj = participantArray.getJSONObject(j);
                            if (HcUtil.hasValue(participantObj, "participantName")) {
                                sb.append(participantObj.getString("participantName") + "、");
                                participantName = participantObj.getString("participantName");
                            }
                            if (HcUtil.hasValue(participantObj, "participantId")) {
                                participantId = participantObj.getString("participantId");
                            }
                            tm.append(participantId);
                            tm.append("_");
                            tm.append(participantName + ";");
                        }
                        info.setTaskMembers(tm.toString().substring(0, tm.toString().length() - 1));
                    }
                }
                if (HcUtil.hasValue(jsonScheduleObj, "fileList")) {
                    String fileId = null, fileName = null;
                    StringBuilder sf = new StringBuilder();
                    JSONArray fileArray = jsonScheduleObj.getJSONArray("fileList");
                    int fileLength = fileArray.length();
                    if (fileLength > 0) {
                        for (int j = 0; j < fileLength; j++) {
                            JSONObject fileObj = fileArray.getJSONObject(j);

                            if (HcUtil.hasValue(fileObj, "fileId")) {
                                fileId = fileObj.getString("fileId");
                            }
                            if (HcUtil.hasValue(fileObj, "fileName")) {
                                fileName = fileObj.getString("fileName");
                            }
                            sf.append(fileId);
                            sf.append(":");
                            sf.append(fileName + ";");
                        }
                        info.setAnnexList(sf.toString().substring(0, sf.toString().length() - 1));
                        HcLog.D("zhujiabinINFO = " + sf.toString().substring(0, sf.toString().length() - 1));
                    }
                }
            } catch (JSONException e) {
                HcLog.D(TAG + " #onSuccess parseJson error =" + e);
            }

            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (mStartTime != null && mEndTime != null) {
                        if (ScheduleUtils.isToday(Long.parseLong(mStartTime), Long.parseLong(mEndTime))) {
                            mScheduleDetailTime.setText(ScheduleUtils.stampToTime(mStartTime) + "~" + ScheduleUtils.stampToTime(mEndTime));
                        } else {
                            mScheduleDetailTime.setText(ScheduleUtils.stampToHomeDate(mStartTime) + "~" + ScheduleUtils.stampToHomeDate(mEndTime));
                        }
                    }
//                    else if (mStartTime != null && mEndTime == null) {
//                        String[] startTimes = ScheduleUtils.stampToHomeDate(mStartTime).split("\\s");
//                        String mMonth = startTimes[0].split("/")[0];
//                        String mDay = startTimes[0].split("/")[1];
//                        Calendar mCalendar = Calendar.getInstance();
//                        int month = mCalendar.get(Calendar.MONTH);
//                        int day = mCalendar.get(Calendar.DAY_OF_MONTH);
//                        HcLog.D(" mMonth = " + mMonth + ",mDay = " + mDay + ",month = " + month + ",day=" + day);
//                        if ((String.valueOf(month + 1)).equals(mMonth) && (String.valueOf(day)).equals(mDay)) {
//                            mScheduleDetailTime.setText(ScheduleUtils.stampToHomeDate(mStartTime));
//                        }
//                    } else if (mStartTime == null && mEndTime == null) {
//                        mScheduleDetailTime.setText("");
//                    }
                    if (mTheme != null) {
                        mScheduleDetailTheme.setText(mTheme);
                    }

                    if (mContent != null) {
                        mScheduleDetailContent.setText(mContent);
                    }
                    if (mExecutor != null) {
                        mScheduleDetailName.setText(mExecutor);
                    }
                    if (mCreator != null) {
                        HcLog.D("userId=" + SettingHelper.getUserId(ScheduleDetailActivity.this) + ",mCreatorId=" + mCreatorId);
                        mCreatorId = mCreator.split("_")[0];
                        mCreatorName = mCreator.split("_")[1];
                        if ("1".equals(isParticipanter)) {
                            mTopBarView.setMenuBtnVisiable(View.GONE);
                            mScheduleDetailText.setText("由" + mCreatorName + "创建");
                            mScheduleDetailTextLinear.setVisibility(View.VISIBLE);
                        } else {
                            mScheduleDetailTextLinear.setVisibility(View.GONE);
                            mTopBarView.setMenuBtnVisiable(View.VISIBLE);
                            mTopBarView.setMenuSrc(R.drawable.schudule_detail_pen);
                        }
                    }
                    if ("0".equals(isParticipanter)) {
                        mScheduleDetailImg.setVisibility(View.GONE);
                    } else if ("1".equals(isParticipanter)) {
                        mScheduleDetailImg.setVisibility(View.VISIBLE);
                    }
                    if (!sb.toString().equals("")) {
                        mScheduleDetailNames.setText(sb.toString().substring(0, sb.toString().length() - 1));
                    }

                    String annexList = info.getAnnexList();
                    HcLog.D(TAG + " #setScheduleData annexList = " + annexList);
                    if (!TextUtils.isEmpty(annexList)) {
                        String[] anneies = annexList.split(";");
                        String id;
                        String name;
                        String[] annex;
                        GalleryView.GalleryItemInfo item;
                        List<ScheduleDetailAddition> additionList = new ArrayList<ScheduleDetailAddition>();
                        ScheduleDetailAddition addition;
                        for (String file : anneies) {
                            annex = file.split(":");
                            id = annex[0];
                            name = annex[1];
                            //如果是图片的后缀名
                            if (name.contains(".jpg") || name.contains(".JPG") || name.contains(".png") || name.contains(".PNG")) {
                                if (ScheduleUtils.isImage(name)) {
                                    item = new GalleryView.GalleryItemInfo();
                                    item.mSeleted = false;
                                    item.mUri = ScheduleUtils.getUrl(id);
                                    HcLog.D("item.mUri = " + item.mUri);
                                    mGalleryView.addItem(item);
                                }
                            } else {
                                addition = new ScheduleDetailAddition();
                                addition.setmAdditionId(id);
                                addition.setmAdditionName(name);
                                additionList.add(addition);
                                adapter = new ScheduleDetailAdapter(ScheduleDetailActivity.this, additionList);
                                mScheduleDetailList.setAdapter(adapter);
                            }
                        }
                    } else {
                        mScheduleDetailTitle.setVisibility(View.GONE);
                    }
                }
            });
        }

        @Override
        public String getTag() {
            return null;
        }

        @Override
        public void onAccountExcluded(String data, String msg, RequestCategory category) {
            HcDialog.deleteProgressDialog();
            HcUtil.reLogining(data, ScheduleDetailActivity.this, msg);
        }

        @Override
        public void onNetworkInterrupt(RequestCategory request) {
            super.onNetworkInterrupt(request);
        }

        @Override
        public void onConnectionTimeout(RequestCategory request) {
            super.onConnectionTimeout(request);
        }

        @Override
        public void onParseDataError(RequestCategory request) {
            super.onParseDataError(request);
        }

        @Override
        public void onRequestFailed(int code, String msg, RequestCategory request) {
            super.onRequestFailed(code, msg, request);
        }

        @Override
        public void onResponseFailed(int code, RequestCategory request) {
            super.onResponseFailed(code, request);
        }

        @Override
        public void unknown(RequestCategory request) {
            super.unknown(request);
        }

        @Override
        public void notifyRequestMd5Url(RequestCategory request, String md5Url) {

        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
