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

public class TwoBtnAlterDialog extends Dialog {

	private static TwoBtnAlterDialog twoBtnDialog = null;
	public static TextView tvAlterContent;
	public static Button btn_ok, btn_cancel;

	public TwoBtnAlterDialog(Context context) {
		super(context);
	}

	public TwoBtnAlterDialog(Context context, int theme) {
		super(context, theme);
	}

	public static TwoBtnAlterDialog createDialog(Context context,String msg) {
		twoBtnDialog = new TwoBtnAlterDialog(context, R.style.CustomAlterDialog);
		LayoutInflater inflater = LayoutInflater.from(context);
		View view = inflater.inflate(R.layout.alter_dialog_two, null);
		twoBtnDialog.setContentView(view);
		Window win = twoBtnDialog.getWindow();
		win.getAttributes().gravity = Gravity.CENTER;
//		win.setWindowAnimations(R.style.dialogWindowAnimTop);
		
		tvAlterContent = (TextView) twoBtnDialog.findViewById(R.id.tv_alter_content);
		tvAlterContent.setText(msg);
		btn_ok = (Button) twoBtnDialog.findViewById(R.id.btn_two_ok);
		btn_cancel = (Button) twoBtnDialog.findViewById(R.id.btn_two_cancel);
		return twoBtnDialog;
	}

}
