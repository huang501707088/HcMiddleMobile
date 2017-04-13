package com.android.hcframe.netdisc;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.hcframe.HcLog;
import com.android.hcframe.netdisc.netdisccls.MySkydriveInfoItem;

import org.json.JSONObject;

import java.util.List;

/**
 * Created by pc on 2016/6/22.
 */
public class MyShareAdapter extends BaseAdapter {

    private List<JSONObject> mySkydriveItem;
    private Context mContext;
    private LayoutInflater mInflater;
    private static ISuccessExecute successExecute;

    public MyShareAdapter(Context context, List<JSONObject> list, ISuccessExecute sExecute) {
        mySkydriveItem = list;
        mContext = context;
        mInflater = LayoutInflater.from(context);
        successExecute = sExecute;
    }

    @Override
    public int getCount() {
        return mySkydriveItem.size();
    }

    @Override
    public Object getItem(int position) {
        return mySkydriveItem.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final JSONObject item = (JSONObject) getItem(position);
        MySkydriveViewHolder mySkydriveViewHolder = null;
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.netdisc_my_share_item,
                    parent, false);
            mySkydriveViewHolder = new MySkydriveViewHolder();
            mySkydriveViewHolder.mNetdiscListImg = (ImageView) convertView
                    .findViewById(R.id.netdisc_search_list_img);
            mySkydriveViewHolder.mNetdiscListText = (TextView) convertView.findViewById(R.id.netdisc_search_list_text);
            mySkydriveViewHolder.mNetdiscListCon = (TextView) convertView.findViewById(R.id.netdisc_id_tv_con);
            mySkydriveViewHolder.netdosc_id_iv_lock = (ImageView) convertView.findViewById(R.id.netdosc_id_iv_lock);
            mySkydriveViewHolder.netdisc_id_iv_del_img = (ImageView) convertView.findViewById(R.id.netdisc_id_iv_del_img);
            convertView.setTag(mySkydriveViewHolder);
        } else {
            mySkydriveViewHolder = (MySkydriveViewHolder) convertView.getTag();
        }
        if ("0".equals(item.optString("extType"))) { // 未知
            mySkydriveViewHolder.mNetdiscListImg.setImageResource(R.drawable.netdisc_none_file);
        } else if ("1".equals(item.optString("extType"))) {
            mySkydriveViewHolder.mNetdiscListImg.setImageResource(R.drawable.netdisc_search_list_img);
        } else if ("2".equals(item.optString("extType"))) {
            mySkydriveViewHolder.mNetdiscListImg.setImageResource(R.drawable.netdisc_word_file);
        } else if ("3".equals(item.optString("extType"))) {
            mySkydriveViewHolder.mNetdiscListImg.setImageResource(R.drawable.netdisc_excel_file);
        } else if ("4".equals(item.optString("extType"))) {
            mySkydriveViewHolder.mNetdiscListImg.setImageResource(R.drawable.netdisc_ppt_file);
        } else if ("5".equals(item.optString("extType"))) {
            mySkydriveViewHolder.mNetdiscListImg.setImageResource(R.drawable.netdisc_img_file);
        } else if ("6".equals(item.optString("extType"))) {
            mySkydriveViewHolder.mNetdiscListImg.setImageResource(R.drawable.netdisc_zip_file);
        } else if ("7".equals(item.optString("extType"))) {
            mySkydriveViewHolder.mNetdiscListImg.setImageResource(R.drawable.netdisc_none_file);
        }
        mySkydriveViewHolder.mNetdiscListText.setText(item.optString("infoname"));
        if ("B".equals(item.optString("type"))) {
            mySkydriveViewHolder.netdosc_id_iv_lock.setVisibility(View.VISIBLE);
        } else {
            mySkydriveViewHolder.netdosc_id_iv_lock.setVisibility(View.INVISIBLE);
        }
        mySkydriveViewHolder.mNetdiscListCon.setText("浏览:" + item.optString("browsenum") + "       下载:" + item.optString("downnum"));
        mySkydriveViewHolder.netdisc_id_iv_del_img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                successExecute.successExecute(position);
            }
        });
        return convertView;
    }


    public static class MySkydriveViewHolder {
        ImageView mNetdiscListImg;
        TextView mNetdiscListText;
        TextView mNetdiscListCon;
        ImageView netdosc_id_iv_lock;
        ImageView netdisc_id_iv_del_img;
    }

    public interface ISuccessExecute {
        void successExecute(int position);

    }
}
