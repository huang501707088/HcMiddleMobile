package com.android.hcframe.intro;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.KeyEvent;

import com.android.hcframe.HcConfig;
import com.android.hcframe.HcUtil;
import com.android.hcframe.R;
import com.android.hcframe.intro.GuideView.SlideEndListener;
import com.android.hcframe.menu.Menu1Activity;
import com.android.hcframe.push.HcAppState;

public class IntroActivity extends AbsGuideActivity {

	/**
	 * @author jrjin
	 * @date 2015-12-14 下午5:19:42
	 * 是否只退出当前的Activity
	 */
	private boolean mFinishOnly = false;
	
	@Override
	public List<SinglePage> buildGuideContent() {
		// prepare the information for our guide
		List<SinglePage> guideContent = new ArrayList<SinglePage>();
		List<HashMap<String, String>> introbgs = HcConfig.getConfig()
				.getIntrobgs();
		for (int i = 0; i < introbgs.size(); i++) {
			SinglePage page = new SinglePage();
			HashMap<String, String> introbg = introbgs.get(i);
			try {
				int bg = R.drawable.class.getField(introbg.get("imageName"))
						.getInt(null);
				page.mCustomFragment = new EntryFragment(bg,
						HcUtil.isEmpty(introbg.get("title")) ? ""
								: introbg.get("title"), HcUtil.isEmpty(introbg
								.get("detailText")) ? ""
								: introbg.get("detailText"));
				guideContent.add(page);
			} catch (Exception e) {
			}

		}
		return guideContent;
	}

	@Override
	public Bitmap dotDefault() {
		return BitmapFactory.decodeResource(getResources(),
				R.drawable.ic_dot_default);
	}

	@Override
	public Bitmap dotSelected() {
		return BitmapFactory.decodeResource(getResources(),
				R.drawable.ic_dot_selected);
	}

	@Override
	public boolean drawDot() {
		return true;
	}

	public void startMainActivity() {
		Intent intent = new Intent(this, Menu1Activity.class);
		startActivity(intent);
		overridePendingTransition(0, 0);
	}

	/**
	 * You need provide an id to the pager. You could define an id in
	 * values/ids.xml and use it.
	 */
	@Override
	public int getPagerId() {
		return R.id.guide_container;
	}

	@Override
	public SlideEndListener getSlideEndListener() {
		return new SlideEndListener() {

			@Override
			public void sildeEnd() {
				entryApp();
			}
		};
	}

	@Override
	public void entryApp() {
		HcAppState.getInstance().removeActivity(this);
		if (!mFinishOnly)
			startMainActivity();
		else {
			if (buildGuideContent().size() == 0) {
				HcUtil.showToast(this, "暂无欢迎页!");
			}
		}
		finish();
	}

	/**
	 * @author jrjin
	 * @date 2015-11-2 下午4:41:21
	 * 点击返回按钮直接进入应用
	 */
	@SuppressWarnings("需要测试")
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		entryApp();
		return true;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		/**
		 * @author jrjin
		 * @date 2015-12-14 下午5:25:12
		 * 从关于页面进入的话不需要跳转到MainActivity
		 */
		Intent intent = getIntent();
		if (intent != null && intent.getExtras() != null) {
			mFinishOnly = intent.getExtras().getBoolean("finishOnly", mFinishOnly);
		}
		super.onCreate(savedInstanceState);
	}

}
