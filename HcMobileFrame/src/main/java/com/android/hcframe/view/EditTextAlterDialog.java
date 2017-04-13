package com.android.hcframe.view;

import android.app.Dialog;
import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.hcframe.R;

import java.util.List;

public class EditTextAlterDialog extends Dialog {

    private static EditTextAlterDialog editTextAlterDialog = null;
    public static TextView tvAlterContent;
    public static Button btn_ok, btn_cancel;
    public  static EditText ebook_id_et;

    public EditTextAlterDialog(Context context) {
        super(context);
    }

    public EditTextAlterDialog(Context context, int theme) {
        super(context, theme);
    }

    public static AutoLineFeedLayout llTags;

    public static EditTextAlterDialog createDialog(Context context, List<String> listmsg) {
        editTextAlterDialog = new EditTextAlterDialog(context, R.style.CustomAlterDialog);
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.alter_dialog_edit, null);
        editTextAlterDialog.setContentView(view);
        Window win = editTextAlterDialog.getWindow();
        win.getAttributes().gravity = Gravity.CENTER;
        llTags = (AutoLineFeedLayout) editTextAlterDialog.findViewById(R.id.rg_tags);
        ebook_id_et = (EditText) editTextAlterDialog.findViewById(R.id.ebook_id_et);
        btn_ok = (Button) editTextAlterDialog.findViewById(R.id.btn_two_ok);
        btn_cancel = (Button) editTextAlterDialog.findViewById(R.id.btn_two_cancel);
        initTags(context, listmsg);
        return editTextAlterDialog;
    }

    public static void initTags(Context context, final List<String> listTags) {
        llTags.removeAllViews();
        float density = getScreenDensity(context);
        for (int i = 0; i < listTags.size(); i++) {
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
                    RelativeLayout.LayoutParams.WRAP_CONTENT);
            params.setMargins(0, 0, 0, 0);
//            RadioButton childButton = new RadioButton(this);
            TextView childButton = new TextView(context);
            childButton.setTextColor(context.getResources().getColor(R.color.white));
            childButton.setText(listTags.get(i));
            childButton.setTextSize(14);
            childButton.setLayoutParams(params);
            childButton.setPadding((int) (10 * density), (int) (5 * density), (int) (10 * density), (int) (5 * density));
            childButton.setId(i);
            childButton.setBackgroundResource(R.drawable.shape_corner_reg_gray);
            childButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int number = view.getId();
                    final String name = listTags.get(number);
                    ebook_id_et.setText(name);
//                    TextView tv = (TextView) view;
//                    boolean b = false;
//                    if (listTags.size() > 0) {
//                        for (int i = 0; i < listTags.size(); i++) {
//                            if (list.get(number).toString().equals(listTags.get(i))) {
//                                listTag.remove(i);
//                                b = true;
//                            }
//                        }
//                    } else {
//                        listTag.add(list.get(number));
//                    }
//                    if (b) {
//                        view.setSelected(false);
//                        tv.setTextColor(getResources().getColor(R.color.gray_ab));
//                    } else {
//                        listTag.add(list.get(number));
//                        tv.setTextColor(getResources().getColor(R.color.yellow_ff));
//                        view.setSelected(true);
//                    }
                }
            });
            llTags.addView(childButton);
        }
    }

    public static float getScreenDensity(Context context) {
        return context.getResources().getDisplayMetrics().density;
    }
}
