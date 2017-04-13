package com.android.hcframe.intro;

import java.util.List;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.KeyEvent;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;

import com.android.hcframe.intro.GuideView.SlideEndListener;
import com.android.hcframe.push.HcAppState;

public abstract class AbsGuideActivity extends FragmentActivity {

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        HcAppState.getInstance().addActivity(this);
        List<SinglePage> guideContent = buildGuideContent();

        if (guideContent == null || guideContent.size()==0) {
            entryApp();
            return;
        }
        guideContent.add(new SinglePage());
        // prepare views
        FrameLayout container = new FrameLayout(this);
        ViewPager pager = new ViewPager(this);
        pager.setId(getPagerId());

        container.addView(pager, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));

        setContentView(container);

        FragmentPagerAdapter adapter = new FragmentTabAdapter(this, guideContent);
        pager.setAdapter(adapter);

        GuideView guideView = new GuideView(this, guideContent, drawDot(), dotDefault(), dotSelected());
        guideView.setSlideEndListener(getSlideEndListener());
        pager.setOnPageChangeListener(guideView);

        container.addView(guideView, new LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT));
    }

    abstract public List<SinglePage> buildGuideContent();

    abstract public boolean drawDot();

    abstract public Bitmap dotDefault();

    abstract public Bitmap dotSelected();

    abstract public int getPagerId();
    
    public abstract SlideEndListener getSlideEndListener();
    
    public abstract void entryApp();

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		switch (keyCode) {
		case KeyEvent.KEYCODE_BACK:
			if (keyCode == KeyEvent.KEYCODE_BACK
			&& event.getAction() == KeyEvent.ACTION_DOWN) {
				HcAppState.getInstance().removeActivity(this);
			}
			break;

		default:
			break;
		}
		return super.onKeyDown(keyCode, event);
	}

    @Override
    protected void onDestroy() {
        HcAppState.getInstance().removeActivity(this);
        super.onDestroy();
    }
}
