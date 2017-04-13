package com.android.hcframe.view;

import android.app.Dialog;
import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import com.android.hcframe.R;

public class OneBtnAlterDialog extends Dialog {

	private static OneBtnAlterDialog oneBtnDialog = null;
	public static TextView tvAlterContent;
	public static Button btn_ok, btn_cancel;

	public OneBtnAlterDialog(Context context) {
		super(context);
	}

	public OneBtnAlterDialog(Context context, int theme) {
		super(context, theme);
	}

	public static OneBtnAlterDialog createDialog(Context context,String msg) {
		oneBtnDialog = new OneBtnAlterDialog(context, R.style.CustomAlterDialog);
		LayoutInflater inflater = LayoutInflater.from(context);
		View view = inflater.inflate(R.layout.alter_dialog_one, null);
		oneBtnDialog.setContentView(view);
		Window win = oneBtnDialog.getWindow();
		win.getAttributes().gravity = Gravity.CENTER;
//		win.setWindowAnimations(R.style.dialogWindowAnimTop);
		
		tvAlterContent = (TextView) oneBtnDialog.findViewById(R.id.tv_alter_content);
		tvAlterContent.setText(msg);
		btn_ok = (Button) oneBtnDialog.findViewById(R.id.btn_two_ok);
//		btn_cancel = (Button) oneBtnDialog.findViewById(R.id.btn_two_cancel);
		return oneBtnDialog;
	}

}
