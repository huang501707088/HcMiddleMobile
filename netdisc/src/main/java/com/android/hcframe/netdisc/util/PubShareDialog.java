package com.android.hcframe.netdisc.util;

import android.app.Dialog;
import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.LinearLayout;

import com.android.hcframe.netdisc.R;

public class PubShareDialog extends Dialog {

    private static PubShareDialog shareDialog = null;
    public static LinearLayout netdisc_id_pub_share, netdisc_id_pri_share;

    public PubShareDialog(Context context) {
        super(context);
    }

    public PubShareDialog(Context context, int theme) {
        super(context, theme);
    }

    public static PubShareDialog createDialog(Context context) {
        shareDialog = new PubShareDialog(context, R.style.CustomAlterDialog);
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.netdisc_alter_dialog_pubshare, null);
        shareDialog.setContentView(view);
        Window win = shareDialog.getWindow();
        win.getAttributes().gravity = Gravity.CENTER;
        netdisc_id_pub_share = (LinearLayout) shareDialog.findViewById(R.id.netdisc_id_pub_share);
        netdisc_id_pri_share = (LinearLayout) shareDialog.findViewById(R.id.netdisc_id_pri_share);
        return shareDialog;
    }

}
