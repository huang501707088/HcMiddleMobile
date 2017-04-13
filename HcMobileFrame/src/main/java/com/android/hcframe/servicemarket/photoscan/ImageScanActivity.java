package com.android.hcframe.servicemarket.photoscan;

import java.util.ArrayList;
import java.util.List;

import com.android.hcframe.HcDialog;
import com.android.hcframe.HcUtil;
import com.android.hcframe.R;
import com.android.hcframe.TopBarView;
import com.android.hcframe.http.HcHttpRequest;
import com.android.hcframe.http.IHttpResponse;
import com.android.hcframe.http.RequestCategory;
import com.android.hcframe.http.ResponseCategory;
import com.android.hcframe.http.ResponseCodeInfo;
import com.android.hcframe.push.HcAppState;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

public class ImageScanActivity extends FragmentActivity implements
        IHttpResponse, OnClickListener {
    private static final String STATE_POSITION = "STATE_POSITION";
    public static final String EXTRA_IMAGE_INDEX = "image_index";
    public static final String EXTRA_IMAGE_URLS = "image_urls";
    public static final String EXTRA_IMAGE_ID = "newsId";

    private HackyViewPager mPager;

    private TextView indicator;
    private TopBarView html_top_bar;
    private TextView horizongtal_desc;
    private String newsId;

    private int pagerPosition;

    private List<PicInfo> pics = new ArrayList<PicInfo>();

    private NewsDetailsInfo mdetails = null;

    private ImageView detail_back;
    private RelativeLayout photo_browser_rl;
    private TextView detail_title;
    private TextView detail_date;
    private TextView detail_indicator;
    private ScrollView details_scroll;
    private LinearLayout details_top;
    private TextView details_description;
    private ImagePagerAdapter mAdapter;
    private boolean isFull = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        HcAppState.getInstance().addActivity(this);
        setContentView(R.layout.image_detail_pager);
        html_top_bar = (TopBarView) findViewById(R.id.html_top_bar);
        html_top_bar.setReturnViewListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                finish();
            }
        });

        html_top_bar.setTitle("鸿程生活");
        horizongtal_desc = (TextView) findViewById(R.id.horizongtal_desc);
        horizongtal_desc.setText(getIntent().getStringExtra("title"));

        pagerPosition = getIntent().getIntExtra(EXTRA_IMAGE_INDEX, 0);
        newsId = getIntent().getStringExtra(EXTRA_IMAGE_ID);

        ArrayList<String> urls = getIntent().getStringArrayListExtra(
                EXTRA_IMAGE_URLS);

        mPager = (HackyViewPager) findViewById(R.id.pager);
        mAdapter = new ImagePagerAdapter(getSupportFragmentManager(), pics);
        mPager.setAdapter(mAdapter);
        indicator = (TextView) findViewById(R.id.indicator);

        CharSequence text = getString(R.string.viewpager_indicator, 1, mPager
                .getAdapter().getCount());
        indicator.setText(text);
        // 更新下标
        mPager.setOnPageChangeListener(new OnPageChangeListener() {

            @Override
            public void onPageScrollStateChanged(int arg0) {
            }

            @Override
            public void onPageScrolled(int arg0, float arg1, int arg2) {
            }

            @Override
            public void onPageSelected(int pos) {
                // CharSequence text = getString(R.string.viewpager_indicator,
                // arg0 + 1, mPager.getAdapter().getCount());
                // indicator.setText(text);
                refreshUI(pos);
            }

        });

        detail_back = (ImageView) findViewById(R.id.detail_back);
        photo_browser_rl = (RelativeLayout) findViewById(R.id.photo_browser_rl);
        detail_title = (TextView) findViewById(R.id.detail_title);
        detail_date = (TextView) findViewById(R.id.detail_date);
        detail_indicator = (TextView) findViewById(R.id.detail_indicator);
        details_scroll = (ScrollView) findViewById(R.id.details_scroll);
        details_description = (TextView) findViewById(R.id.details_description);
        details_top = (LinearLayout) findViewById(R.id.details_top);

        detail_back.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                finish();
            }
        });
        // detail_date.setText(HcUtil.getNowDate(HcUtil.FORMAT_YEAR_MONTH_DAY));
        photo_browser_rl.setOnClickListener(this);

        if (savedInstanceState != null) {
            pagerPosition = savedInstanceState.getInt(STATE_POSITION, 0);
        }

        if (pics.size() > 0) {
            mPager.setCurrentItem(pagerPosition);
        }

        if (mdetails == null) {
            HcHttpRequest.getRequest().sendQueryNewsDetails(newsId, this);
            HcDialog.showProgressDialog(this,
                    R.string.dialog_title_get_data);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putInt(STATE_POSITION, mPager.getCurrentItem());
    }

    private class ImagePagerAdapter extends FragmentStatePagerAdapter {

        public List<PicInfo> fileList;

        public ImagePagerAdapter(FragmentManager fm, List<PicInfo> fileList) {
            super(fm);
            this.fileList = fileList;
        }

        @Override
        public int getCount() {
            return fileList == null ? 0 : fileList.size();
        }

        @Override
        public Fragment getItem(int position) {
            PicInfo pic = fileList.get(position);
            return ImageDetailFragment.newInstance(pic);
        }

    }

    @Override
    public void notify(Object data, RequestCategory request,
                       ResponseCategory category) {
        HcDialog.deleteProgressDialog();
        if (request == RequestCategory.NEWDETAILS) {
            switch (category) {
                case SESSION_TIMEOUT:
                case NETWORK_ERROR:
                    HcUtil.toastTimeOut(this);
                    break;
                case DATA_ERROR:
                    HcUtil.toastDataError(this);
                    break;
                case SYSTEM_ERROR:
                    HcUtil.toastSystemError(this, data);
                    break;
                case SUCCESS:
                    if (data != null && data instanceof NewsDetailsInfo) {
                        mdetails = (NewsDetailsInfo) data;
                        // refreshUI();
                        detail_title.setText(mdetails.getTitle());
                        detail_date.setVisibility(View.GONE);
                        detail_date.setText(mdetails.getDate());
                        CharSequence text = getString(R.string.viewpager_indicator,
                                pagerPosition + 1, mdetails.getPics().size());
                        detail_indicator.setText(text);
                        pics.addAll(mdetails.getPics());
                        mAdapter.notifyDataSetChanged();
                        mPager.setCurrentItem(pagerPosition);
                    }
                    break;
                case REQUEST_FAILED:
                    ResponseCodeInfo info = (ResponseCodeInfo) data;
                    /**
                     * @author zhujb
                     * @date 2016-04-13 下午4:19:07
                     */
                    if (info.getCode() == HcHttpRequest.REQUEST_ACCOUT_EXCLUDED ||
                            info.getCode() == HcHttpRequest.REQUEST_TOKEN_FAILED) {
                        HcUtil.reLogining(info.getBodyData(),this, info.getMsg());
                    } else {
                        HcUtil.showToast(this, info.getMsg());
                    }
                    break;
                default:
                    break;

            }

        }
    }

    private void refreshUI(int pos) {
        CharSequence text = getString(R.string.viewpager_indicator, pos + 1,
                mPager.getAdapter().getCount());
        detail_indicator.setText(text);

        details_description.setText(pics.get(pos).getPicText());
    }

    @Override
    public void notifyRequestMd5Url(RequestCategory request, String md5Url) {

    }

    @Override
    public void onClick(View view) {
        int id = view.getId();

        if (id == R.id.photo_browser_rl) {
            LayoutChange();
        }
    }

    public void LayoutChange() {
        isFull = !isFull;
        if (isFull) {
            details_scroll.setVisibility(View.GONE);
            details_top.setVisibility(View.GONE);
        } else {
            details_scroll.setVisibility(View.VISIBLE);
            details_top.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onDestroy() {
        HcAppState.getInstance().removeActivity(this);
        super.onDestroy();
    }
}
