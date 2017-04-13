package com.android.hcframe.internalservice.sign;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.hcframe.internalservice.signin.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Random;

public class WorkDetailAdapter extends BaseAdapter {
    private Context context;
    public JSONArray jsonArray;
    private TextView item_text;
    private int[] colors = {Color.parseColor("#78cae8"), Color.parseColor("#63dea3"),
            Color.parseColor("#c97bf1"), Color.parseColor("#f1b533"),
            Color.parseColor("#ff5599"), Color.parseColor("#f96633")};
    private Random mRandom;
    private int mPreColorIndex = -1;

    public WorkDetailAdapter(Context context, JSONArray jsonArray) {
        this.context = context;
        this.jsonArray = jsonArray;
        mRandom = new Random();
    }

    @Override
    public int getCount() {
        return jsonArray == null ? 0 : jsonArray.length();
    }

    @Override
    public JSONObject getItem(int position) {
        if (jsonArray != null && jsonArray.length() != 0) {
            try {
                return (JSONObject) jsonArray.get(position);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder mHolder = null;
        View view = convertView;
        if (view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.workdetail_item_layout, null);
            mHolder = new ViewHolder();
            mHolder.emp_shap_name = (TextView) view.findViewById(R.id.emp_shap_name);
            mHolder.emp_name = (TextView) view.findViewById(R.id.emp_name);
            mHolder.id_iv_type = (ImageView) view.findViewById(R.id.id_iv_type);
            view.setTag(mHolder);
        } else {
            mHolder = (ViewHolder) view.getTag();
        }
        JSONObject jsonObject = getItem(position);
        mHolder.emp_shap_name.setText(getNameIcon(jsonObject.optString("empName")));
        ((GradientDrawable) mHolder.emp_shap_name.getBackground()).setColor(/*HcUtil.getShapColor()*/getColor());
        mHolder.emp_name.setText(jsonObject.optString("empName"));
        int signStatus = jsonObject.optInt("signStatus");
        mHolder.id_iv_type.setVisibility(View.VISIBLE);
        switch (signStatus) {
            case 0:
                break;
            case 1:
                mHolder.id_iv_type.setImageResource(R.drawable.normal_img);
                break;
            case 2:
                mHolder.id_iv_type.setImageResource(R.drawable.late_img);
                break;
            case 3:
                mHolder.id_iv_type.setImageResource(R.drawable.sign_img);
                break;
            case 4:
                mHolder.id_iv_type.setImageResource(R.drawable.not_img);
                break;
            case 5:
                //请假标志
                mHolder.id_iv_type.setImageResource(R.drawable.leave_img);
                break;
        }

        return view;
    }

    static class ViewHolder {
        TextView emp_shap_name;
        TextView emp_name;
        ImageView id_iv_type;
    }

    private int getColor() {
        int index = mRandom.nextInt(6);
        while (index == mPreColorIndex) {
            index = mRandom.nextInt(6);
        }
        mPreColorIndex = index;
        return colors[index];
    }

    private String getNameIcon(String name) {
        if (name != null && name.length() > 0) {
            if (name.length() > 2 && name.length() < 4) {
                return name.substring(1, name.length());
            } else if (name.length() <= 2) {
                return name;
            } else {
                return name.substring(0, 2);
            }
        } else {
            return "";
        }
    }
}