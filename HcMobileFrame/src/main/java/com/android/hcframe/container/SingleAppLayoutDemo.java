/*
* @Company 浙 江 鸿 程 计 算 机 系 统 有 限 公 司
* @URL http://www.zjhcsoft.com
* @Address 杭州滨江区伟业路1号
* @Email jinjr@zjhcsoft.com 
* @author jinjr
* @data 2015-11-18 下午4:44:24
*/
package com.android.hcframe.container;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.hcframe.R;
import com.android.hcframe.container.data.ViewInfo;

public class SingleAppLayoutDemo extends AppViewLayout {

	@Override
	public View createAppView(final Context context, ViewGroup parent, ViewInfo info) {
		// TODO Auto-generated method stub
		View layout = ((LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).
				inflate(R.layout.container_singleapp_layout01, parent, false);
		mAppViewInfo = info;
		List<ViewInfo> apps = info.getViewInfos();
		if (!apps.isEmpty()) {
			final ViewInfo app = apps.get(0);
			layout.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					app.onClick(context, ViewInfo.CLICK_TYPE_APP);
				}
			});
			
			/** 元素列表 */
			apps = new ArrayList<ViewInfo>(app.getViewInfos()); // 获取元素列表,这里新建主要是为了遍历的时候可以删除
			ContainerImageView icon = (ContainerImageView) layout.findViewById(R.id.container_single01_img01);
			ContainerTextView title = (ContainerTextView) layout.findViewById(R.id.container_single01_text01);
			ContainerTextView subTitle = (ContainerTextView) layout.findViewById(R.id.container_single01_text01);
			Iterator<ViewInfo> iterator = apps.iterator();
			ViewInfo element;
			while (iterator.hasNext()) {
				element = iterator.next();
				if (element.getRequestType() == ViewInfo.VALUE_REQUEST_NONE && 
						(element.getViewId().equals(title.getElementId())  || element.getViewId().equals(subTitle.getElementId()))) {
					
				}
			}
			
			
			for (ViewInfo viewInfo : apps) {
				if (viewInfo.getViewId().equals(icon.getElementId())) {
					icon.setValue(viewInfo.getElementValue(), viewInfo.getRequestType(), viewInfo.getAttrId());
					continue;
				}
				
				if (viewInfo.getViewId().equals(title.getElementId())) {
					title.setValue(viewInfo.getElementValue(), viewInfo.getRequestType(), viewInfo.getAttrId());
					continue;
				}
				
				if (viewInfo.getViewId().equals(subTitle.getElementId())) {
					subTitle.setValue(viewInfo.getElementValue(), viewInfo.getRequestType(), viewInfo.getAttrId());
					continue;
				}
			}
		} 
		mParent = layout;
		return layout;
	}

	@Override
	public void onResume() {
		// TODO Auto-generated method stub
	}

}
