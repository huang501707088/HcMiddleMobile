package com.android.hcframe.intro;

import com.android.hcframe.R;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

public class EntryFragment extends Fragment {

	private int bg = -1;

	private String title;

	private String details;

	public EntryFragment() {
	}

	public EntryFragment(int bg, String title, String details) {
		this.bg = bg;
		this.title = title;
		this.details = details;
	}

	@Override
	public View onCreateView(LayoutInflater inflater,
			@Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_entry, null);
		ImageView intro_bg = (ImageView) v.findViewById(R.id.intro_bg);
		TextView title_entry = (TextView) v.findViewById(R.id.title_entry);
		TextView details_entry = (TextView) v.findViewById(R.id.details_entry);
		title_entry.setText(title);
		details_entry.setText(details);
		if (bg > 0) {
			intro_bg.setBackgroundResource(bg);
		}
		v.findViewById(R.id.btn_entry).setOnClickListener(
				new OnClickListener() {

					@Override
					public void onClick(View v) {
						IntroActivity activity = (IntroActivity) getActivity();
						activity.entryApp();
					}
				});
		return v;
	}
}
