/*
* @Company 浙 江 鸿 程 计 算 机 系 统 有 限 公 司
* @URL http://www.zjhcsoft.com
* @Address 杭州滨江区伟业路1号
* @Email jinjr@zjhcsoft.com 
* @author jinjr
* @data 2015-12-10 下午12:42:53
*/
package com.android.hcframe.internalservice.contacts;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.SectionIndexer;
import android.widget.TextView;

import com.android.hcframe.HcConfig;
import com.android.hcframe.HcLog;
import com.android.hcframe.HcUtil;
import com.android.hcframe.adapter.HcBaseAdapter;
import com.android.hcframe.contacts.data.ContactsInfo;
import com.android.hcframe.container.ContainerCircleImageView;
import com.android.hcframe.sql.SettingHelper;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;

public class EmployeeAdapter extends HcBaseAdapter<ContactsInfo> implements SectionIndexer {

	/**
	 * @deprecated 替换为显示用户的图片
	 */
	private List<Integer> mColors = new ArrayList<Integer>();

	/**
	 * @deprecated
	 */
	private int[] colors = {Color.parseColor("#78cae8"),Color.parseColor("#63dea3"),
			Color.parseColor("#c97bf1"),Color.parseColor("#f1b533"),
			Color.parseColor("#ff5599"),Color.parseColor("#f96633")};
	/**
	 * @deprecated
	 */
	private int mPreColorIndex = -1;

	private DisplayImageOptions mHeaderOption;
	
	public EmployeeAdapter(Context context, List<ContactsInfo> infos) {
		super(context, infos);
		// TODO Auto-generated constructor stub
//		addColors();
		mHeaderOption = new DisplayImageOptions.Builder()
				.imageScaleType(ImageScaleType.EXACTLY)
				.showImageOnLoading(R.drawable.emp_default_icon)
				.showImageForEmptyUri(R.drawable.emp_default_icon)
				.showImageOnFail(R.drawable.emp_default_icon)
				.cacheInMemory(true)
				.considerExifParams(true)
				.bitmapConfig(Bitmap.Config.ARGB_8888).build();
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		ContactsInfo info = getItem(position);
		ViewHolder holder;
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.contacts_employee_item_layout, null);
			holder = new ViewHolder();
			holder.parent = (LinearLayout) convertView
					.findViewById(R.id.emp_a_parent);
			holder.title = (TextView) convertView
					.findViewById(R.id.emp_a);
			holder.shapeName = (TextView) convertView
					.findViewById(R.id.emp_shap_name);
			holder.name = (TextView) convertView
					.findViewById(R.id.emp_name);
			holder.icon = (ContainerCircleImageView) convertView.findViewById(R.id.emp_icon);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		holder.name.setText(info.getName());
//		holder.shapeName.setText(info.getNameIcon());
//		((GradientDrawable)holder.shapeName.getBackground()).setColor(/*HcUtil.getShapColor()*/mColors.get(position));
		if (!TextUtils.isEmpty(info.getAlphabet()) && position == getPositionForSection(info.getAlphabet().charAt(0))) { // info.getAlphabet()这里可能为空
			holder.parent.setVisibility(View.VISIBLE);
			holder.title.setText(info.getAlphabet());
		} else {
			holder.parent.setVisibility(View.GONE);
		}
//		String uri = HcUtil.getScheme() + "/terminalServer/szf/getEmpIcon?user_id=" + info.getUserId() +
//				"&clientId="+ HcConfig.getConfig().getClientId() +"&token=-1";
		String uri = HcUtil.getHeaderUri(info.getUserId());
//		HcLog.D("EmployeeAdapter icon uri = "+uri + "   position = "+position);
		ImageLoader.getInstance().displayImage(uri,
				holder.icon, mHeaderOption);
		return convertView;
	}

	private class ViewHolder {
		LinearLayout parent;
		TextView title;
		TextView shapeName;
		TextView name;
		ContainerCircleImageView icon;
	}
	
	@Override
	public int getPositionForSection(int section) {
		int size = mInfos.size();
		for (int i = 0; i < size; i++) {
			String alphabet = mInfos.get(i).getAlphabet();
			if (!TextUtils.isEmpty(alphabet)) {
				char firstChar = alphabet.charAt(0);
				if (firstChar == section) {
					return i;
				}
			}
			
		}
		return -1;
	}

	@Override
	public int getSectionForPosition(int arg0) {
		return 0;
	}

	@Override
	public Object[] getSections() {
		return null;
	}

	@Override
	public void notifyDataSetChanged() {
		// TODO Auto-generated method stub
		// 重新获取颜色值
//		addColors();
		super.notifyDataSetChanged();
	}

	/**
	 * @deprecated
	 */
	public void addColors() {;
		/** 不用文字,用用户的头像替代了
		int size = getCount();
		if (size == mColors.size()) return;
		mColors.clear();
		Random random = new Random();
		for (int i = 0; i < size; i++) {
			int index = random.nextInt(6);
			while (index == mPreColorIndex) {
				index = random.nextInt(6);
			}
			mPreColorIndex = index;
			mColors.add(colors[index]);
		}
		
		mPreColorIndex = -1;
		*/
	}
}
