package com.android.hcframe.push;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.android.hcframe.R;
import com.android.hcframe.adapter.HcBaseAdapter;

import java.util.List;

/**
 * @Company 浙 江 鸿 程 计 算 机 系 统 有 限 公 司
 * @URL http://www.zjhcsoft.com
 * @Address 杭州滨江区伟业路1号
 * @Email jinjr@zjhcsoft.com
 * Created by jrjin on 16-9-20 09:58.
 */

public class PushSettingsAdapter extends HcBaseAdapter<PushItem> {

    public PushSettingsAdapter(Context context, List<PushItem> infos) {
        super(context, infos);
    }

    private OnCheckCallback mCallback;

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final PushItem item = getItem(position);
        final ViewHolder holder;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = mInflater.inflate(R.layout.push_settings_item_layout, parent, false);
            holder.mCheck = (ToggleButton) convertView.findViewById(R.id.push_settings_state_switch);
            holder.mName = (TextView) convertView.findViewById(R.id.push_settings_module_name);
            holder.mParent = (LinearLayout) convertView.findViewById(R.id.push_settings_module_parent);
            holder.mTitle = (TextView) convertView.findViewById(R.id.push_settings_title);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        if (item instanceof PushModuleItem) {
            if (holder.mTitle.getVisibility() != View.VISIBLE)
                holder.mTitle.setVisibility(View.VISIBLE);
            if (holder.mParent.getVisibility() != View.GONE)
                holder.mParent.setVisibility(View.GONE);
            holder.mTitle.setText(item.getName());
        } else {
            if (holder.mTitle.getVisibility() != View.GONE)
                holder.mTitle.setVisibility(View.GONE);
            if (holder.mParent.getVisibility() != View.VISIBLE)
                holder.mParent.setVisibility(View.VISIBLE);
            holder.mName.setText(item.getName());
            holder.mCheck.setChecked(item.isPushed());
            holder.mCheck.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mCallback != null) {
                        mCallback.onCheckChanged(item, holder.mCheck);
                    }
                }
            });
        }

        return convertView;
    }

    private class ViewHolder {
        TextView mTitle;
        TextView mName;
        LinearLayout mParent;
        ToggleButton mCheck;
    }

    public interface OnCheckCallback {

        public void onCheckChanged(PushItem item, ToggleButton view);
    }

    public void setOnCheckCallback(OnCheckCallback callback) {
        mCallback = callback;
    }
}
