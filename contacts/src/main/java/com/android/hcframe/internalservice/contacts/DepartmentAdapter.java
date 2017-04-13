/*
* @Company 浙 江 鸿 程 计 算 机 系 统 有 限 公 司
* @URL http://www.zjhcsoft.com
* @Address 杭州滨江区伟业路1号
* @Email jinjr@zjhcsoft.com 
* @author jinjr
* @data 2015-12-10 下午12:43:53
*/
package com.android.hcframe.internalservice.contacts;

import java.util.List;
import java.util.Random;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AbsListView.LayoutParams;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.hcframe.HcConfig;
import com.android.hcframe.HcLog;
import com.android.hcframe.HcUtil;
import com.android.hcframe.adapter.HcBaseAdapter;
import com.android.hcframe.contacts.data.ContactsInfo;
import com.android.hcframe.contacts.data.EmployeeInfo;
import com.android.hcframe.container.ContainerCircleImageView;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;

public class DepartmentAdapter extends HcBaseAdapter<ContactsInfo> {

	private LayoutParams mParams;
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

	/**
	 * @deprecated
	 */
	private Random mRandom;

	private DisplayImageOptions mHeaderOption;
	
	public DepartmentAdapter(Context context, List<ContactsInfo> infos) {
		super(context, infos);
		// TODO Auto-generated constructor stub
		int height = context.getResources().getDimensionPixelSize(R.dimen.contacts_dep_list_item_height);
		mParams = new LayoutParams(LayoutParams.MATCH_PARENT, height);
//		mRandom = new Random();
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
		int type = getItemViewType(position);

		EmpHolder empHolder = null;

		DerpHolder derpHolder = null;

		final ContactsInfo ci = getItem(position);

		if (convertView == null) {
			if (type == 0) {
				empHolder = new EmpHolder();
				convertView = initEmpHolder(empHolder);
			} else if (type == 1) {
				derpHolder = new DerpHolder();
				convertView = initDerpHolder(derpHolder);
			}
		} else {
			Object tag = convertView.getTag();
			HcLog.D("DepartmentAdapter getView view tag = "+tag);
			if (type == 0) {
				if (tag instanceof EmpHolder)
					empHolder = (EmpHolder) convertView.getTag();
				else {
					tag = null;
					convertView = null;
					empHolder = new EmpHolder();
					convertView = initEmpHolder(empHolder);
				}
			} else if (type == 1) {
				if (tag instanceof DerpHolder)
					derpHolder = (DerpHolder) convertView.getTag();	
				else {
					tag = null;
					convertView = null;
					derpHolder = new DerpHolder();
					convertView = initDerpHolder(derpHolder);
				}
			}
		}

		if (type == 0) {
//			empHolder.shapeName.setText(ci.getNameIcon());
//			((GradientDrawable)empHolder.shapeName.getBackground()).setColor(/*HcUtil.getShapColor()*/getColor());
			empHolder.name.setText(ci.getName());
			ImageLoader.getInstance().displayImage(HcUtil.getHeaderUri(ci.getUserId()),
					empHolder.icon, mHeaderOption);
		} else if (type == 1) {
			derpHolder.name.setText(ci.getName());
			derpHolder.count.setText(String.format(
					mContext.getString(R.string.contact_num),
					ci.getEmployeeCount() + ""));
			derpHolder.message
					.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View view) {

							ci.sendMessage((Activity) mContext, "");
						}
					});
		}

		return convertView;
	}

	private class EmpHolder {
		TextView shapeName;

		TextView name;

		ContainerCircleImageView icon;
	}

	private class DerpHolder {
		TextView name;

		TextView count;
		/**
		 * @deprecated
		 */
		ImageView message;
	}
	
	@Override
	public int getViewTypeCount() {

		return 2;
	}

	@Override
	public int getItemViewType(int position) {
		if (mInfos.get(position) instanceof EmployeeInfo) {
			return 0;
		} else {
			return 1;
		}
	}
	
	private View initEmpHolder(EmpHolder empHolder) {
		View convertView;
		convertView = mInflater.inflate(R.layout.contacts_employee_item_layout, null);
		convertView.setLayoutParams(mParams);
		empHolder.shapeName = (TextView) convertView
				.findViewById(R.id.emp_shap_name);
		empHolder.name = (TextView) convertView
				.findViewById(R.id.emp_name);
		empHolder.icon = (ContainerCircleImageView) convertView.findViewById(R.id.emp_icon);
		convertView.setTag(empHolder);
		return convertView;
	}

	private View initDerpHolder(DerpHolder derpHolder) {
		View convertView;
		convertView = mInflater.inflate(R.layout.contacts_department_item_layout, null);
		convertView.setLayoutParams(mParams);
		derpHolder.name = (TextView) convertView
				.findViewById(R.id.dep_name);
		derpHolder.count = (TextView) convertView
				.findViewById(R.id.dep_count);
		derpHolder.message = (ImageView) convertView
				.findViewById(R.id.dep_message);
		convertView.setTag(derpHolder);
		return convertView;
	}
	
	private int getColor() {
		int index = mRandom.nextInt(6);
		while (index == mPreColorIndex) {
			index = mRandom.nextInt(6);
		}
		mPreColorIndex = index;
		return colors[index];
	}
}
