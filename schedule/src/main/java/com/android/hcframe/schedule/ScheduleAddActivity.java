package com.android.hcframe.schedule;

import android.app.Activity;
import android.app.TimePickerDialog;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TimePicker;

import com.android.frame.download.DownloadUtil;
import com.android.frame.download.FileColumn;
import com.android.frame.download.HcDownloadService;
import com.android.frame.download.ServiceUtils;
import com.android.hcframe.HcApplication;
import com.android.hcframe.HcBaseActivity;
import com.android.hcframe.HcDialog;
import com.android.hcframe.HcLog;
import com.android.hcframe.HcUtil;
import com.android.hcframe.TopBarView;
import com.android.hcframe.http.AbstractHttpRequest;
import com.android.hcframe.http.AbstractHttpResponse;
import com.android.hcframe.http.HttpRequestQueue;
import com.android.hcframe.http.RequestCategory;
import com.android.hcframe.view.datepicker.DatePickerDialog;
import com.android.hcframe.sql.SettingHelper;
import com.android.hcframe.view.gallery.GalleryView;
import com.android.hcframe.view.gallery.RecyclerViewDivider;
import com.android.hcframe.view.selector.HcChooseHomeView;
import com.android.hcframe.view.selector.ItemInfo;
import com.android.hcframe.view.selector.StaffInfo;
import com.android.hcframe.view.selector.file.image.ImageItemInfo;

import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.protocol.HTTP;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;


/**
 * Created by zhujiabin on 2016/11/18.
 */

public class ScheduleAddActivity extends HcBaseActivity implements View.OnClickListener,
        DatePickerDialog.OnDateSetListener, ServiceConnection {

    private static final String TAG = "ScheduleAddActivity";

    private TopBarView mTopBarView;
    /**
     * 主题
     */
    private EditText mThemeEdit;
    /**
     * 内容
     */
    private EditText mContentEdit;
    /**
     * 执行人
     */
    private TextView mExcutorText;

    private View mExcutorDivider;

    /**
     * 开始时间
     */
    private TextView mStartTime;
    /**
     * 结束时间
     */
    private TextView mEndTime;
    /**
     * 确认提交按钮
     */
    private TextView mScheduleAddSubmitBtn;
    /**
     * 日期框
     */
    private DatePickerDialog datePickerDialog;
    /**
     * 日期tag
     */
    public static final String DATEPICKER_TAG = "datepicker";

    private final Handler mHandler = new Handler();

    private GalleryView mGalleryView;

    private RecyclerView mRecyclerView;

    private ParticipantsRecycleAdapter mAdapter;

    private List<ItemInfo> mParts = new ArrayList<ItemInfo>();

    private ItemInfo mExcutorInfo;

    private TextView mPartDescription;

    private ImageView mAddPartBtn;

    private Calendar mCalendar;

    /**
     * 是否点击的开始时间
     */
    private boolean mStartClicked = true;

    private ScheduleDate mStartDate;

    private ScheduleDate mEndDate;

    private ServiceUtils.ServiceToken mToken;


    private static final int MAX_IMAGE_SIZE = 320 * 480 / 2;


    /**
     * 存储上传文件的ID
     */
    private List<String> mFileIds = new ArrayList<>();

    private String mTheme;

    private String mContent;

    private String mSTime; // 开始时间

    private String mETime; // 结束时间

    private int mCount; // 需要上传的图片数量

    /**************************** 修改日程 ***************************/

    /**
     * 需要修改的日程
     */
    private ScheduleDetailsInfo mOldSchedule;

    /** 保存原有的图片ID */
    private List<String> mImageIds = new ArrayList<String>();

    /** 保存已经删除的图片ID */
    private List<String> mDeleteIds = new ArrayList<String>();

    private RelativeLayout mExcutorParent;

    public static final int REQUEST_CODE_ADD = 0;

    public static final int REQUEST_CODE_MODIFY = 1;

    private TimePickerDialog mTimeDialog;

    /** 原有的参与人,用;隔开 */
    private String mOldParts;

    /** 是否上传失败 */
    private boolean mPostFailed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.schedule_add_layout);
        mOldSchedule = getIntent().getParcelableExtra("schedule");
        initView();
        initData();
        initEvent();
        mToken = ServiceUtils.bindToService(this, this);
        if (savedInstanceState != null) {
            setLastData(savedInstanceState);
        }
    }

    private void setScheduleData(ScheduleDetailsInfo info) {
        long start = Long.valueOf(info.getStartTime());
        long end = Long.valueOf(info.getEndTime());
        mThemeEdit.setText(info.getTheme());
        mThemeEdit.setSelection(info.getTheme().length());
        mContentEdit.setText(info.getContent());
        mStartTime.setText(HcUtil.getDate("yyyy.MM.dd HH:mm", start));
        mStartTime.setTextColor(getResources().getColor(R.color.schedule_add_content_text));
        mEndTime.setText(HcUtil.getDate("yyyy.MM.dd HH:mm", end));
        mEndTime.setTextColor(getResources().getColor(R.color.schedule_add_content_text));
        String parts = info.getTaskMembers();
        if (!TextUtils.isEmpty(parts)) {
            String[] users = parts.split(";");
            ItemInfo item;
            String[] part;
            StringBuilder builder = new StringBuilder();
            for (String user : users) {
            	item = new StaffInfo();
                part = user.split("_");
                builder.append(part[0] + ";");
                item.setUserId(part[0]);
                item.setItemValue(part[1]);
                mParts.add(item);
            }
            if (builder.length() > 0) {
                builder.deleteCharAt(builder.length() - 1);
            }
            mOldParts = builder.toString();

            if (mPartDescription.getVisibility() != View.GONE) {
                mPartDescription.setVisibility(View.GONE);
            }
            if (mRecyclerView.getVisibility() != View.VISIBLE) {
                mRecyclerView.setVisibility(View.VISIBLE);
            }
            mAdapter.notifyDataSetChanged();
        }

        String annexList = info.getAnnexList();
        HcLog.D(TAG + " #setScheduleData annexList = "+annexList + " parts = "+parts);
        if (!TextUtils.isEmpty(annexList)) {
            String[] anneies = annexList.split(";");
            String id;
            String name;
            String[] annex;
            GalleryView.GalleryItemInfo item;
            for (String file : anneies) {
                annex = file.split(":");
                id = annex[0];
                name = annex[1];
                if (ScheduleUtils.isImage(name)) {
                    item = new GalleryView.GalleryItemInfo();
                    item.mSeleted = true;
                    item.mUri = ScheduleUtils.getUrl(id);
                    mGalleryView.addItem(item);
                    mImageIds.add(id);
                }
            }
        }

        mCalendar.setTimeInMillis(start);
        mStartDate = new ScheduleDate();
        initDate(mStartDate);
        mCalendar.setTimeInMillis(end);
        mEndDate = new ScheduleDate();
        initDate(mEndDate);
    }

    private void initDate(ScheduleDate date) {
        date.year = mCalendar.get(Calendar.YEAR);
        date.month = mCalendar.get(Calendar.MONTH);
        date.day = mCalendar.get(Calendar.DAY_OF_MONTH);
        date.hour = mCalendar.get(Calendar.HOUR_OF_DAY);
        date.minute = mCalendar.get(Calendar.MINUTE);
    }

    private void initData() {
        mCalendar = Calendar.getInstance();
        mAdapter = new ParticipantsRecycleAdapter();
        mRecyclerView.setAdapter(mAdapter);

        if (mOldSchedule != null) {
            mTopBarView.setTitle("修改日程");
            mExcutorParent.setVisibility(View.GONE);
            mExcutorDivider.setVisibility(View.GONE);
            String excutorString = mOldSchedule.getExecutor();
            if (!TextUtils.isEmpty(excutorString)) {
                String[] excutor = excutorString.split("_");
                if (excutor.length == 2) {
                    mExcutorInfo = new StaffInfo();
                    mExcutorInfo.setItemValue(excutor[1]);
                    mExcutorInfo.setUserId(excutor[0]);
                }
            }
            setScheduleData(mOldSchedule);
        } else {
            mTopBarView.setTitle("新增日程");
            //获取执行人
            mExcutorInfo = new StaffInfo();
            mExcutorInfo.setItemValue(SettingHelper.getName(this));
            mExcutorInfo.setUserId(SettingHelper.getUserId(this));
            mExcutorText.setText(mExcutorInfo.getItemValue());
        }

    }

    private void initEvent() {
        mExcutorText.setOnClickListener(this);
        mStartTime.setOnClickListener(this);
        mEndTime.setOnClickListener(this);
        mScheduleAddSubmitBtn.setOnClickListener(this);
        mPartDescription.setOnClickListener(this);
        mAddPartBtn.setOnClickListener(this);
        findViewById(R.id.schedule_add_start_time_img).setOnClickListener(this);
        findViewById(R.id.schedule_add_end_time_img).setOnClickListener(this);
    }

    private void initView() {

        mTopBarView = (TopBarView) findViewById(R.id.schedule_add_top_bar);

        mThemeEdit = (EditText) findViewById(R.id.schedule_add_edit);

        mContentEdit = (EditText) findViewById(R.id.schedule_add_content_edit);

        mExcutorParent = (RelativeLayout) findViewById(R.id.schedule_add_excutor_include);
        mExcutorText = (TextView) findViewById(R.id.schedule_add_excutor_edit);
        mExcutorDivider = findViewById(R.id.schedule_add_excutor_divider);

        mStartTime = (TextView) findViewById(R.id.schedule_add_start_time_edit);

        mEndTime = (TextView) findViewById(R.id.schedule_add_end_time_edit);

        mScheduleAddSubmitBtn = (TextView) findViewById(R.id.schedule_add_submit_btn);


        mGalleryView = (GalleryView) findViewById(R.id.schedule_add_galleryview);

        mRecyclerView = (RecyclerView) findViewById(R.id.schedule_add_part_recylerview);

        mRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        mRecyclerView.addItemDecoration(new RecyclerViewDivider(this, LinearLayoutManager.HORIZONTAL, R.drawable.schedule_recyclerview_divider, false));

        mPartDescription = (TextView) findViewById(R.id.schedule_add_part_description);
        mAddPartBtn = (ImageView) findViewById(R.id.schedule_add_part_img);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.schedule_add_excutor_edit) {
        	Intent intent = new Intent(this, ScheduleChooseActivity.class);
            startActivityForResult(intent, HcChooseHomeView.REQUEST_CODE);
        } else if (v.getId() == R.id.schedule_add_part_description) {
            //跳转到参与人选择页面
            Intent intent = new Intent(this, ScheduleChooseActivity.class);
            intent.putExtra(ScheduleChooseActivity.SELECT, true);
            startActivityForResult(intent, HcChooseHomeView.REQUEST_CODE);
        } else if (v.getId() == R.id.schedule_add_start_time_edit ||
                v.getId() == R.id.schedule_add_start_time_img) {
            mCalendar.setTimeInMillis(System.currentTimeMillis());
            if (datePickerDialog == null) {
                datePickerDialog = new DatePickerDialog();
            }
            datePickerDialog.initialize(this, mCalendar.get(Calendar.YEAR), mCalendar.get(Calendar.MONTH), mCalendar.get(Calendar.DAY_OF_MONTH), false);
            mStartClicked = true;
            //弹出日历框
            datePickerDialog.setVibrate(false);
            datePickerDialog.setYearRange(1985, 2028);
            datePickerDialog.setCloseOnSingleTapDay(false);
            datePickerDialog.show(getFragmentManager(), DATEPICKER_TAG);
        } else if (v.getId() == R.id.schedule_add_end_time_edit ||
                v.getId() == R.id.schedule_add_end_time_img) {
            if (mStartDate == null) {
                HcUtil.showToast(this, "请先选择开始时间!");
                return;
            }
            mCalendar.setTimeInMillis(System.currentTimeMillis());
            if (datePickerDialog == null) {
                datePickerDialog = new DatePickerDialog();
            }
            datePickerDialog.initialize(this, mCalendar.get(Calendar.YEAR), mCalendar.get(Calendar.MONTH), mCalendar.get(Calendar.DAY_OF_MONTH), false);
            mStartClicked = false;
            //弹出日历框
            datePickerDialog.setVibrate(false);
            datePickerDialog.setYearRange(1985, 2028);
            datePickerDialog.setCloseOnSingleTapDay(false);
            datePickerDialog.show(getFragmentManager(), DATEPICKER_TAG);
        } else if (v.getId() == R.id.schedule_add_submit_btn) {
            String themeEdit = mThemeEdit.getText().toString();
            if (TextUtils.isEmpty(themeEdit)) {
                HcUtil.showToast(this, "日程主题不能为空!");
                return;
            }
            mTheme = themeEdit;
            String contentEdit = mContentEdit.getText().toString();
            if (TextUtils.isEmpty(contentEdit)) {
                HcUtil.showToast(this, "日程内容不能为空!");
                return;
            }
            mContent = contentEdit;
            if (mStartDate == null) {
                HcUtil.showToast(this, "请选择开始时间!");
                return;
            }
            if (mEndDate == null) {
                HcUtil.showToast(this, "请选择结束时间!");
                return;
            }
            long current = System.currentTimeMillis();
            mCalendar.set(mStartDate.year, mStartDate.month, mStartDate.day, mStartDate.hour, mStartDate.minute);
            long start = mCalendar.getTimeInMillis();
            mCalendar.set(mEndDate.year, mEndDate.month, mEndDate.day, mEndDate.hour, mEndDate.minute);
            long end = mCalendar.getTimeInMillis();
            if (mOldSchedule == null) {
                if (start < current) {
                    HcUtil.showToast(this, "开始时间不能早于当前时间!");
                    return;
                }
            }

            if (end < current) {
                HcUtil.showToast(this, "结束时间不能早于当前时间!");
                return;
            }
            if (start >= end) {
                HcUtil.showToast(this, "结束时间不能早于开始时间!");
                return;
            }

            mSTime = "" + start;

            mETime = "" + end;

            List<GalleryView.GalleryItemInfo> infos = mGalleryView.getImages();

            mPostFailed = false;

            if (mOldSchedule != null) {
                if (mImageIds.isEmpty()) {
                    HcDialog.showProgressDialog(this, R.string.dialog_title_post_data);
                    if (infos.isEmpty()) {
                        updateSchedule();
                    } else {
                        mCount = infos.size();
                        for (GalleryView.GalleryItemInfo info : infos) {
                            uploadFile(info);
                        }
                        infos.clear();
                    }
                } else {

                    Iterator<GalleryView.GalleryItemInfo> iterator;
                    mDeleteIds.clear();
                    boolean delete = true;
                    for (String annex : mImageIds) {
                        delete = true;
                        iterator = infos.iterator();
                        while (iterator.hasNext()) {
                            if (iterator.next().mUri.equals(ScheduleUtils.getUrl(annex))) {
                                // 找到了
                                iterator.remove();
                                delete = false;
                                break;
                            }
                        }
                        // 肯定被删除了
                        if (delete)
                            mDeleteIds.add(annex);
                    }
                    HcDialog.showProgressDialog(this, R.string.dialog_title_post_data);
                    if (infos.isEmpty()) {
                        updateSchedule();
                    } else {
                        // 新增的
                        mCount = infos.size();
                        for (GalleryView.GalleryItemInfo info : infos) {
                            uploadFile(info);
                        }
                        infos.clear();
                    }

                }

            } else {
                HcDialog.showProgressDialog(this, R.string.dialog_title_post_data);
                if (infos.isEmpty()) {
                    sendSchedule();
                } else {
                    mCount = infos.size();
                    for (GalleryView.GalleryItemInfo info : infos) {
                        uploadFile(info);
                    }
                    infos.clear();
                }
            }

        } else if (v.getId() == R.id.schedule_add_part_img) {
            //跳转到参与人选择页面
            Intent intent = new Intent(this, ScheduleChooseActivity.class);
            intent.putExtra(ScheduleChooseActivity.SELECT, true);
            startActivityForResult(intent, HcChooseHomeView.REQUEST_CODE);
        }
    }

    public class ScheduleAddRequest extends AbstractHttpRequest {

        public ScheduleAddRequest() {
            super(1);
        }

        @Override
        public String getRequestMethod() {
            return "createSchedule";
        }

        @Override
        public String getParameterUrl() {
            return "";
        }
    }

    private class ScheduleAddResponse extends AbstractHttpResponse {

        private static final String TAG = ScheduleAddActivity.TAG + "$ScheduleAddResponse";

        @Override
        public void onSuccess(Object data, RequestCategory request) {
            HcLog.D(TAG + " #onSuccess data = " + data + " request = " + request);
            // 清空数据
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    mThemeEdit.setText("");
                    HcDialog.deleteProgressDialog();
                    //添加，跳转ScheduleDetailActivity
                    Intent intent = new Intent();
                    intent.putExtra("failed", mPostFailed);
                    setResult(Activity.RESULT_OK, intent);
                    finish();
                }
            });


        }

        @Override
        public String getTag() {
            return TAG;
        }

        @Override
        public void onAccountExcluded(String data, String msg, RequestCategory category) {
            HcDialog.deleteProgressDialog();
            HcUtil.reLogining(data, ScheduleAddActivity.this, msg);
        }

        @Override
        public void notifyRequestMd5Url(RequestCategory request, String md5Url) {

        }
    }


    @Override
    public void onDateSet(DatePickerDialog datePickerDialog, int year, int month, int day) {
        HcLog.D(TAG + " #onDateSet year = "+year + " month = "+month + " day ="+day);
        mCalendar.setTimeInMillis(System.currentTimeMillis());
        int currentYear = mCalendar.get(Calendar.YEAR);
        int currentMonth = mCalendar.get(Calendar.MONTH); // 1月是0
        int currentDay = mCalendar.get(Calendar.DAY_OF_MONTH);

        if (currentYear > year || (currentYear == year && currentMonth > month) || (currentMonth == month && currentDay > day)) {
            HcUtil.showToast(this, "选择的日期不能早于当前时间!");
            return;
        }
        if (mStartClicked) {
            ScheduleDate date = new ScheduleDate();
            date.day = day;
            date.month = month;
            date.year = year;
            mStartDate = date;
        } else {
            if (mStartDate == null) {
                HcUtil.showToast(this, "请先选择开始时间!");
                return;
            }
            currentDay = mStartDate.day;
            currentMonth = mStartDate.month;
            currentYear = mStartDate.year;
            if (currentYear > year || (currentYear == year && currentMonth > month) || (currentMonth == month && currentDay > day)) {
                HcUtil.showToast(this, "结束的日期不能早于开始日期!");
                return;
            }
            ScheduleDate date = new ScheduleDate();
            date.day = day;
            date.month = month;
            date.year = year;
            mEndDate = date;

        }
        showTimePickerDialog();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case HcChooseHomeView.REQUEST_CODE:
                    if (data != null && data.getExtras() != null) {
                        ItemInfo info = data.getParcelableExtra(HcChooseHomeView.ITEM_KEY);
                        if (info != null) { // 说明是选择执行人
                            String excutor = info.getUserId();
//                            if (TextUtils.isEmpty(excutor)) { // 数据出错,已经在ScheduleChooseActivity里面处理了.
//                                HcLog.D(TAG + " #onActivityResult 执行人" + info.getItemValue() + "没有userId！");
//                                return;
//                            }
                            for (ItemInfo part : mParts) {
                            	if (excutor.equals(part.getUserId())) { // 说明选择的执行人已经在参与人里面了
                                    HcUtil.showToast(this, info.getItemValue() + "已经在参与人里面了!");
                                    return;
                                }
                            }
                            mExcutorInfo = info;
                            mExcutorText.setText(mExcutorInfo.getItemValue());
                        } else { // 说明是选择参与人
                            ArrayList<ItemInfo> infos = data.getParcelableArrayListExtra(HcChooseHomeView.CLICK_KEY);
                            if (infos != null) {
                                if (mPartDescription.getVisibility() != View.GONE) {
                                    mPartDescription.setVisibility(View.GONE);
                                }
                                if (mRecyclerView.getVisibility() != View.VISIBLE) {
                                    mRecyclerView.setVisibility(View.VISIBLE);
                                }
                                if (mExcutorInfo != null) { // 修改的时候现在为空
                                    int size = infos.size();
                                    ItemInfo part;
                                    for (int i = size - 1; i >= 0; i--) {
                                        part = infos.get(i);
//                                    if (TextUtils.isEmpty(part.getUserId())) { // 已经在ScheduleChooseActivity里面处理了.
//                                        HcLog.D(TAG + " #onActivityResult 参与人" + part.getItemValue() + "没有userId！");
//                                        infos.remove(i);
//                                        size --;
//                                        continue;
//                                    }
                                        if (mExcutorInfo.getUserId().equals(part.getUserId())) {
                                            infos.remove(i);
                                            size --;
                                            HcUtil.showToast(this, part.getItemValue() + "已经是执行人了!");
                                            break;
                                        }
                                    }
                                }
                                if (!infos.isEmpty()) {
                                    mParts.clear();
                                    mParts.addAll(infos);
                                    infos.clear();
                                    if (mAdapter != null) {
                                        mAdapter.notifyDataSetChanged();
                                    }
                                }

                            }
                        }
                    }
                    break;
                case HcUtil.REQUEST_CODE_FROM_CAMERA:
                    /**
                    if (data != null && data.getExtras() != null) {
                        ImageItemInfo info = data.getParcelableExtra("image");
                        if (info != null) {
                            uploadFile(info);
                        } else {
                            ArrayList<ImageItemInfo> infos = data.getParcelableArrayListExtra("images");
                            if (infos != null) {
                                for (ImageItemInfo item : infos) {
                                    uploadFile(item);
                                }
                            }
                        }
                    }*/
                    if (mGalleryView != null) {
                        mGalleryView.onActivityResult(requestCode, resultCode, data);
                    }

                    break;
                default:
                    break;

            }
        }
    }


    @Override
    protected void onDestroy() {
        ServiceUtils.unbindFromService(mToken);
        super.onDestroy();
    }

    private class ParticipantsViewHolder extends RecyclerView.ViewHolder {

        private TextView mName;

        private ImageView mDelete;

        public ParticipantsViewHolder(View itemView) {
            super(itemView);

            mName = (TextView) itemView.findViewById(R.id.participant_item_name);
            mDelete = (ImageView) itemView.findViewById(R.id.participant_item_delete_btn);
        }
    }


    private class ParticipantsRecycleAdapter extends RecyclerView.Adapter<ParticipantsViewHolder> {

        @Override
        public int getItemCount() {
            return mParts.size();
        }

        @Override
        public ParticipantsViewHolder onCreateViewHolder(ViewGroup viewGroup, int position) {
            return new ParticipantsViewHolder(LayoutInflater.from(ScheduleAddActivity.this).inflate(R.layout.schedule_participant_item, viewGroup, false));
        }

        @Override
        public void onBindViewHolder(ParticipantsViewHolder viewHolder, int position) {
            final ItemInfo info = mParts.get(position);
            viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //跳转到参与人选择页面
                    Intent intent = new Intent(ScheduleAddActivity.this, ScheduleChooseActivity.class);
                    intent.putExtra(ScheduleChooseActivity.SELECT, true);
                    startActivityForResult(intent, HcChooseHomeView.REQUEST_CODE);
                }
            });
            viewHolder.mDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mParts.remove(info);
                    if (mAdapter != null) {
                        mAdapter.notifyDataSetChanged();
                    }
                    if (mParts.isEmpty()) {
                        if (mPartDescription.getVisibility() != View.VISIBLE) {
                            mPartDescription.setVisibility(View.VISIBLE);
                        }
                        if (mRecyclerView.getVisibility() != View.GONE) {
                            mRecyclerView.setVisibility(View.GONE);
                        }
                    }
                }
            });
            viewHolder.mName.setText(info.getItemValue());
        }
    }

    private void showTimePickerDialog() {
        mCalendar.setTimeInMillis(System.currentTimeMillis());
        if (mTimeDialog == null)
            mTimeDialog = new TimePickerDialog(this, R.style.Schedule_TimePickerDialog, mTimeListener, mCalendar.get(Calendar.HOUR_OF_DAY), mCalendar.get(Calendar.MINUTE), true);
        mTimeDialog.updateTime(mCalendar.get(Calendar.HOUR_OF_DAY), mCalendar.get(Calendar.MINUTE));
        mTimeDialog.show();
    }

    private TimePickerDialog.OnTimeSetListener mTimeListener = new TimePickerDialog.OnTimeSetListener() {
        @Override
        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            HcLog.D(TAG + " #onTimeSet hourOfDay = "+hourOfDay + " minute = "+minute);
            long currentTime = System.currentTimeMillis();
            int year = mStartClicked ? mStartDate.year : mEndDate.year;
            int month = mStartClicked ? mStartDate.month : mEndDate.month;
            int day = mStartClicked ? mStartDate.day : mEndDate.day;
            mCalendar.set(year, month, day, hourOfDay, minute);
            long time = mCalendar.getTimeInMillis();
            mCalendar.setTimeInMillis(System.currentTimeMillis());

            if (currentTime > time) {
                HcUtil.showToast(ScheduleAddActivity.this, "选择的时间不能早于当前时间!");
                return;
            }
            if (mStartClicked) {
                mStartDate.hour = hourOfDay;
                mStartDate.minute = minute;
                setDate(mStartDate);
            } else {
                if (mStartDate != null) {
                    year = mStartDate.year;
                    month = mStartDate.month;
                    day = mStartDate.day;
                    int hour = mStartDate.hour;
                    int min = mStartDate.minute;
                    mCalendar.set(year, month, day, hour, min);
                    currentTime = mCalendar.getTimeInMillis();
                    if (currentTime >= time) {
                        HcUtil.showToast(ScheduleAddActivity.this, "结束的时间不能早于开始时间!");
                        return;
                    }

                    mEndDate.hour = hourOfDay;
                    mEndDate.minute = minute;
                    setDate(mEndDate);
                }
            }

        }
    };

    private static class ScheduleDate{
        int year;
        int month;
        int day;
        int hour;
        int minute;
    }

    private void setDate(ScheduleDate date) {
        if (mStartClicked) {
            mStartTime.setText(date.year + "." + (date.month + 1 ) + "." + date.day + " " + date.hour + ":" + date.minute);
            mStartTime.setTextColor(getResources().getColor(R.color.schedule_add_content_text));
        } else {
            mEndTime.setText(date.year + "." + (date.month + 1) + "." + date.day + " " + date.hour + ":" + date.minute);
            mEndTime.setTextColor(getResources().getColor(R.color.schedule_add_content_text));
        }
    }

    private StringBuilder mBuilder;

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        ServiceUtils.addServiceCallback(new HcDownloadService.ServiceCallback() {
            @Override
            public void serviceCallback(FileColumn fileColumn) {
                HcLog.D(TAG + " #onServiceConnected#serviceCallback fileColumn id = "+fileColumn.getFileid() + " success = "+fileColumn.getSuccess());

                if (ScheduleUtils.SERVICE_SOURCE == fileColumn.getSource()) {
                    if (mFileIds.contains(fileColumn.getFileid())) {
                        if(fileColumn.getSuccess() == 1) {
                            mFileIds.remove(fileColumn.getFileid());
                            mCount --;
                            if (mBuilder == null) {
                                mBuilder = new StringBuilder();
                            }
                            String filePath = fileColumn.getPath();

                            mBuilder.append(fileColumn.getUrl() + "," + filePath.substring(filePath.lastIndexOf("/") + 1, filePath.length()) + ";");
                            HcLog.D(TAG + " #onServiceConnected#serviceCallback file path = "+filePath + " count = "+mCount + " file disk id = "+fileColumn.getUrl() + " builder = "+mBuilder.toString());
                            if (mCount == 0) {
                                if (mOldSchedule == null) {
                                    // 发布日程
                                    sendSchedule();
                                } else {
                                    // 修改日程
                                    updateSchedule();
                                }

                            }
                        } else if ("3".equals(fileColumn.getState())) { //
                            mFileIds.remove(fileColumn.getFileid());
                            mCount --;
                            mPostFailed = true;
                            if (mOldSchedule == null) {
                                // 发布日程
                                sendSchedule();
                            } else {
                                // 修改日程
                                updateSchedule();
                            }
                            HcLog.D(TAG + " #onServiceConnected#serviceCallback 出错了,文件上传失败! fileColumn id = "+fileColumn.getFileid());
                        } else {
                            HcLog.D(TAG + " #onServiceConnected#serviceCallback 文件上传未结束, fileColumn id = "+fileColumn.getFileid());
                        }
                    } else {
                        HcLog.D(TAG + " #onServiceConnected#serviceCallback 出错了,找不到fileId, fileColumn id = "+fileColumn.getFileid());
                    }
                }

            }
        }, ScheduleUtils.SERVICE_SOURCE);
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {

    }

    private void uploadFile(ImageItemInfo info) {
        File file = new File(HcApplication.getImagePhotoPath(), System.currentTimeMillis() + ".jpg");
        Bitmap bitmap = null;
        OutputStream stream = null;
        try {
            bitmap = HcUtil.makeBitmap(info.getImagePath(), 320, MAX_IMAGE_SIZE);
            HcLog.D(TAG + " #uploadFile bitmap size = " + bitmap.getByteCount() + " width = " + bitmap.getWidth() + " height = " + bitmap.getHeight());
            stream = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
            stream.flush();
        } catch (Exception e) {
            // TODO: handle exception
            HcLog.D(TAG + " #uploadFile stream error = " + e);
            file = null;
        } finally {
            try {
                if (stream != null)
                    stream.close();
            } catch (Exception e) {
                // do nothing
            }

            if (bitmap != null) {
                bitmap.recycle();
                bitmap = null;
            }
        }
        if (file != null && file.exists()) {
            String MD5 = DownloadUtil.getFileMD5(file, Long.valueOf(file.length()));
            FileColumn fileColumn = ServiceUtils.createFile(info.getImagePath(), ScheduleUtils.NETDISK_DIRECTORY, MD5, ScheduleUtils.SERVICE_SOURCE);
            if (fileColumn != null) {
                ServiceUtils.uploadFile(fileColumn);
            }
        }
    }

    private void sendSchedule() {

        String parts = null;

        String images = null;

        if (!mParts.isEmpty()) {
            StringBuilder builder = new StringBuilder();
            for (ItemInfo info : mParts) {
                builder.append(info.getUserId() + ";");
            }
            builder.deleteCharAt(builder.length() - 1);
            parts = builder.toString();
        }

        if (mBuilder != null && mBuilder.length() > 0) {
            mBuilder.deleteCharAt(mBuilder.length() - 1);
            images = mBuilder.toString();
            mBuilder.delete(0, mBuilder.length());
        }
        HcLog.D(TAG + " #sendSchedule parts = "+parts + " images = "+images);
        String url = HcUtil.getScheme() + HttpRequestQueue.BASE_URL + "createSchedule";
        HttpPost scheduleAdd = new HttpPost(url);
        MultipartEntityBuilder builder = MultipartEntityBuilder.create();
        builder.setCharset(Charset.forName(HTTP.UTF_8));//设置请求的编码格式
        builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
        builder.addTextBody("theme", mTheme, ContentType.create(HTTP.PLAIN_TEXT_TYPE, HTTP.UTF_8));
        builder.addTextBody("content", mContent, ContentType.create(HTTP.PLAIN_TEXT_TYPE, HTTP.UTF_8));
        builder.addTextBody("executeId", mExcutorInfo.getUserId(), ContentType.create(HTTP.PLAIN_TEXT_TYPE, HTTP.UTF_8));
        if (parts != null)
            builder.addTextBody("participantId", parts, ContentType.create(HTTP.PLAIN_TEXT_TYPE, HTTP.UTF_8));
        builder.addTextBody("startTime", mSTime, ContentType.create(HTTP.PLAIN_TEXT_TYPE, HTTP.UTF_8));
        builder.addTextBody("endTime", mETime, ContentType.create(HTTP.PLAIN_TEXT_TYPE, HTTP.UTF_8));
        if (images != null)
            builder.addTextBody("file", images, ContentType.create(HTTP.PLAIN_TEXT_TYPE, HTTP.UTF_8));

        scheduleAdd.setEntity(builder.build());
        ScheduleAddRequest request = new ScheduleAddRequest();
        ScheduleAddResponse response = new ScheduleAddResponse();
//        HcDialog.showProgressDialog(this, R.string.dialog_title_post_data);
        request.sendRequestCommand(url, scheduleAdd, RequestCategory.NONE, response, false);
    }

    private void uploadFile(GalleryView.GalleryItemInfo info) {
        File file = new File(HcApplication.getImagePhotoPath(), System.currentTimeMillis() + ".jpg");
        Bitmap bitmap = null;
        OutputStream stream = null;
        try {
            bitmap = HcUtil.makeBitmap(info.mUri.replace("file://", ""), 320, MAX_IMAGE_SIZE);
            HcLog.D(TAG + " #uploadFile bitmap size = " + bitmap.getByteCount() + " width = " + bitmap.getWidth() + " height = " + bitmap.getHeight());
            stream = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
            stream.flush();
        } catch (Exception e) {
            // TODO: handle exception
            HcLog.D(TAG + " #uploadFile stream error = " + e);
            file = null;
        } finally {
            try {
                if (stream != null)
                    stream.close();
            } catch (Exception e) {
                // do nothing
            }

            if (bitmap != null) {
                bitmap.recycle();
                bitmap = null;
            }
        }
        if (file != null && file.exists()) {
            String MD5 = DownloadUtil.getFileMD5(file, Long.valueOf(file.length()));
            FileColumn fileColumn = ServiceUtils.createFile(/*info.mUri.replace("file://", "")*/file.getAbsolutePath(), ScheduleUtils.NETDISK_DIRECTORY, MD5, ScheduleUtils.SERVICE_SOURCE);
            if (fileColumn != null) {
                HcLog.D(TAG + " #uploadFile file id = "+fileColumn.getFileid());
                mFileIds.add(fileColumn.getFileid());
                ServiceUtils.uploadFile(fileColumn);
            }
        }
    }

    private void updateSchedule() {
        String theme = null;
        String content = null;
        String parts = null;
        String startTime = null;
        String endTime = null;
        String images = null;
        String delete = null;
        if (!mOldSchedule.getTheme().equals(mTheme)) {
            theme = mTheme;
        }

        if (!mOldSchedule.getContent().equals(mContent)) {
            content = mContent;
        }

        if (!mParts.isEmpty()) {
            StringBuilder builder = new StringBuilder();
            for (ItemInfo info : mParts) {
                builder.append(info.getUserId() + ";");
            }
            builder.deleteCharAt(builder.length() - 1);
            parts = builder.toString();
        } else {
            if (TextUtils.isEmpty(mOldSchedule.getTaskMembers())) {
                parts = null;
            } else {
                parts = "";
            }

        }

        if (!mDeleteIds.isEmpty()) {
            StringBuilder builder = new StringBuilder();
            for (String image : mDeleteIds) {
                builder.append(image + ";");
            }
            builder.deleteCharAt(builder.length() - 1);
            delete = builder.toString();
        }

        if (parts != null && parts.equals(mOldParts)) {
            parts = null;
        }

        if (!mOldSchedule.getStartTime().equals(mSTime)) {
            startTime = mSTime;
        }

        if (!mOldSchedule.getEndTime().equals(mETime)) {
            endTime = mETime;
        }

        if (mBuilder != null && mBuilder.length() > 0) {
            mBuilder.deleteCharAt(mBuilder.length() - 1);
            images = mBuilder.toString();
            mBuilder.delete(0, mBuilder.length());
        }

        HcLog.D(TAG + " #updateSchedule theme = "+theme + " content ="+content + " parts = "+parts
            + " startTime ="+startTime + " endTime = "+endTime + " delete images = "+delete + " add images = " +images);

        if (theme == null && content == null && parts == null &&
                startTime == null && endTime == null && images == null && delete == null) {
            HcDialog.deleteProgressDialog();
            if (mPostFailed) {
                HcUtil.showToast(this, "上传图片失败!");
            } else {
                HcUtil.showToast(this, "未做出修改!");
            }

            return;
        }


        String url = HcUtil.getScheme() + HttpRequestQueue.BASE_URL + "editSchedule";
        HttpPost scheduleModify = new HttpPost(url);
        MultipartEntityBuilder builder = MultipartEntityBuilder.create();
        builder.setCharset(Charset.forName(HTTP.UTF_8));//设置请求的编码格式
        builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
        builder.addTextBody("scheduleId", mOldSchedule.getId(), ContentType.create(HTTP.PLAIN_TEXT_TYPE, HTTP.UTF_8));
        if (theme != null)
            builder.addTextBody("theme", mTheme, ContentType.create(HTTP.PLAIN_TEXT_TYPE, HTTP.UTF_8));
        if (content != null)
            builder.addTextBody("content", mContent, ContentType.create(HTTP.PLAIN_TEXT_TYPE, HTTP.UTF_8));
        if (parts != null)
            builder.addTextBody("participantId", parts, ContentType.create(HTTP.PLAIN_TEXT_TYPE, HTTP.UTF_8));
//        if (startTime != null)
        // 必传字段
            builder.addTextBody("startTime", mSTime, ContentType.create(HTTP.PLAIN_TEXT_TYPE, HTTP.UTF_8));
        if (endTime != null)
            builder.addTextBody("endTime", mETime, ContentType.create(HTTP.PLAIN_TEXT_TYPE, HTTP.UTF_8));
        if (images != null)
            builder.addTextBody("addFileList", images, ContentType.create(HTTP.PLAIN_TEXT_TYPE, HTTP.UTF_8));
        if (delete != null)
            builder.addTextBody("deleteFileIdList", delete, ContentType.create(HTTP.PLAIN_TEXT_TYPE, HTTP.UTF_8));
        builder.addTextBody("sts", "Y", ContentType.create(HTTP.PLAIN_TEXT_TYPE, HTTP.UTF_8));
        scheduleModify.setEntity(builder.build());
        ScheduleModifyRequest request = new ScheduleModifyRequest();
        ScheduleModifyResponse response = new ScheduleModifyResponse();
//        HcDialog.showProgressDialog(this, R.string.dialog_title_post_data);
        request.sendRequestCommand(url, scheduleModify, RequestCategory.NONE, response, false);
    }

    private class ScheduleModifyRequest extends AbstractHttpRequest {

        public ScheduleModifyRequest() {
            super();
        }

        @Override
        public String getParameterUrl() {
            return "";
        }

        @Override
        public String getRequestMethod() {
            return "editSchedule";
        }
    }

    private class ScheduleModifyResponse extends AbstractHttpResponse {

        private static final String TAG = ScheduleAddActivity.TAG + "$ScheduleModifyResponse";

        @Override
        public String getTag() {
            return TAG;
        }

        @Override
        public void onSuccess(Object data, RequestCategory request) {
            HcLog.D(TAG + " #onSuccess data = " + data + " request = " + request);
            HcDialog.deleteProgressDialog();
            //修改,跳转ScheduleDetailActivity
            Intent intent = new Intent();
            intent.putExtra("failed", mPostFailed);
            setResult(Activity.RESULT_OK, intent);
            finish();
        }

        @Override
        public void onAccountExcluded(String data, String msg, RequestCategory category) {
            HcDialog.deleteProgressDialog();
            HcUtil.reLogining(data, ScheduleAddActivity.this, msg);
        }

        @Override
        public void notifyRequestMd5Url(RequestCategory request, String md5Url) {

        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        String themeEdit = mThemeEdit.getText().toString();
        if (!TextUtils.isEmpty(themeEdit)) {
            outState.putString("theme", themeEdit);
        }
        String contentEdit = mContentEdit.getText().toString();
        if (!TextUtils.isEmpty(contentEdit)) {
            outState.putString("content", contentEdit);
        }
        if (mStartDate != null) {
            mCalendar.set(mStartDate.year, mStartDate.month, mStartDate.day, mStartDate.hour, mStartDate.minute);
            outState.putLong("start", mCalendar.getTimeInMillis());
        }
        if (mEndDate != null) {
            mCalendar.set(mEndDate.year, mEndDate.month, mEndDate.day, mEndDate.hour, mEndDate.minute);
            outState.putLong("end", mCalendar.getTimeInMillis());
        }

        outState.putParcelable("executor", mExcutorInfo);

        if (!mParts.isEmpty()) {
            outState.putParcelableArrayList("parts", new ArrayList<ItemInfo>(mParts));
        }

        List<GalleryView.GalleryItemInfo> images = mGalleryView.getImages();
        if (!images.isEmpty()) {
            StringBuilder builder = new StringBuilder();
            for (GalleryView.GalleryItemInfo info : images) {
                builder.append(info.mUri + ";");
            }
            builder.deleteCharAt(builder.length() - 1);
            outState.putString("images", builder.toString());
        }
    }

    private void setLastData(Bundle last) {
        mThemeEdit.setText(last.getString("theme", ""));
        mContentEdit.setText(last.getString("content", ""));
        mExcutorInfo = last.getParcelable("executor");
        long start = last.getLong("start", 0);
        long end = last.getLong("end", 0);
        if (start > 0) {
            mStartTime.setText(HcUtil.getDate("yyyy.MM.dd HH:mm", start));
            mStartTime.setTextColor(getResources().getColor(R.color.schedule_add_content_text));
            mCalendar.setTimeInMillis(start);
            if (mStartDate == null)
                mStartDate = new ScheduleDate();
            initDate(mStartDate);
        }
        if (end > 0) {
            mEndTime.setText(HcUtil.getDate("yyyy.MM.dd HH:mm", end));
            mEndTime.setTextColor(getResources().getColor(R.color.schedule_add_content_text));
            mCalendar.setTimeInMillis(start);
            if (mEndDate == null)
                mEndDate = new ScheduleDate();
            initDate(mEndDate);
        }

        ArrayList<ItemInfo> parts = last.getParcelableArrayList("parts");
        if (parts != null && !parts.isEmpty()) {
            mParts.addAll(parts);
            mAdapter.notifyDataSetChanged();
        }

        String images = last.getString("images");
        if (images != null) {
            String[] imageList = images.split(";");
            GalleryView.GalleryItemInfo image;
            for (String url : imageList) {
            	image = new GalleryView.GalleryItemInfo();
                image.mUri = url;
                image.mSeleted = true;
                mGalleryView.addItem(image);
            }
        }

    }
}
